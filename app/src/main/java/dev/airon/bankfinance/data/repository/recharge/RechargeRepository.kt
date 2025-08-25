package dev.airon.bankfinance.data.repository.recharge

import dev.airon.bankfinance.data.model.Deposit
import dev.airon.bankfinance.data.model.Recharge

interface RechargeRepository {

    suspend fun saveRecharge(recharge: Recharge) : Recharge
    suspend fun getRecharge(id:String) : Recharge

    suspend fun getPasswordTransaction() : String
}