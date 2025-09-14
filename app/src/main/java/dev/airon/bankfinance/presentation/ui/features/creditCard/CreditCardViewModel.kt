package dev.airon.bankfinance.presentation.ui.features.creditCard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.core.util.InsufficientBalanceException
import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.domain.usecase.creditCard.AddCreditCardToUserUserCase
import dev.airon.bankfinance.domain.usecase.creditCard.GetCreditCardUseCase
import dev.airon.bankfinance.domain.usecase.creditCard.InitCreditCardUseCase
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.domain.usecase.creditCard.PayCreditCardUseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class CreditCardViewModel @Inject constructor(
    private val initCreditCard: InitCreditCardUseCase,
    private val addCreditCardToUserUserCase: AddCreditCardToUserUserCase,
    private val getCreditCardUseCase: GetCreditCardUseCase,
    private val payCreditCardUseCase: PayCreditCardUseCase
) : ViewModel() {

    fun initCreditCard(creditCard: CreditCard) = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())
            initCreditCard.invoke(creditCard)
            emit(StateView.Success(Unit))
        } catch (ex: Exception) {
            emit(StateView.Error(ex.message))
        }
    }

    fun payCreditCard(cardId: String, billAmount: Float) = liveData(Dispatchers.IO) {
        emit(StateView.Loading())
        try {
            val paymentSuccessful = payCreditCardUseCase.invoke(cardId, billAmount)
            emit(StateView.Success(paymentSuccessful))
        } catch (e: InsufficientBalanceException) {
            // Trata a exceção de saldo insuficiente especificamente
            emit(StateView.Error(e.message)) // A mensagem já está formatada no repositório
        } catch (e: Exception) {
            // Captura outras exceções gerais do processo de pagamento
            emit(StateView.Error(e.message ?: "Erro desconhecido ao pagar a fatura."))
        }
    }



    fun getCreditCard() = liveData(Dispatchers.IO) {

        try {
            emit(StateView.Loading())
            val creditCard = getCreditCardUseCase.getCreditCard()
            emit(StateView.Success(creditCard))
        } catch (ex: Exception) {
            emit(StateView.Error(ex.message))
        }
    }
}
