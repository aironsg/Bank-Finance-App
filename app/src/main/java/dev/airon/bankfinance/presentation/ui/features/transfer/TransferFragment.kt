package dev.airon.bankfinance.presentation.ui.features.transfer



import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.zxing.integration.android.IntentIntegrator
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.core.extensions.addEmailValidation
import dev.airon.bankfinance.core.extensions.addMoneyMask
import dev.airon.bankfinance.core.extensions.bottomSheetPasswordTransaction
import dev.airon.bankfinance.core.extensions.initToolbar
import dev.airon.bankfinance.core.extensions.showBottomSheet
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.GetMask
import dev.airon.bankfinance.core.util.PixPayloadParser
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.data.enum.PaymentMethod
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.databinding.FragmentTransferBinding
import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.presentation.ui.home.HomeViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransferFragment : Fragment(R.layout.fragment_transfer) {

    private val transferViewModel: TransferViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private var balance: Float = 0f
    private var selectedPaymentMethod: PaymentMethod? = null
    private var isBalanceVisible = false

    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: android.view.View, savedInstanceState: android.os.Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTransferBinding.bind(view)
        initToolbar(binding.toolbar, isToolbarDefaultColor = true)
        observeTransactions()
        initRadioGroup()
        setupListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        binding.editValuePix.addMoneyMask() // ✅ máscara de valor (R$)
        binding.editKeyPix.addEmailValidation() // ✅ validação de email

        binding.btnConfirmTransaction.setOnClickListener { validateTransfer() }
        binding.btnQrCode.setOnClickListener { startQrScanner() }
        binding.btnSearchKey.setOnClickListener { searchPixKey() }

        binding.toggleVisibility.setOnClickListener {
            isBalanceVisible = !isBalanceVisible
            updateBalanceUI()
        }
    }

    private fun initRadioGroup() {
        binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
            selectedPaymentMethod = when (checkedId) {
                R.id.radio_credit_card -> PaymentMethod.CREDIT_CARD
                R.id.radio_balance -> PaymentMethod.BALANCE
                else -> null
            }
            validateForm()
        }
    }

    private fun validateForm() {
        val amountText = binding.editValuePix.text?.toString()
        val pixKey = binding.editKeyPix.text?.toString()
        val isFormValid = !amountText.isNullOrBlank() && !pixKey.isNullOrBlank() && selectedPaymentMethod != null

        if (isFormValid) {
            binding.frameLayout.visibility = android.view.View.VISIBLE
            val amount = amountText.toFloatOrNull() ?: 0f
            val method = when (selectedPaymentMethod) {
                PaymentMethod.CREDIT_CARD -> "Cartão de Crédito"
                PaymentMethod.BALANCE -> "Saldo Bancário"
                else -> ""
            }

            // Busca o nome do usuário de forma suspensa
            lifecycleScope.launch {
                val name = FirebaseHelper.getUserName() ?: "Usuário"
                binding.textUserName.text = name
                binding.textAmountTransaction.text = GetMask.getFormatedValue(amount)
                binding.textPhoneNumber.text = pixKey
                binding.textMethodPaymentValue.text = method
            }
        } else {
            binding.frameLayout.visibility = android.view.View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateTransfer() {
        val amountText = binding.editValuePix.text.toString()
        val pixKey = binding.editKeyPix.text.toString()

        if (amountText.isEmpty()) {
            Toast.makeText(requireContext(), "Digite um valor", Toast.LENGTH_SHORT).show()
            return
        }
        if (pixKey.isEmpty()) {
            Toast.makeText(requireContext(), "Digite ou leia uma chave Pix", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedPaymentMethod == null) {
            Toast.makeText(requireContext(), "Selecione um método de pagamento", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toFloat()
        when (selectedPaymentMethod) {
            PaymentMethod.BALANCE -> {
                if (amount > balance) {
                    showBottomSheet(message = "Saldo insuficiente para transferência")
                } else {
                    confirmationTransfer(pixKey, amount)
                }
            }
            PaymentMethod.CREDIT_CARD -> {
                fetchCreditCardLimit { limit ->
                    if (amount > limit) {
                        showBottomSheet(message = "Limite insuficiente no cartão")
                    } else {
                        confirmationTransfer(pixKey, amount)
                    }
                }
            }
            else -> Unit
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confirmationTransfer(pixKey: String, amount: Float) {
        showBottomSheet(
            titleDialog = R.string.txt_information_data_recharge_alert,
            message = "Valor: R$ ${GetMask.getFormatedValue(amount)}\n" +
                    "Chave Pix: $pixKey\n" +
                    "Método de Pagamento: ${PaymentMethod.getOperation(selectedPaymentMethod!!)}\n" +
                    "Deseja confirmar a transferência?",
            titleButton = R.string.txt_button_bottomSheet_confirm,
            onClick = {
                bottomSheetPasswordTransaction(
                    message = "Informe sua senha para confirmar a transferência",
                    titleButton = R.string.txt_button_bottomSheet_confirm
                ) {
                    sendTransfer(pixKey, amount)
                }
            }
        )
    }


    private fun sendTransfer(pixKey: String, amount: Float) {
        lifecycleScope.launch {
            val senderName = FirebaseHelper.getUserName() ?: "Usuário"
            val senderId = FirebaseHelper.getUserId()
            val recipientId = pixKey // se a chave Pix estiver associada ao UID do usuário, use aqui

            transferViewModel.sendPix(senderName, recipientName, pixKey, amount, senderId, recipientId)
                .observe(viewLifecycleOwner) { stateView ->
                    when (stateView) {
                        is StateView.Loading -> binding.progressBar.visibility = android.view.View.VISIBLE
                        is StateView.Success -> {
                            binding.progressBar.visibility = android.view.View.GONE
                            Toast.makeText(requireContext(), "Transferência realizada com sucesso", Toast.LENGTH_SHORT).show()
                        }
                        is StateView.Error -> {
                            binding.progressBar.visibility = android.view.View.GONE
                            showBottomSheet(message = stateView.message)
                        }
                    }
                }
        }
    }

    var recipientName: String = ""
    private fun searchPixKey() {
        val pixKey = binding.editKeyPix.text.toString().replace("[()\\s-]".toRegex(), "")
        if (pixKey.isEmpty()) {
            Toast.makeText(requireContext(), "Digite a chave Pix", Toast.LENGTH_SHORT).show()
            return
        }

        val userRef = FirebaseDatabase.getInstance().getReference("profile")
        userRef.orderByChild("email").equalTo(pixKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            recipientName = child.child("name").getValue(String::class.java) ?: "Usuário"
                            binding.textPhoneNumber.text = pixKey
                            binding.textUserName.text = recipientName
                            validateForm()
                        }
                    } else {
                        showBottomSheet(message = "Usuário não encontrado")
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    showBottomSheet(message = "Erro: ${error.message}")
                }
            })
    }

    private fun observeTransactions() {
        homeViewModel.getTransactions().observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Success -> {
                    balance = calculateBalance(stateView.data ?: emptyList())
                    updateBalanceUI()
                }
                is StateView.Error -> showBottomSheet(message = stateView.message)
                else -> Unit
            }
        }
    }

    private fun calculateBalance(transactions: List<Transaction>): Float {
        var cashIn = 0f
        var cashOut = 0f
        transactions.forEach { t ->
            if (t.type == TransactionType.CASH_IN) cashIn += t.amount
            else cashOut += t.amount
        }
        return cashIn - cashOut
    }

    private fun updateBalanceUI() {
        if (isBalanceVisible) {
            binding.balanceValue.text =
                getString(R.string.text_formated_value, GetMask.getFormatedValue(balance))
            binding.balanceValue.visibility = android.view.View.VISIBLE
            binding.toggleVisibility.setImageResource(R.drawable.ic_no_visibility)
        } else {
            binding.balanceValue.visibility = android.view.View.GONE
            binding.toggleVisibility.setImageResource(R.drawable.ic_visibility)
        }
    }

    private fun fetchCreditCardLimit(onResult: (Float) -> Unit) {
        val userId = FirebaseHelper.getUserId()
        val creditCardRef = FirebaseDatabase.getInstance()
            .getReference("creditCard")
            .child(userId)

        creditCardRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var limit = 0f
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val card = child.getValue(CreditCard::class.java)
                        card?.let { limit = it.limit }
                    }
                }
                onResult(limit)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(0f)
            }
        })
    }

    private fun startQrScanner() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        integrator.setPrompt("Aponte para o QR Code Pix")
        integrator.setBeepEnabled(false)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            handleQrResult(result.contents)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleQrResult(payload: String) {
        val parsed = PixPayloadParser.parse(payload)
        binding.editKeyPix.setText(parsed?.keyValue)
        binding.editValuePix.setText(parsed?.amount.toString())
        validateForm()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





