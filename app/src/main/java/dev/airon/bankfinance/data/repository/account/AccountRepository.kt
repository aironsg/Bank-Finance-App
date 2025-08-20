package dev.airon.bankfinance.data.repository.account

import dev.airon.bankfinance.data.model.Account

interface AccountRepository {

    suspend fun initAccount(account: Account)
    suspend fun getAccountById(id: String): Account?
    suspend fun updateAccount(account: Account): Boolean
    suspend fun deleteAccount(id: String): Boolean
}