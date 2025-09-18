package dev.airon.bankfinance.presentation.ui.features.recharge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.core.util.InsufficientBalanceException
import dev.airon.bankfinance.domain.model.Recharge
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.domain.usecase.transaction.GetTransactionsUseCase
import dev.airon.bankfinance.domain.usecase.transaction.SaveTransactionUseCase
import dev.airon.bankfinance.domain.usecase.recharge.GetPasswordTransactionUseCase
import dev.airon.bankfinance.domain.usecase.recharge.SaveRechargeUseCase
import dev.airon.bankfinance.core.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class RechargeViewModel @Inject constructor(
    private val saveRechargeUseCase: SaveRechargeUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getPasswordTransactionUseCase: GetPasswordTransactionUseCase
) : ViewModel(){


    fun saveRecharge(recharge: Recharge) = liveData(Dispatchers.IO) { // Renomeado para clareza
        try {
            emit(StateView.Loading())
            // Teste: "processNewRecharge_callsSaveRechargeUseCase_withCorrectRecharge"
            val savedRechargeWithDate = saveRechargeUseCase.invoke(recharge)
            // Teste: "processNewRecharge_whenUseCaseSucceeds_emitsSuccessWithRecharge"
            emit(StateView.Success(savedRechargeWithDate))
        } catch (e: InsufficientBalanceException) {
            // Teste: "processNewRecharge_whenUseCaseFailsWithInsufficientBalance_emitsError"
            emit(StateView.Error(e.message)) // Mensagem já formatada da exceção
        } catch (ex: Exception) {
            // Teste: "processNewRecharge_whenUseCaseFailsWithOtherError_emitsError"
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