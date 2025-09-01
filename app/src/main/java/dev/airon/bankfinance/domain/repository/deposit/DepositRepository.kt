package dev.airon.bankfinance.domain.repository.deposit

import dev.airon.bankfinance.domain.model.Deposit

interface DepositRepository {

    suspend fun saveDeposit(deposit: Deposit) : Deposit
    suspend fun getDeposit(id:String) : Deposit
}