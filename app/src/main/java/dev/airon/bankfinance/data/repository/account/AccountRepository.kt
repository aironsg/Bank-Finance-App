package dev.airon.bankfinance.data.repository.account

import dev.airon.bankfinance.data.model.Account

interface AccountRepository {

    suspend fun getAllAccounts(): List<Account>

    suspend fun getAccountById(id: String): Account?

    suspend fun addAccount(account: String)

    suspend fun updateAccount(account: String): Boolean

    suspend fun deleteAccount(id: String): Boolean
}