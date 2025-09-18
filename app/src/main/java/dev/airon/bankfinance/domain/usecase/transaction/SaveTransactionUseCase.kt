package dev.airon.bankfinance.domain.usecase.transaction

import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import dev.airon.bankfinance.domain.repository.transaction.TransactionRepository
import javax.inject.Inject
class SaveTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        repository.saveTransaction(transaction)
    }
}