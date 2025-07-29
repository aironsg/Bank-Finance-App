package dev.airon.bankfinance.domain.Transaction

import dev.airon.bankfinance.data.model.Transaction
import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import javax.inject.Inject

class SaveTransactionUseCase @Inject constructor(
    private val transactionRepositoryImpl: TransactionRepositoryImpl
) {

    suspend operator fun invoke(transaction: Transaction) {
        transactionRepositoryImpl.saveTransaction(transaction)
    }
}