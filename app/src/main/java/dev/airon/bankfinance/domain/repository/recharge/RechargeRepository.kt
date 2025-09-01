package dev.airon.bankfinance.domain.repository.recharge

import dev.airon.bankfinance.domain.model.Recharge

interface RechargeRepository {

    suspend fun saveRecharge(recharge: Recharge) : Recharge
    suspend fun getRecharge(id:String) : Recharge

    suspend fun getPasswordTransaction() : String
}