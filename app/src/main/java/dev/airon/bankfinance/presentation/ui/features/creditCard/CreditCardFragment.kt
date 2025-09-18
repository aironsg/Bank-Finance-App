package dev.airon.bankfinance.presentation.ui.features.creditCard

import android.os.Build
import android.os.Bundle
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
import dev.airon.bankfinance.core.extensions.bottomSheetPasswordTransaction
import dev.airon.bankfinance.core.extensions.showBottomSheet
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.GetMask
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.databinding.FragmentCreditCardBinding
import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType

@AndroidEntryPoint
class CreditCardFragment : Fragment() {

    private var _binding: FragmentCreditCardBinding? = null
    private val binding get() = _binding!!
    private val creditCardViewModel: CreditCardViewModel by viewModels()

    private var currentCreditCard: CreditCard? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreditCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeCreditCardData()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeCreditCardData() {
        binding.textErrorMessage.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        creditCardViewModel.getCreditCard().observe(viewLifecycleOwner) { state ->
            when (state) {
                is StateView.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is StateView.Success -> {
                    binding.progressBar.visibility = View.GONE
                    state.data?.let { card ->
                        currentCreditCard = card
                        updateUI(card)
                        setupListeners()
                    } ?: run {
                        handleFetchError("Dados do cartão não encontrados")
                    }
                }
                is StateView.Error -> {
                    binding.progressBar.visibility = View.GONE
                    handleFetchError(state.message ?: "Erro ao carregar cartão")
                }
            }
        }
    }

    private fun handleFetchError(message: String) {
        currentCreditCard = null
        binding.textErrorMessage.visibility = View.VISIBLE
        binding.textErrorMessage.text = message
        clearUI()
        binding.buttonPaymentCreditCard.isEnabled = false
    }

    private fun updateUI(creditCard: CreditCard) {
        binding.cardBalanceFront.creditCardNumber.text = creditCard.number
        binding.cardBalanceFront.creditCardValidDate.text = creditCard.validDate.ifEmpty { "--/--" }
        binding.textAvailableBalanceValue.text = GetMask.getFormatedValue(creditCard.balance)
        binding.textAvailableLimitValue.text = GetMask.getFormatedValue(creditCard.limit)
        binding.cardBalanceBack.textSecurityCodeNumber.text = creditCard.securityCode.ifEmpty { "----" }
        binding.cardBalanceFront.textUserName.text = creditCard.officialUser.ifEmpty { "Titular não encontrado" }
        binding.cardBalanceBack.textBankBranchNumber.text = creditCard.account?.branch?.ifEmpty { "----" } ?: "----"
        binding.cardBalanceBack.textAccountNumber.text = creditCard.account?.accountNumber?.ifEmpty { "----" } ?: "----"
        binding.textErrorMessage.visibility = View.GONE
        binding.buttonPaymentCreditCard.isEnabled = creditCard.balance > 0f
    }

    private fun clearUI() {
        binding.cardBalanceFront.creditCardNumber.text = "---- ---- ---- ----"
        binding.cardBalanceFront.creditCardValidDate.text = "--/--"
        binding.textAvailableBalanceValue.text = GetMask.getFormatedValue(0f)
        binding.textAvailableLimitValue.text = GetMask.getFormatedValue(0f)
        binding.cardBalanceBack.textSecurityCodeNumber.text = "----"
        binding.cardBalanceFront.textUserName.text = "------------------"
        binding.cardBalanceBack.textBankBranchNumber.text = "----"
        binding.cardBalanceBack.textAccountNumber.text = "--------"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupListeners() {
        binding.buttonPaymentCreditCard.setOnClickListener {
            currentCreditCard?.let { card ->
                val billAmount = card.balance
                if (billAmount <= 0f) {
                    showBottomSheet(message = "Nenhuma fatura pendente para este cartão.")
                    return@setOnClickListener
                }

                showBottomSheet(
                    titleDialog = R.string.txt_information_data_payment,
                    message = "Sua fatura é de R$ ${GetMask.getFormatedValue(billAmount)}. Deseja pagar agora?",
                    titleButton = R.string.txt_button_bottomSheet_confirm,
                    onClick = {
                        bottomSheetPasswordTransaction(
                            message = "Informe sua senha para confirmar o pagamento",
                            titleButton = R.string.txt_button_bottomSheet_confirm
                        ) {
                            creditCardViewModel.payCreditCard(card.id, billAmount).observe(viewLifecycleOwner) { state ->
                                when (state) {
                                    is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                                    is StateView.Success -> {
                                        binding.progressBar.visibility = View.GONE
                                        if (state.data == true) {
                                            // cria transaction e salva via ViewModel (SaveTransactionUseCase)
                                            val transaction = Transaction(
                                                id = "", // repo irá gerar um id se vazio
                                                operation = TransactionOperation.CARD_PAYMENT,
                                                date = 0L, // será substituído pelo ServerValue.TIMESTAMP no repo
                                                amount = billAmount,
                                                type = TransactionType.CASH_OUT,
                                                senderId = FirebaseHelper.getUserId(),
                                                recipientId = FirebaseHelper.getUserId(),
                                                relatedCardId = card.id
                                            )

                                            creditCardViewModel.recordPaymentTransaction(transaction).observe(viewLifecycleOwner) { txState ->
                                                when (txState) {
                                                    is StateView.Loading -> binding.progressBar.visibility = View.VISIBLE
                                                    is StateView.Success -> {
                                                        binding.progressBar.visibility = View.GONE
                                                        // recarrega ui do cartão
                                                        observeCreditCardData()
                                                        val action = CreditCardFragmentDirections.actionCreditCardFragmentToCreditCardReceiptFragment(card.id, billAmount)
                                                        findNavController().navigate(action)
                                                    }
                                                    is StateView.Error -> {
                                                        binding.progressBar.visibility = View.GONE
                                                        showBottomSheet(message = txState.message ?: "Erro ao salvar transação")
                                                    }
                                                }
                                            }
                                        } else {
                                            showBottomSheet(message = "Falha ao efetuar pagamento")
                                        }
                                    }
                                    is StateView.Error -> {
                                        binding.progressBar.visibility = View.GONE
                                        showBottomSheet(message = state.message ?: "Erro ao processar pagamento")
                                    }
                                }
                            }
                        }
                    }
                )
            } ?: showBottomSheet(message = "Cartão não carregado. Aguarde e tente novamente.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        currentCreditCard = null
    }
}


