package dev.airon.bankfinance.presenter.auth.register

import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.data.model.Account
import dev.airon.bankfinance.data.model.CreditCard
import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.data.model.Wallet
import dev.airon.bankfinance.databinding.FragmentRegisterBinding
import dev.airon.bankfinance.presenter.features.creditCard.CreditCardViewModel
import dev.airon.bankfinance.presenter.profile.ProfileViewModel
import dev.airon.bankfinance.presenter.wallet.WalletViewModel
import dev.airon.bankfinance.util.ColorStatusBar
import dev.airon.bankfinance.util.CreditCardGenerator
import dev.airon.bankfinance.util.FirebaseHelper
import dev.airon.bankfinance.util.StateView
import dev.airon.bankfinance.util.addCpfMask
import dev.airon.bankfinance.util.addRgMask
import dev.airon.bankfinance.util.hideKeyboard
import dev.airon.bankfinance.util.initToolbar
import dev.airon.bankfinance.util.onlyDigits
import dev.airon.bankfinance.util.showBottomSheet
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlin.random.Random

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val walletViewModel: WalletViewModel by viewModels()
    private val creditCardViewModel: CreditCardViewModel by viewModels()

    private lateinit var secretKey: SecretKey
    private var accountNumber: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ColorStatusBar(R.color.white)
        initToolbar(binding.toolbar)
        secretKey = generateKey() // Gera uma chave secreta para criptografia
        initMaskCPFandRG()
        initListener()
    }

    private fun initMaskCPFandRG() {
        binding.editCpf.addCpfMask()
        binding.editRg.addRgMask()
    }

    private fun initListener() {
        binding.btnCreateAccount.setOnClickListener {
            validateData()
        }
    }

    private fun validateData() {
        val name = binding.editName.text.toString().trim()
        val phone = binding.editPhone.unMaskedText
        val cpf = binding.editCpf.text.toString().onlyDigits()
        val rg = binding.editRg.text.toString().onlyDigits()
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString().trim()
        val passwordTransaction = binding.editPasswordTransaction.text.toString().trim()

        if (name.isNotEmpty()) {
            if (phone?.isNotEmpty() == true && phone.length == 11) {
                if (cpf.isNotEmpty() && rg.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && passwordTransaction.isNotEmpty()) {
                    hideKeyboard()
                    val user = User(
                        name = name,
                        cpf = encryptData(cpf, secretKey),
                        rg = encryptData(rg, secretKey),
                        phone = encryptData(phone, secretKey),
                        email = email,
                        password = password,
                        passwordTransaction = passwordTransaction
                    )
                    registerUser(user)
                } else {
                    showBottomSheet(message = "Preencha todos os campos corretamente.")
                }
            } else {
                showBottomSheet(message = "Digite um telefone válido com 11 dígitos.")
            }
        } else {
            showBottomSheet(message = getString(R.string.name_is_empty_alert))
        }
    }

    private fun registerUser(user: User) {
        registerViewModel.register(
            user.name,
            user.cpf,
            user.rg,
            user.phone,
            user.email,
            user.password,
            user.passwordTransaction
        ).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> {
                    stateView.data?.let { saveProfile(it) }
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(
                        message = getString(FirebaseHelper.validError(stateView.message ?: ""))
                    )
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
                    // cria conta, carteira e cartão
                    initAccount(user)
                    // leva para home
                    findNavController().navigate(R.id.action_global_homeFragment)
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(
                        message = getString(FirebaseHelper.validError(stateView.message ?: ""))
                    )
                }
            }
        }
    }

    private fun initAccount(user: User) {
        val account = Account(
            id = FirebaseHelper.getUserId(),
            name = user.name,
            accountNumber = generateAccountNumber(),
            balance = 0f
        )
        // ⚠️ Aqui você pode futuramente mover para AccountRepository + UseCase
        initWallet()
        initCreditCard(account)
    }

    private fun initWallet() {
        walletViewModel.initWallet(
            Wallet(userId = FirebaseHelper.getUserId())
        ).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> binding.progressBar.visibility = View.INVISIBLE
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(
                        message = getString(FirebaseHelper.validError(stateView.message ?: ""))
                    )
                }
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
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> binding.progressBar.visibility = View.INVISIBLE
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(message = "Erro ao criar cartão")
                }
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
        return Base64.encodeToString(
            cipher.doFinal(data.toByteArray(Charsets.UTF_8)),
            Base64.DEFAULT
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
