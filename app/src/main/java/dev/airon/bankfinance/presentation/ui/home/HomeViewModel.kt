package dev.airon.bankfinance.presentation.ui.home



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.core.util.StateView.Error
import dev.airon.bankfinance.core.util.StateView.Loading
import dev.airon.bankfinance.core.util.StateView.Success
import dev.airon.bankfinance.domain.usecase.transaction.GetTransactionPixUseCase
import dev.airon.bankfinance.domain.usecase.transaction.GetTransactionsUseCase
import dev.airon.bankfinance.domain.usecase.wallet.GetWalletUseCase

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getTransactionPixUseCase: GetTransactionPixUseCase // <-- novo caso de uso
) : ViewModel() {

    fun getWallet() = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())
            val wallet = getWalletUseCase.invoke()
            emit(Success(wallet))
        } catch (ex: Exception) {
            emit(Error(ex.message))
        }
    }

    fun getTransactions() = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())
            val transactions = getTransactionsUseCase.invoke()
            emit(Success(transactions))
        } catch (ex: Exception) {
            emit(Error(ex.message))
        }
    }

    /**
     * Busca o TransactionPix completo — usado pela Home/Extract para abrir recibos de PIX
     */
    fun getTransactionPix(transactionId: String) = liveData(Dispatchers.IO) {
        try {
            emit(Loading())
            val transactionPix = getTransactionPixUseCase.invoke(transactionId) // suspenso
            emit(Success(transactionPix))
        } catch (ex: Exception) {
            emit(Error(ex.message))
        }
    }
}
