package dev.airon.bankfinance.presenter.features.deposit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.data.model.Deposit
import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.domain.deposit.SaveDepositUseCase
import dev.airon.bankfinance.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class DepositViewModel @Inject constructor(
    private val saveDepositUseCase: SaveDepositUseCase
) : ViewModel(){


    fun saveDeposit(deposit: Deposit) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            val depositId = saveDepositUseCase.invoke(deposit)
            emit(StateView.Success(depositId))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }
}