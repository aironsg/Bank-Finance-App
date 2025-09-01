package dev.airon.bankfinance.domain.usecase.transaction
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val transactionRepositoryImpl: TransactionRepositoryImpl
) {

    suspend operator fun invoke() : List<Transaction> {
       return  transactionRepositoryImpl.getTransactions()
    }
}