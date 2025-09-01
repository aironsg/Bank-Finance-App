package dev.airon.bankfinance.presentation.ui.features.deposit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.domain.usecase.deposit.GetDepositUseCase
import dev.airon.bankfinance.core.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class DepositReceiptViewModel @Inject constructor(
    private val getDepositUseCase: GetDepositUseCase
) : ViewModel(){

    fun getDeposit(id:String) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            val result = getDepositUseCase.invoke(id)
            emit(StateView.Success(result))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }
}