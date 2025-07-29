package dev.airon.bankfinance.domain.Transaction
import dev.airon.bankfinance.data.model.Transaction
import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionRepositoryImpl: TransactionRepositoryImpl
) {

    suspend operator fun invoke() : List<Transaction> {
       return  transactionRepositoryImpl.getTransactions()
    }
}