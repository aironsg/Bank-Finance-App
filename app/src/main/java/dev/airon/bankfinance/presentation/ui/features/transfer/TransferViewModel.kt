package dev.airon.bankfinance.presentation.ui.features.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.StateView
import dev.airon.bankfinance.data.enum.PaymentMethod
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.domain.model.PixDetails
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.domain.model.TransactionPix
import dev.airon.bankfinance.domain.usecase.transaction.GetTransactionsUseCase
import dev.airon.bankfinance.domain.usecase.transaction.SaveTransactionUseCase
import dev.airon.bankfinance.domain.usecase.transaction.SendTransactionPixUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@HiltViewModel
class  TransferViewModel @Inject constructor(
    private val sendTransactionPixUseCase: SendTransactionPixUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase
) : ViewModel() {

    fun sendPix(
        senderName: String,
        recipientName: String,
        recipientPix: String,
        amount: Float,
        senderId: String,
        recipientId: String,
        paymentMethod: PaymentMethod
    ): LiveData<StateView<TransactionPix>> = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())

            val transaction = Transaction(
                id = "", // será gerado no repo
                operation = TransactionOperation.PIX,
                amount = amount,
                type = TransactionType.PIX_OUT,
                senderId = senderId,
                recipientId = recipientId
            )

            val pixDetails = PixDetails(
                sendName = senderName,
                recipientName = recipientName,
                recipientPix = recipientPix,
                fee = 0.0
            )

            val transactionPix = TransactionPix(transaction, pixDetails, paymentMethod)

            // Agora o useCase retorna o TransactionPix completo (com id + date)
            val saved = sendTransactionPixUseCase(transactionPix)

            emit(StateView.Success(saved))
        } catch (e: Exception) {
            emit(StateView.Error(e.message))
        }
    }


    fun getTransfer(id: String): LiveData<StateView<TransactionPix>> = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())
            val transactionPix = sendTransactionPixUseCase.getTransactionPixById(id)
            if (transactionPix != null) {
                emit(StateView.Success(transactionPix))
            } else {
                emit(StateView.Error("Transação não encontrada"))
            }
        } catch (e: Exception) {
            emit(StateView.Error(e.message ?: "Erro desconhecido"))
        }
    }

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
