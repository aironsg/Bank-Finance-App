package dev.airon.bankfinance.domain.usecase.transaction

import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import dev.airon.bankfinance.domain.model.TransactionPix
import jakarta.inject.Inject

class SendTransactionPixUseCase @Inject constructor(
    private val repository: TransactionRepositoryImpl
) {
    suspend operator fun invoke(transactionPix: TransactionPix): Boolean {
        return repository.sendTransactionByPix(transactionPix)
    }

    suspend fun getTransactionPixById(id: String): TransactionPix? {
        return repository.getTransactionPixById(id)
    }
}