package dev.airon.bankfinance.domain.repository.transaction

import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.domain.model.TransactionPix

interface TransactionRepository {
    suspend fun saveTransaction(transaction: Transaction)
    suspend fun getTransactions(): List<Transaction>
    suspend fun getTransactionsById(userId: String): List<Transaction>
    suspend fun sendTransactionByPix(transactionPix: TransactionPix): TransactionPix
    suspend fun getTransactionPixById(id: String): TransactionPix?
}
