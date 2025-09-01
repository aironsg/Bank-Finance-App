package dev.airon.bankfinance.domain.repository.account

import dev.airon.bankfinance.domain.model.Account

interface AccountRepository {

    suspend fun initAccount(account: Account)
    suspend fun getAccount(): Account
    suspend fun updateAccount(account: Account): Boolean
    suspend fun deleteAccount(id: String): Boolean
}