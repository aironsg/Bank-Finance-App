package dev.airon.bankfinance.presenter.features.creditCard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.data.model.CreditCard
import dev.airon.bankfinance.domain.creditCard.AddCreditCardToUserUserCase
import dev.airon.bankfinance.domain.creditCard.InitCreditCardUseCase
import dev.airon.bankfinance.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class CreditCardViewModel @Inject constructor(
    private val initCreditCard: InitCreditCardUseCase,
    private val addCreditCardToUserUserCase: AddCreditCardToUserUserCase
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

    fun addCreditCardToUser(creditCard: CreditCard) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            addCreditCardToUserUserCase.invoke(creditCard)
            emit(StateView.Success(null))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }


}