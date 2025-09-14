package dev.airon.bankfinance.presentation.ui.features.recharge

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.core.extensions.addMoneyMask
import dev.airon.bankfinance.core.extensions.addPhoneMask
import dev.airon.bankfinance.core.extensions.bottomSheetPasswordTransaction
import dev.airon.bankfinance.core.extensions.formatPhoneNumber
import dev.airon.bankfinance.core.extensions.hideKeyboard
import dev.airon.bankfinance.core.extensions.initToolbar
import dev.airon.bankfinance.core.extensions.showBottomSheet
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.GetMask
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.data.enum.PaymentMethod
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.databinding.FragmentRechargeBinding
import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.domain.model.Recharge
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.presentation.ui.home.HomeViewModel
import dev.airon.bankfinance.presentation.ui.home.TransactionsAdapter


@AndroidEntryPoint
class RechargeFragment : Fragment() {

    private var _binding: FragmentRechargeBinding? = null
    private val binding get() = _binding!!

    private var selectedPaymentMethod: PaymentMethod? = null
    private val rechargeViewModel: RechargeViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var transactionAdapter: TransactionsAdapter
    private var balance: Float = 0f
    private lateinit var typeOperation: TransactionOperation
    private lateinit var transactionType: TransactionType
    private var isBalanceVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRechargeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar, isToolbarDefaultColor = true)
        observeTransactions()
        getCreditCardNumber()
        getTransactions()
        initRadioGroup()
        initListener()
        showMaskMoney()
        showMaskPhone()
    }

    private fun showMaskMoney() = binding.editAmount.addMoneyMask()
    private fun showMaskPhone() = binding.editPhone.addPhoneMask()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListener() {
        binding.btnRecharge.setOnClickListener { validateAndProcessRecharge() }

        // Toggle saldo visível/oculto
        binding.toggleVisibility.setOnClickListener {
            isBalanceVisible = !isBalanceVisible
            updateBalanceUI()
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateAndProcessRecharge() { // Renomeado
        val amountText = binding.editAmount.text.toString()
            .replace("[R$\\s.]".toRegex(), "")
            .replace(",", ".")
        val phone = binding.editPhone.text.toString()
            .replace("[()\\s-]".toRegex(), "")
            .replace("#", "")
            .replace("-", "")
            .replace(" ", "")

        // ... (validações de amountText, phone, selectedPaymentMethod como antes) ...
        if (amountText.isEmpty() || phone.isEmpty() || selectedPaymentMethod == null) {
            // Mostrar Toasts apropriados
            if (amountText.isEmpty()) Toast.makeText(requireContext(), "Digite um valor", Toast.LENGTH_SHORT).show()
            else if (phone.isEmpty()) Toast.makeText(requireContext(), "Digite um telefone", Toast.LENGTH_SHORT).show()
            else Toast.makeText(requireContext(), "Selecione um método de pagamento", Toast.LENGTH_SHORT).show()
            return
        }

        hideKeyboard()

        // O ID será gerado no construtor de Recharge.
        // date e hour serão definidos pelo RechargeRepositoryImpl.
        val rechargeToProcess = Recharge(
            amount = amountText.toFloat(),
            phoneNumber = phone,
            typeRecharge = selectedPaymentMethod!!
        )

        // As variáveis typeOperation e transactionType não são mais necessárias no Fragment,
        // pois o SaveRechargeUseCase determinará o TransactionType com base no PaymentMethod.
        // E TransactionOperation será sempre RECHARGE para este fluxo.

        when (selectedPaymentMethod) {
            PaymentMethod.BALANCE -> {
                // A verificação de saldo da UI ainda é útil para feedback rápido
                if (rechargeToProcess.amount > balance) {
                    showBottomSheet(message = "Saldo em conta insuficiente para recarga.")
                } else {
                    confirmAndExecuteRecharge(rechargeToProcess)
                }
            }
            PaymentMethod.CREDIT_CARD -> {
                fetchCreditCardLimit { limit ->
                    if (rechargeToProcess.amount > limit) {
                        showBottomSheet(message = "Limite do cartão insuficiente para recarga.")
                    } else {
                        // IMPORTANTE: A atualização do cartão acontece ANTES de chamar o UseCase.
                        // Idealmente, o UseCase também lidaria com isso para atomicidade,
                        // mas para esta refatoração, manteremos assim.
                        updateBalanceCreditCard(rechargeToProcess.amount) // Isso é assíncrono
                        confirmAndExecuteRecharge(rechargeToProcess)
                    }
                }
            }
            else -> Toast.makeText(requireContext(), "Método de pagamento inválido", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confirmAndExecuteRecharge(recharge: Recharge) { // Renomeado
        showBottomSheet(
            titleDialog = R.string.txt_information_data_recharge_alert,
            message = "Valor: R$ ${GetMask.getFormatedValue(recharge.amount)}\n" +
                    "Telefone: ${formatPhoneNumber(recharge.phoneNumber)}\n" +
                    "Método de Pagamento: ${PaymentMethod.getOperation(recharge.typeRecharge)}\n" +
                    "Deseja confirmar a recarga?",
            titleButton = R.string.txt_button_bottomSheet_confirm,
            onClick = {
                bottomSheetPasswordTransaction(
                    message = "Informe sua senha para confirmar a recarga",
                    titleButton = R.string.txt_button_bottomSheet_confirm
                ) {
                    // Chama o método do ViewModel que agora invoca o UseCase refatorado
                    executeRechargeProcessing(recharge)
                }
            }
        )
    }

    private fun executeRechargeProcessing(recharge: Recharge) { // Novo método
        rechargeViewModel.processNewRecharge(recharge).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    // Sucesso! Recarga salva, wallet atualizada (se aplicável), transação registrada.
                    // Navega para o recibo.
                    stateView.data?.let { savedRecharge ->
                        val action = RechargeFragmentDirections
                            .actionRechargeFragmentToRechargeReceiptFragment(savedRecharge.id)
                        findNavController().navigate(action)
                    }
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(
                        message = getString( // Se FirebaseHelper.validError retorna Int (ID de string)
                            FirebaseHelper.validError(stateView.message ?: "")
                        )
                        // ou apenas: message = stateView.message // Se validError não for necessário aqui
                    )
                }
            }
        }
    }


    private fun initRadioGroup() {
        binding.radioGroupOptions.setOnCheckedChangeListener { _, checkedId ->
            selectedPaymentMethod = when (checkedId) {
                R.id.radio_credit_card -> PaymentMethod.CREDIT_CARD
                R.id.radio_balance -> PaymentMethod.BALANCE
                else -> null
            }
        }
    }

    /** 🔹 Observa a lista de transações para atualizar saldo */
    private fun observeTransactions() {
        homeViewModel.getTransactions().observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> Unit
                is StateView.Success -> {
                    balance = calculateBalance(stateView.data ?: emptyList())
                    updateBalanceUI()
                }
                is StateView.Error -> showBottomSheet(message = stateView.message)
            }
        }
    }

    private fun getCreditCardNumber() {
        val userId = FirebaseHelper.getUserId()
        val creditCardRef = FirebaseDatabase.getInstance()
            .getReference("creditCard")
            .child(userId)

        creditCardRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val creditCard = child.getValue(CreditCard::class.java)
                        creditCard?.number?.let {
                            binding.creditCardNumber.text = maskCreditCardNumber(it)
                        }
                    }
                } else {
                    binding.creditCardNumber.text = "Número do cartão não encontrado"
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateBalanceCreditCard(valueRecharge: Float) {
        val userId = FirebaseHelper.getUserId()
        val creditCardRef = FirebaseDatabase.getInstance()
            .getReference("creditCard")
            .child(userId)

        creditCardRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (child in snapshot.children) {
                        val creditCard = child.getValue(CreditCard::class.java)
                        val cardKey = child.key

                        if (creditCard != null && cardKey != null) {
                            val currentLimit = creditCard.limit
                            val currentBalanceDue = creditCard.balance

                            val newLimit = currentLimit - valueRecharge
                            val newBalanceDue = currentBalanceDue + valueRecharge

                            creditCardRef.child(cardKey).updateChildren(
                                mapOf(
                                    "limit" to newLimit,
                                    "balance" to newBalanceDue
                                )
                            ).addOnSuccessListener {
                                // OK
                            }.addOnFailureListener { e ->
                                showBottomSheet(message = "Erro ao atualizar cartão: ${e.message}")
                            }
                        }
                    }
                } else {
                    showBottomSheet(message = "Nenhum cartão cadastrado encontrado")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showBottomSheet(message = "Erro ao acessar dados do cartão: ${error.message}")
            }
        })
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

    private fun maskCreditCardNumber(number: String): String {
        return if (number.length >= 8) {
            val firstFour = number.take(4)
            val lastFour = number.takeLast(4)
            "$firstFour **** **** $lastFour"
        } else number
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
            binding.balanceValue.visibility = View.VISIBLE
            binding.toggleVisibility.setImageResource(R.drawable.ic_no_visibility)
        } else {
            binding.balanceValue.visibility = View.GONE
            binding.toggleVisibility.setImageResource(R.drawable.ic_visibility)
        }
    }

    private fun getTransactions() {
        rechargeViewModel.getTransactions().observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> {
                    binding.progressBar.visibility = View.GONE
                    balance = calculateBalance(stateView.data ?: emptyList())
                    updateBalanceUI()
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showBottomSheet(message = stateView.message)
                }
            }
        }
    }







    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}



