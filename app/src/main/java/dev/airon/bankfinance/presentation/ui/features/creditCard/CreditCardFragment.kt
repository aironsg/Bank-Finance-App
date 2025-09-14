package dev.airon.bankfinance.presentation.ui.features.creditCard

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import dev.airon.bankfinance.core.extensions.bottomSheetPasswordTransaction
import dev.airon.bankfinance.core.extensions.showBottomSheet
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.GetMask
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import dev.airon.bankfinance.databinding.FragmentCreditCardBinding
import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.domain.model.Transaction
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreditCardFragment : Fragment() {

    private var _binding: FragmentCreditCardBinding? = null
    private val binding get() = _binding!!
    private val creditCardViewModel: CreditCardViewModel by viewModels()

    private var currentCreditCard: CreditCard? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreditCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getCreditCardAndSetupListeners() // Chamada única para buscar dados e configurar listeners
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCreditCardAndSetupListeners() {
        binding.textErrorMessage.visibility = View.GONE // Esconde mensagem de erro inicialmente
        // binding.progressBar.visibility = View.VISIBLE // Opcional: Mostrar ProgressBar

        val userId = FirebaseHelper.getUserId()
        val creditCardRef = FirebaseDatabase.getInstance()
            .getReference("creditCard")
            .child(userId)

        creditCardRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // binding.progressBar.visibility = View.GONE // Opcional: Esconder ProgressBar
                if (snapshot.exists()) {

                    val cardDataSnapshot = snapshot.children.firstOrNull() // Pega o primeiro filho, se houver
                    val creditCard = cardDataSnapshot?.getValue(CreditCard::class.java)

                    if (creditCard != null) {
                        currentCreditCard = creditCard // << IMPORTANTE: Atualiza currentCreditCard
                        updateUI(creditCard) // Usa a função de UI separada
                        setupListeners()     // << IMPORTANTE: Configura listeners APÓS ter os dados
                        binding.buttonPaymentCreditCard.isEnabled = true
                    } else {
                        handleFetchError("Dados do cartão não encontrados ou formato inválido.")
                    }
                } else {
                    handleFetchError("Nenhum cartão de crédito encontrado para este usuário.")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // binding.progressBar.visibility = View.GONE // Opcional: Esconder ProgressBar
                handleFetchError("Erro ao buscar dados do cartão: ${error.message}")
            }
        })
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
        binding.cardBalanceFront.creditCardValidDate.text = creditCard.validDate ?: "--/--"
        binding.textAvailableBalanceValue.text = GetMask.getFormatedValue(creditCard.balance ?: 0.0)
        binding.textAvailableLimitValue.text = GetMask.getFormatedValue(creditCard.limit ?: 0.0)
        binding.cardBalanceBack.textSecurityCodeNumber.text = creditCard.securityCode ?: "----"
        binding.cardBalanceFront.textUserName.text = creditCard.account?.name ?: "Titular não encontrado"
        binding.cardBalanceBack.textBankBranchNumber.text = creditCard.account?.branch ?: "----"
        binding.cardBalanceBack.textAccountNumber.text = creditCard.account?.accountNumber ?: "----"
        binding.textErrorMessage.visibility = View.GONE // Garante que a mensagem de erro seja escondida se a UI for atualizada com sucesso
    }

    private fun clearUI() {
        binding.cardBalanceFront.creditCardNumber.text = "---- ---- ---- ----"
        binding.cardBalanceFront.creditCardValidDate.text = "--/--"
        binding.textAvailableBalanceValue.text = GetMask.getFormatedValue(0.0)
        binding.textAvailableLimitValue.text = GetMask.getFormatedValue(0.0)
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

                if (billAmount > 0f) {
                    showBottomSheet(
                        titleDialog = R.string.txt_information_data_payment,
                        message = "Sua fatura é de R$ ${GetMask.getFormatedValue(billAmount)}. Deseja pagar agora?",
                        titleButton = R.string.txt_button_bottomSheet_confirm,
                        onClick = {
                            bottomSheetPasswordTransaction(
                                message = "Informe sua senha para confirmar o pagamento",
                                titleButton = R.string.txt_button_bottomSheet_confirm
                            ) {
                                // 🔹 Segue a lógica do fluxo de recarga
                                creditCardViewModel.payCreditCard(card.id, billAmount)
                                    .observe(viewLifecycleOwner) { state ->
                                        handlePaymentState(state, billAmount, card.id)
                                    }
                            }
                        }
                    )
                } else {
                    showBottomSheet(message = "Nenhuma fatura pendente para este cartão.")
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun handlePaymentState(state: StateView<Boolean>, amountPaid: Float, cardId: String) {
        when (state) {
            is StateView.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
            }
            is StateView.Success -> {
                binding.progressBar.visibility = View.GONE
                if (state.data == true) {
                    val transaction = Transaction(
                        id = FirebaseHelper.getGeneratedId(),
                        operation = TransactionOperation.CARD_PAYMENT,
                        date = System.currentTimeMillis(),
                        amount = amountPaid,
                        type = TransactionType.CASH_OUT,
                        senderId = FirebaseHelper.getUserId(),
                        recipientId = FirebaseHelper.getUserId(),
                        relatedCardId = cardId
                    )

                    lifecycleScope.launch {
                        val repository = TransactionRepositoryImpl(FirebaseDatabase.getInstance())
                        repository.saveTransaction(transaction)

                        val action = CreditCardFragmentDirections
                            .actionCreditCardFragmentToCreditCardReceiptFragment(cardId, amountPaid)
                        findNavController().navigate(action)
                    }
                } else {
                    showBottomSheet(message = "Não foi possível concluir o pagamento.")
                }
            }
            is StateView.Error -> {
                binding.progressBar.visibility = View.GONE
                showBottomSheet(message = state.message ?: "Erro ao processar pagamento")
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        currentCreditCard = null
    }
}
