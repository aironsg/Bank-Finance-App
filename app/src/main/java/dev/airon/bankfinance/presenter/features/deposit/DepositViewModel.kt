package dev.airon.bankfinance.presenter.features.deposit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.data.model.Deposit
import dev.airon.bankfinance.data.model.Transaction
import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.domain.Transaction.SaveTransactionUseCase
import dev.airon.bankfinance.domain.deposit.SaveDepositUseCase
import dev.airon.bankfinance.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class DepositViewModel @Inject constructor(
    private val saveDepositUseCase: SaveDepositUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase
) : ViewModel(){


    fun saveDeposit(deposit: Deposit) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            val result = saveDepositUseCase.invoke(deposit)
            emit(StateView.Success(result))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }

    fun saveTransaction(transaction: Transaction) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            saveTransactionUseCase.invoke(transaction)
            emit(StateView.Success(Unit))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }
}