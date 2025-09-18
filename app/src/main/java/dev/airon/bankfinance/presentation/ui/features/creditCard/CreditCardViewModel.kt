package dev.airon.bankfinance.presentation.ui.features.creditCard

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.core.util.InsufficientBalanceException
import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.domain.usecase.creditCard.AddCreditCardToUserUserCase
import dev.airon.bankfinance.domain.usecase.creditCard.GetCreditCardUseCase
import dev.airon.bankfinance.domain.usecase.creditCard.InitCreditCardUseCase
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.domain.repository.transaction.TransactionRepository
import dev.airon.bankfinance.domain.usecase.creditCard.PayCreditCardUseCase
import dev.airon.bankfinance.domain.usecase.transaction.SaveTransactionUseCase
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject



@HiltViewModel
class CreditCardViewModel @Inject constructor(
    private val getCreditCardUseCase: GetCreditCardUseCase,
    private val payCreditCardUseCase: PayCreditCardUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val initCreditCard: InitCreditCardUseCase
) : ViewModel() {

    fun initCreditCard(creditCard: CreditCard) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            initCreditCard.invoke(creditCard)
            emit(StateView.Success(null))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }

    fun getCreditCard(): LiveData<StateView<CreditCard>> = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())
            val card = getCreditCardUseCase.getCreditCard()
            emit(StateView.Success(card))
        } catch (e: Exception) {
            emit(StateView.Error(e.message))
        }
    }

    fun payCreditCard(cardId: String, amount: Float) = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())
            val result = payCreditCardUseCase.invoke(cardId, amount)
            emit(StateView.Success(result))
        } catch (e: Exception) {
            emit(StateView.Error(e.message))
        }
    }

    fun recordPaymentTransaction(transaction: Transaction) = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())
            saveTransactionUseCase.invoke(transaction)
            emit(StateView.Success(Unit))
        } catch (e: Exception) {
            emit(StateView.Error(e.message))
        }
    }
}
