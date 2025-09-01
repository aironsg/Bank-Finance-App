package dev.airon.bankfinance.domain.repository.transaction

import dev.airon.bankfinance.domain.model.Transaction

interface TransactionRepository {

    suspend fun saveTransaction(transaction: Transaction)
    suspend fun getTransactions(): List<Transaction>
}