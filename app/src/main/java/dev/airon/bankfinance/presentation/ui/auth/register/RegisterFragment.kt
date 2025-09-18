package dev.airon.bankfinance.presentation.ui.auth.register

import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.core.extensions.ColorStatusBar
import dev.airon.bankfinance.core.extensions.addCpfMask
import dev.airon.bankfinance.core.extensions.addRgMask
import dev.airon.bankfinance.core.extensions.hideKeyboard
import dev.airon.bankfinance.core.extensions.initToolbar
import dev.airon.bankfinance.core.extensions.onlyDigits
import dev.airon.bankfinance.core.extensions.showBottomSheet
import dev.airon.bankfinance.core.util.CreditCardGenerator
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.SecurityUtils
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.databinding.FragmentRegisterBinding
import dev.airon.bankfinance.domain.model.Account
import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.domain.model.User
import dev.airon.bankfinance.domain.model.Wallet
import dev.airon.bankfinance.presentation.ui.features.account.AccountViewModel
import dev.airon.bankfinance.presentation.ui.features.creditCard.CreditCardViewModel
import dev.airon.bankfinance.presentation.ui.profile.ProfileViewModel
import dev.airon.bankfinance.presentation.ui.wallet.WalletViewModel
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.random.Random

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels()
    private val creditCardViewModel: CreditCardViewModel by viewModels()
    private val accountViewModel: AccountViewModel by viewModels()
    private lateinit var secretKey: SecretKey

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ColorStatusBar(R.color.white)
        initToolbar(binding.toolbar)
        secretKey = generateKey()
        initMaskCPFandRG()
        initListener()
    }

    private fun initMaskCPFandRG() {
        binding.editCpf.addCpfMask()
        binding.editRg.addRgMask()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListener() {
        binding.btnCreateAccount.setOnClickListener {
            validateData()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateData() {
        val name = binding.editName.text.toString().trim()
        val phone = binding.editPhone.unMaskedText
        val cpf = binding.editCpf.text.toString().onlyDigits()
        val rg = binding.editRg.text.toString().onlyDigits()
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()
        val passwordTransaction = binding.editPasswordTransaction.text.toString().trim()

        if (name.isEmpty()) {
            showBottomSheet(message = getString(R.string.name_is_empty_alert))
            return
        }
        if (phone.isNullOrEmpty() || phone.length != 11) {
            showBottomSheet(message = "Digite um telefone válido com 11 dígitos.")
            return
        }
        if (cpf.isEmpty() || rg.isEmpty() || email.isEmpty() || password.isEmpty() || passwordTransaction.isEmpty()) {
            showBottomSheet(message = "Preencha todos os campos corretamente.")
            return
        }

        hideKeyboard()

        val salt = SecurityUtils.generateSalt()
        val hashedPasswordTransaction = SecurityUtils.hashPassword(passwordTransaction, salt)

        // Criar usuário SEM ID inicialmente, o ID virá do Firebase Auth
        val userToRegister = User(
            name = name,
            cpf = encryptData(cpf, secretKey),
            rg = encryptData(rg, secretKey),
            phone = encryptData(phone, secretKey),
            email = email,
            password = password,
            passwordTransaction = hashedPasswordTransaction,
            passwordSalt = salt
            // id será preenchido após o registro no Firebase Auth
        )
        registerUser(userToRegister)
    }

    private fun registerUser(userToRegister: User) {
        registerViewModel.register(
            userToRegister.name,
            userToRegister.cpf,
            userToRegister.rg,
            userToRegister.phone,
            userToRegister.email,
            userToRegister.password,
            userToRegister.passwordTransaction,
            userToRegister.passwordSalt
        ).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> {
                    // Espera-se que stateView.data (User) agora tenha o 'id' preenchido
                    // com o UID do Firebase Auth, retornado pelo RegisterViewModel/Repository.
                    stateView.data?.let { registeredUserWithId ->
                        if (registeredUserWithId.id.isNotBlank()) {
                            Log.i("RegisterFragment", "Usuário registrado com ID: ${registeredUserWithId.id}")
                            saveProfile(registeredUserWithId)
                        } else {
                            binding.progressBar.visibility = View.INVISIBLE
                            showBottomSheet(message = "Falha ao obter ID do usuário após registro.")
                            Log.e("RegisterFragment", "ID do usuário vazio após registro bem-sucedido.")
                        }
                    } ?: run {
                        binding.progressBar.visibility = View.INVISIBLE
                        showBottomSheet(message = "Dados do usuário não retornados após registro.")
                        Log.e("RegisterFragment", "Dados do usuário nulos após registro bem-sucedido.")
                    }
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(message = getString(FirebaseHelper.validError(stateView.message ?: "")))
                    Log.e("RegisterFragment", "Erro no registro: ${stateView.message}")
                }
            }
        }
    }

    private fun saveProfile(userWithId: User) { // userWithId já tem o ID do Firebase Auth
        profileViewModel.saveProfile(userWithId).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    Log.i("RegisterFragment", "Perfil salvo para usuário ID: ${userWithId.id}. Iniciando criação de conta, wallet e cartão.")
                    // Agora passamos o userWithId para a próxima etapa
                    initializeFinancialEntities(userWithId)
                    // Navega para Home APÓS todas as inicializações terem sido disparadas
                    // Idealmente, você esperaria a conclusão de todas antes de navegar,
                    // mas para simplificar, disparamos e navegamos.
                    // Para uma UI mais robusta, considere usar coroutines com joinAll ou similar.
                    findNavController().navigate(R.id.action_global_homeFragment)
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(message = getString(FirebaseHelper.validError(stateView.message ?: "")))
                    Log.e("RegisterFragment", "Erro ao salvar perfil: ${stateView.message}")
                }
            }
        }
    }

    // Nova função para agrupar a inicialização de Account, Wallet e CreditCard
    private fun initializeFinancialEntities(userWithId: User) {
        val userId = userWithId.id // Usar o ID do usuário obtido do Firebase Auth

        // 1. Criar e Inicializar a Conta Bancária
        val account = Account(
            id = userId, // ID da conta é o ID do usuário
            name = userWithId.name,
            branch = "0101", // Exemplo
            accountNumber = generateAccountNumber(),
            balance = 0f // Saldo inicial da conta bancária
        )
        // Dispara a inicialização da conta
        accountViewModel.initAccount(account).observe(viewLifecycleOwner) { stateView ->
            if (stateView is StateView.Loading) Log.d("RegisterFragment", "Iniciando Account...")
            if (stateView is StateView.Success) Log.i("RegisterFragment", "Account inicializada para usuário ID: $userId")
            if (stateView is StateView.Error) {
                Log.e("RegisterFragment", "Erro ao inicializar Account para ID $userId: ${stateView.message}")
                // Você pode mostrar um BottomSheet aqui também ou tratar o erro
            }
        }

        // 2. Criar e Inicializar a Wallet
        val wallet = Wallet(
            id = userId, // ID da Wallet é o ID do usuário
            userId = userId,
            balance = 0f // Saldo inicial da wallet
        )
        // Dispara a inicialização da wallet
        walletViewModel.initWallet(wallet).observe(viewLifecycleOwner) { stateView ->
            if (stateView is StateView.Loading) Log.d("RegisterFragment", "Iniciando Wallet...")
            if (stateView is StateView.Success) Log.i("RegisterFragment", "Wallet inicializada para usuário ID: $userId")
            if (stateView is StateView.Error) {
                Log.e("RegisterFragment", "Erro ao inicializar Wallet para ID $userId: ${stateView.message}")
            }
        }

        // 3. Criar e Inicializar o Cartão de Crédito
        val creditCard = CreditCard(
            id = userId, // ID do objeto CreditCard é o ID do usuário
            number = CreditCardGenerator.generateNumber(),
            account = account, // Associa a conta bancária
            securityCode = CreditCardGenerator.generateSecurityCode(),
            officialUser = userWithId.name,
            limit = CreditCardGenerator.generateLimit(), // Defina um limite padrão
            validDate = CreditCardGenerator.generateValidDate(),
            balance = 0f // Fatura inicial do cartão é zero
        )
        // Dispara a inicialização do cartão de crédito
        creditCardViewModel.initCreditCard(creditCard).observe(viewLifecycleOwner) { stateView ->
            if (stateView is StateView.Loading) Log.d("RegisterFragment", "Iniciando CreditCard...")
            if (stateView is StateView.Success) {
                Log.i("RegisterFragment", "CreditCard inicializado para usuário ID: $userId com ID de objeto ${creditCard.id}")
            }
            if (stateView is StateView.Error) {
                Log.e("RegisterFragment", "Erro ao inicializar CreditCard para ID $userId: ${stateView.message}")
                showBottomSheet(message = "Erro ao criar cartão: ${stateView.message}")
            }
        }
    }

    private fun generateAccountNumber(): String = (100000..999999).random().toString()

    private fun generateKey(): SecretKey {
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256)
        return keyGen.generateKey()
    }

    private fun encryptData(data: String, key: SecretKey): String {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return Base64.encodeToString(cipher.doFinal(data.toByteArray(Charsets.UTF_8)), Base64.DEFAULT)
    }

    private fun hashPassword(password: String, salt: String): String {
        val iterations = 65536
        val keyLength = 256
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), iterations, keyLength)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        Random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



