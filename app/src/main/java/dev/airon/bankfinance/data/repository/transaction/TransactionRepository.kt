package dev.airon.bankfinance.data.repository.transaction

import dev.airon.bankfinance.data.model.Transaction

interface TransactionRepository {

    suspend fun saveTransaction(transaction: Transaction)
    suspend fun getTransactions(): List<Transaction>
}