package dev.airon.bankfinance.presentation.ui.features.transfer



import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
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
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.databinding.FragmentTransferBinding
import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.presentation.ui.home.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransferFragment : Fragment(R.layout.fragment_transfer) {

    private val transferViewModel: TransferViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private var balance: Float = 0f
    private var selectedPaymentMethod: PaymentMethod? = null
    private var isBalanceVisible = false
    private var recipientName: String = ""
    private var recipientId: String? = null
    private var _binding: FragmentTransferBinding? = null
    private val binding get() = _binding!!

    private val qrCodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            handleQrResult(result.contents)
        } else {
            showBottomSheet(message = "Leitura cancelada")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTransferBinding.bind(view)

        initToolbar(binding.toolbar, isToolbarDefaultColor = true)
        observeTransactions()
        initRadioGroup()
        setupListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        binding.editValuePix.addMoneyMask()
        binding.editKeyPix.addEmailValidation()

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
        val isFormValid = !amountText.isNullOrBlank() &&
                !pixKey.isNullOrBlank() &&
                selectedPaymentMethod != null

        if (isFormValid) {
            binding.frameLayout.visibility = View.VISIBLE

            val amount = amountText?.replace("[^\\d,.]".toRegex(), "")
                ?.replace(",", ".")
                ?.toFloatOrNull() ?: 0f

            val method = when (selectedPaymentMethod) {
                PaymentMethod.CREDIT_CARD -> "Cartão de Crédito"
                PaymentMethod.BALANCE -> "Saldo Bancário"
                else -> ""
            }

            binding.textAmountTransaction.text = GetMask.getFormatedValue(amount)
            binding.textPhoneNumber.text = pixKey
            binding.textMethodPaymentValue.text = method
        } else {
            binding.frameLayout.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateTransfer() {
        val amountText = binding.editValuePix.text.toString()
        val pixKey = binding.editKeyPix.text.toString()

        if (amountText.isEmpty()) {
            showBottomSheet(message = "Digite um valor")
            return
        }
        if (pixKey.isEmpty()) {
            showBottomSheet(message = "Digite ou leia uma chave Pix")
            return
        }
        if (selectedPaymentMethod == null) {
            showBottomSheet(message = "Selecione um método de pagamento")
            return
        }

        val amount = amountText.replace("[^\\d,.]".toRegex(), "")
            .replace(",", ".").toFloatOrNull() ?: 0f

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confirmationTransfer(pixKey: String, amount: Float) {
        showBottomSheet(
            titleDialog = R.string.txt_information_data_transfer_pix_alert,
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
            val senderId = FirebaseHelper.getUserId()
            val senderName = FirebaseHelper.getUserName() ?: "Usuário"

            if (recipientId.isNullOrEmpty()) {
                showBottomSheet(message = "Destinatário inválido")
                return@launch
            }

            transferViewModel.sendPix(
                senderName = senderName,
                recipientName = recipientName,
                recipientPix = pixKey,
                amount = amount,
                senderId = senderId,
                recipientId = recipientId!!,
                paymentMethod = selectedPaymentMethod!!
            ).observe(viewLifecycleOwner) { stateView ->
                when (stateView) {
                    is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                    is StateView.Success -> {
                        val transactionId = stateView.data!!.transaction.id

                        lifecycleScope.launch {
                            // 🔹 busca no Firebase já com todos os dados
                            transferViewModel.getTransfer(transactionId)
                                .observe(viewLifecycleOwner) { transferState ->
                                    binding.progressBar.visibility = View.GONE
                                    when (transferState) {
                                        is StateView.Success -> {
                                            val transactionPix = transferState.data
                                            if (transactionPix != null) {
                                                val action =
                                                    TransferFragmentDirections
                                                        .actionTransferFragmentToTransferReceiptFragment(transactionPix)
                                                findNavController().navigate(action)
                                            } else {
                                                showBottomSheet(message = "Não foi possível carregar a transação.")
                                            }
                                        }
                                        is StateView.Error -> {
                                            showBottomSheet(message = transferState.message)
                                        }
                                        else -> Unit
                                    }
                                }
                        }
                    }

                    is StateView.Error -> {
                        binding.progressBar.visibility = View.GONE
                        showBottomSheet(message = stateView.message)
                    }
                }
            }
        }
    }

    private fun searchPixKey() {
        val pixKey = binding.editKeyPix.text.toString().replace("[()\\s-]".toRegex(), "")
        if (pixKey.isEmpty()) {
            showBottomSheet(message = "Digite a chave Pix")
            return
        }

        val userRef = FirebaseDatabase.getInstance().getReference("profile")
        userRef.orderByChild("email").equalTo(pixKey)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            recipientId = child.key
                            recipientName = child.child("name").getValue(String::class.java) ?: "Usuário"
                            binding.textPhoneNumber.text = pixKey
                            binding.textUserName.text = recipientName
                            validateForm()
                            showBottomSheet(
                                message = "Usuário $recipientName foi localizado com sucesso!"
                            )
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
                    val transactions = stateView.data ?: emptyList()
                    balance = calculateBalance(transactions)
                    updateBalanceUI()
                }
                is StateView.Error -> {
                    showBottomSheet(message = stateView.message)
                }
                else -> Unit
            }
        }
    }

    private fun calculateBalance(transactions: List<Transaction>): Float {
        var cashIn = 0f
        var cashOut = 0f
        transactions.forEach { t ->
            when (t.type) {
                TransactionType.CASH_IN, TransactionType.PIX_IN -> cashIn += t.amount
                TransactionType.CASH_OUT, TransactionType.PIX_OUT -> cashOut += t.amount
                else -> Unit
            }
        }
        return cashIn - cashOut
    }

    private fun updateBalanceUI() {
        if (isBalanceVisible) {
            binding.balanceValue.text =
                getString(R.string.text_formated_value, GetMask.getFormatedValue(balance))
            binding.balanceValue.visibility = View.VISIBLE
            binding.toggleVisibility.setImageResource(R.drawable.ic_no_visibility)
        } else {
            binding.balanceValue.visibility = View.GONE
            binding.toggleVisibility.setImageResource(R.drawable.ic_visibility)
        }
    }

    private fun startQrScanner() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Aponte a câmera para o QR Code Pix")
            setBeepEnabled(true)
            setOrientationLocked(false)
        }
        qrCodeLauncher.launch(options)
    }

    private fun handleQrResult(payload: String) {
        val parsed = PixPayloadParser.parse(payload)

        if (parsed == null) {
            showBottomSheet(message = "QR Code inválido ou mal formatado")
        } else {
            binding.editKeyPix.setText(parsed.keyValue)
            validateForm()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}





