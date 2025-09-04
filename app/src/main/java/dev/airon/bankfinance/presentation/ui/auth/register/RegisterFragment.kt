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

        // 🔑 Gerar salt único e hash da senha de transação
        val salt = SecurityUtils.generateSalt()
        Log.i("INFOTEST", "validateData: ${salt}")
        val hashedPasswordTransaction = SecurityUtils.hashPassword(passwordTransaction, salt)

        val user = User(
            name = name,
            cpf = encryptData(cpf, secretKey),
            rg = encryptData(rg, secretKey),
            phone = encryptData(phone, secretKey),
            email = email,
            password = password,
            passwordTransaction = hashedPasswordTransaction,
            passwordSalt = salt
        )
        registerUser(user)
    }

    private fun registerUser(user: User) {
        registerViewModel.register(
            user.name,
            user.cpf,
            user.rg,
            user.phone,
            user.email,
            user.password,
            user.passwordTransaction,
             user.passwordSalt
        ).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> stateView.data?.let { saveProfile(it) }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(message = getString(FirebaseHelper.validError(stateView.message ?: "")))
                }
            }
        }
    }

    private fun saveProfile(user: User) {
        profileViewModel.saveProfile(user).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    initAccount(user)
                    findNavController().navigate(R.id.action_global_homeFragment)
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(message = getString(FirebaseHelper.validError(stateView.message ?: "")))
                }
            }
        }
    }

    private fun initAccount(user: User) {
        val account = Account(
            id = FirebaseHelper.getUserId(),
            name = user.name,
            branch = "0101",
            accountNumber = generateAccountNumber(),
            balance = 0f
        )
        initWallet()
        initCreditCard(account)
        accountViewModel.initAccount(account).observe(viewLifecycleOwner) { stateView ->
            binding.progressBar.visibility = if (stateView is StateView.Loading) View.VISIBLE else View.INVISIBLE
            if (stateView is StateView.Error) {
                showBottomSheet(message = getString(FirebaseHelper.validError(stateView.message ?: "")))
            }
        }
    }

    private fun initWallet() {
        walletViewModel.initWallet(Wallet(userId = FirebaseHelper.getUserId()))
            .observe(viewLifecycleOwner) { stateView ->
                binding.progressBar.visibility = if (stateView is StateView.Loading) View.VISIBLE else View.INVISIBLE
                if (stateView is StateView.Error) {
                    showBottomSheet(message = getString(FirebaseHelper.validError(stateView.message ?: "")))
                }
            }
    }

    private fun initCreditCard(account: Account) {
        val card = CreditCard(
            id = FirebaseHelper.getUserId(),
            number = CreditCardGenerator.generateNumber(),
            account = account,
            securityCode = CreditCardGenerator.generateSecurityCode(),
            officialUser = account.name,
            limit = CreditCardGenerator.generateLimit(),
            validDate = CreditCardGenerator.generateValidDate(),
            balance = 0f
        )
        creditCardViewModel.initCreditCard(card).observe(viewLifecycleOwner) { stateView ->
            binding.progressBar.visibility = if (stateView is StateView.Loading) View.VISIBLE else View.INVISIBLE
            if (stateView is StateView.Error) showBottomSheet(message = "Erro ao criar cartão")
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

    // 🔐 Hash com salt único
    private fun hashPassword(password: String, salt: String): String {
        val iterations = 65536
        val keyLength = 256
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), iterations, keyLength)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    // 🔑 Gerar salt aleatório
    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        Random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}


