package dev.airon.bankfinance.presenter.features.recharge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.data.model.Deposit
import dev.airon.bankfinance.data.model.Recharge
import dev.airon.bankfinance.data.model.Transaction
import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.domain.Transaction.GetTransactionsUseCase
import dev.airon.bankfinance.domain.Transaction.SaveTransactionUseCase
import dev.airon.bankfinance.domain.deposit.SaveDepositUseCase
import dev.airon.bankfinance.domain.recharge.GetPasswordTransactionUseCase
import dev.airon.bankfinance.domain.recharge.SaveRechargeUseCase
import dev.airon.bankfinance.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class RechargeViewModel @Inject constructor(
    private val saveRechargeUseCase: SaveRechargeUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getPasswordTransactionUseCase: GetPasswordTransactionUseCase
) : ViewModel(){


    fun saveRecharge(recharge: Recharge) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            val result = saveRechargeUseCase.invoke(recharge)
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

    fun getTransactions() = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            val transactions = getTransactionsUseCase.invoke()
            emit(StateView.Success(transactions))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }

    fun getPasswordTransaction() = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            val passwordTransaction = getPasswordTransactionUseCase.invoke()
            emit(StateView.Success(passwordTransaction))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }
}