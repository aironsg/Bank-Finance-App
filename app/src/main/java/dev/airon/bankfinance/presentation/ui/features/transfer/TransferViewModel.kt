package dev.airon.bankfinance.presentation.ui.features.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.domain.model.PixDetails
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.domain.model.TransactionPix
import dev.airon.bankfinance.domain.usecase.transaction.GetTransactionsUseCase
import dev.airon.bankfinance.domain.usecase.transaction.SendTransactionPixUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import perfetto.protos.UiState

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val sendTransactionPixUseCase: SendTransactionPixUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase
) : ViewModel() {

    // Função para enviar uma transação Pix
    fun sendPix(
        senderName: String,
        recipientName: String,
        recipientPix: String,
        amount: Float,
        senderId: String,
        recipientId: String
    ): LiveData<StateView<Boolean>> = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())

            val transaction = Transaction(
                id = "",
                operation = TransactionOperation.PIX,
                amount = amount,
                type = TransactionType.PIX,
                senderId = senderId,
                recipientId = recipientId
            )

            val pixDetails = PixDetails(
                sendName = senderName,
                recipientName = recipientName,
                recipientPix = recipientPix,
                fee = 0f
            )

            val transactionPix = TransactionPix(transaction, pixDetails)
            sendTransactionPixUseCase(transactionPix)

            emit(StateView.Success(true))
        } catch (e: Exception) {
            emit(StateView.Error(e.message))
        }
    }

    // Função para obter todas as transações do usuário
    fun getTransactions(): LiveData<StateView<List<Transaction>>> = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())
            val transactions = getTransactionsUseCase.invoke()
            emit(StateView.Success(transactions))
        } catch (ex: Exception) {
            emit(StateView.Error(ex.message))
        }
    }
}
