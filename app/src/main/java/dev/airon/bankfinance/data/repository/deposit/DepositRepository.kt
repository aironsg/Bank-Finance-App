package dev.airon.bankfinance.data.repository.deposit

import dev.airon.bankfinance.data.model.Deposit

interface DepositRepository {

    suspend fun saveDeposit(deposit: Deposit) : String
}