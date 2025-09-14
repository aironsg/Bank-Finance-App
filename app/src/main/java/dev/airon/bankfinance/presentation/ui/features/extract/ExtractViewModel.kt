package dev.airon.bankfinance.presentation.ui.features.extract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.domain.usecase.transaction.GetTransactionsUseCase
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


@HiltViewModel
class ExtractViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val transactionRepository: TransactionRepositoryImpl
) : ViewModel() {

    fun getTransactions() = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())
            val transactions = getTransactionsUseCase.invoke()
            emit(StateView.Success(transactions))
        } catch (ex: Exception) {
            emit(StateView.Error(ex.message))
        }
    }

    fun getTransactionPix(id: String) = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())
            val transactionPix = transactionRepository.getTransactionPixById(id)
            if (transactionPix != null) {
                emit(StateView.Success(transactionPix))
            } else {
                emit(StateView.Error("Transação PIX não encontrada"))
            }
        } catch (ex: Exception) {
            emit(StateView.Error(ex.message ?: "Erro desconhecido"))
        }
    }
}
