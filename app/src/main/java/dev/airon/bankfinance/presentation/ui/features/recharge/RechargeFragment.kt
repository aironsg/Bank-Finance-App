package dev.airon.bankfinance.presentation.ui.features.recharge

import android.os.Build
import android.os.Bundle
import android.util.Log
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

@AndroidEntryPoint
class RechargeFragment : Fragment() {

    private var _binding: FragmentRechargeBinding? = null
    private val binding get() = _binding!!

    private var selectedPaymentMethod: PaymentMethod? = null
    private val rechargeViewModel: RechargeViewModel by viewModels()
    private val homeViewModel: HomeViewModel by viewModels()

    private var balance: Float = 0f
    private var isBalanceVisible = false
    private val logTag = "RechargeFragment"

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
        observeWalletBalance() // 🔹 Agora pega do servidor
        getCreditCardNumber()
        initRadioGroup()
        initListener()
        showMaskMoney()
        showMaskPhone()
    }

    private fun showMaskMoney() = binding.editAmount.addMoneyMask()
    private fun showMaskPhone() = binding.editPhone.addPhoneMask()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListener() {
        binding.btnRecharge.setOnClickListener { validateRecharge() }
        binding.toggleVisibility.setOnClickListener {
            isBalanceVisible = !isBalanceVisible
            updateBalanceUI()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
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
        val amountValue = amountText.toFloatOrNull()
        if (amountValue == null || amountValue <= 0f) {
            Toast.makeText(requireContext(), "Valor inválido", Toast.LENGTH_SHORT).show()
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

        val recharge = Recharge(
            amount = amountValue,
            phoneNumber = phone,
            typeRecharge = selectedPaymentMethod!!
        )

        when (selectedPaymentMethod) {
            PaymentMethod.BALANCE -> {
                if (recharge.amount > balance) {
                    showBottomSheet(message = "Saldo insuficiente para recarga")
                } else {
                    confirmationRecharge(recharge)
                }
            }
            PaymentMethod.CREDIT_CARD -> {
                fetchCreditCardLimit { availableLimit ->
                    if (recharge.amount > availableLimit) {
                        showBottomSheet(message = "Limite do cartão insuficiente para recarga")
                    } else {
                        confirmationRecharge(recharge)
                    }
                }
            }
            else -> {
                Toast.makeText(requireContext(), "Método de pagamento inválido", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun confirmationRecharge(recharge: Recharge) {
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
                    doSaveRecharge(recharge)
                }
            })
    }

    private fun doSaveRecharge(recharge: Recharge) {
        binding.btnRecharge.isEnabled = false
        rechargeViewModel.saveRecharge(recharge).observe(viewLifecycleOwner) { stateView ->
            when (stateView) {
                is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                is StateView.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRecharge.isEnabled = true
                    val saved = stateView.data ?: recharge
                    val action = RechargeFragmentDirections
                        .actionRechargeFragmentToRechargeReceiptFragment(saved.id)
                    findNavController().navigate(action)
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRecharge.isEnabled = true
                    showBottomSheet(message = stateView.message)
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

    /** 🔹 Agora buscamos o saldo diretamente da Wallet no servidor */
    private fun observeWalletBalance() {
        homeViewModel.refreshWallet().observe(viewLifecycleOwner) { state ->
            when (state) {
                is StateView.Success -> {
                    val wallet = state.data
                    wallet?.let { balance = it.balance }
                    updateBalanceUI()
                }
                is StateView.Error -> {
                    showBottomSheet(message = state.message)
                }
                else -> Unit
            }
        }
    }

    private fun getCreditCardNumber() {
        val userId = FirebaseHelper.getUserId()
        if (userId.isBlank()) {
            binding.creditCardNumber.text = "Usuário não logado"
            return
        }
        val creditCardRef = FirebaseDatabase.getInstance()
            .getReference("creditCard")
            .child(userId)

        creditCardRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val creditCard = snapshot.getValue(CreditCard::class.java)
                    if (creditCard != null) {
                        binding.creditCardNumber.text = GetMask.maskCreditCardNumber(creditCard.number)
                    } else {
                        binding.creditCardNumber.text = "Dados do cartão inválidos"
                    }
                } else {
                    binding.creditCardNumber.text = "Cartão não encontrado"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                binding.creditCardNumber.text = "Erro ao buscar cartão"
            }
        })
    }

    private fun fetchCreditCardLimit(onResult: (Float) -> Unit) {
        val userId = FirebaseHelper.getUserId()
        if (userId.isBlank()) {
            onResult(0f)
            return
        }
        val creditCardRef = FirebaseDatabase.getInstance()
            .getReference("creditCard")
            .child(userId)

        creditCardRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var available = 0f
                if (snapshot.exists()) {
                    val creditCard = snapshot.getValue(CreditCard::class.java)
                    if (creditCard != null) {
                        available = creditCard.limit - creditCard.balance
                    }
                }
                onResult(available)
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(0f)
            }
        })
    }

    private fun updateBalanceUI() {
        if (isBalanceVisible) {
            binding.balanceValue.text = getString(
                R.string.text_formated_value,
                GetMask.getFormatedValue(balance)
            )
            binding.balanceValue.visibility = View.VISIBLE
            binding.toggleVisibility.setImageResource(R.drawable.ic_no_visibility)
        } else {
            binding.balanceValue.visibility = View.GONE
            binding.toggleVisibility.setImageResource(R.drawable.ic_visibility)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}






