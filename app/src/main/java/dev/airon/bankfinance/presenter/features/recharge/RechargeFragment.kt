package dev.airon.bankfinance.presenter.features.recharge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.airon.bankfinance.R
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.data.model.Recharge
import dev.airon.bankfinance.data.model.Transaction
import dev.airon.bankfinance.databinding.FragmentRechargeBinding
import dev.airon.bankfinance.presenter.features.creditCard.CreditCardViewModel
import dev.airon.bankfinance.presenter.home.HomeViewModel
import dev.airon.bankfinance.presenter.home.TransactionsAdapter
import dev.airon.bankfinance.util.FirebaseHelper
import dev.airon.bankfinance.util.GetMask
import dev.airon.bankfinance.util.StateView
import dev.airon.bankfinance.util.addMoneyMask
import dev.airon.bankfinance.util.addPhoneMask
import dev.airon.bankfinance.util.hideKeyboard
import dev.airon.bankfinance.util.initToolbar
import dev.airon.bankfinance.util.showBottomSheet
import androidx.core.view.isGone
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.data.enum.PaymentMethod
import dev.airon.bankfinance.data.model.CreditCard

@AndroidEntryPoint
class RechargeFragment : Fragment() {

    private var _binding: FragmentRechargeBinding? = null
    private val binding get() = _binding!!

    private var selectedPaymentMethod: PaymentMethod? = null
    private val rechargeViewModel: RechargeViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private lateinit var transactionAdapter: TransactionsAdapter
    private var balance: Float = 0f
    private var isBalanceVisible = false // controle de visibilidade

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRechargeBinding.inflate(inflater, container, false)
        return binding.root
    }

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

    private fun initListener() {
        binding.btnRecharge.setOnClickListener { validateRecharge() }

        // Toggle saldo visível/oculto
        binding.toggleVisibility.setOnClickListener {
            isBalanceVisible = !isBalanceVisible
            updateBalanceUI()
        }
    }

    private fun validateRecharge() {
        val amountText = binding.editAmount.text.toString()
            .replace("[R$\\s.]".toRegex(), "")
            .replace(",", ".")
        val phone = binding.editPhone.text.toString()
            .replace("[()\\s-]".toRegex(), "")
            .replace("#", "")
            .replace("-", "")
            .replace(" ", "")

        if (amountText.isEmpty()) {
            Toast.makeText(requireContext(), "Digite um valor", Toast.LENGTH_SHORT).show()
            return
        }
        if (phone.isEmpty()) {
            Toast.makeText(requireContext(), "Digite um telefone", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedPaymentMethod == null) {
            Toast.makeText(requireContext(), "Selecione um método de pagamento", Toast.LENGTH_SHORT).show()
            return
        }

        hideKeyboard()
        val recharge = Recharge(amount = amountText.toFloat(), phoneNumber = phone)

        when (selectedPaymentMethod) {
            PaymentMethod.BALANCE -> {
                if (recharge.amount > balance) {
                    showBottomSheet(message = "Saldo insuficiente para recarga")
                } else {
                    saveRecharge(recharge)
                }
            }

            PaymentMethod.CREDIT_CARD -> {
                fetchCreditCardLimit { limit ->
                    if (recharge.amount > limit) {
                        showBottomSheet(message = "Limite do cartão insuficiente para recarga")
                    } else {
                        saveRecharge(recharge)
                    }
                }
            }

            else -> Toast.makeText(requireContext(), "Método de pagamento inválido", Toast.LENGTH_SHORT).show()
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
                    transactionAdapter.submitList(stateView.data?.reversed()?.take(6))
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
            binding.balanceValue.text = getString(R.string.text_formated_value, GetMask.getFormatedValue(balance))
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

    private fun saveRecharge(recharge: Recharge) {
        rechargeViewModel.saveRecharge(recharge).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    saveTransaction(recharge)
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(message = getString(FirebaseHelper.validError(stateView.message ?: "")))
                }
            }
        }
    }

    private fun saveTransaction(recharge: Recharge) {
        val transaction = Transaction(
            id = recharge.id,
            operation = TransactionOperation.RECHARGE,
            date = recharge.date,
            amount = recharge.amount,
            type = TransactionType.CASH_OUT
        )

        rechargeViewModel.saveTransaction(transaction).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    val action = RechargeFragmentDirections.actionRechargeFragmentToRechargeReceiptFragment(recharge.id)
                    findNavController().navigate(action)
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    showBottomSheet(message = getString(FirebaseHelper.validError(stateView.message ?: "")))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
