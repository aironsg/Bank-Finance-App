package dev.airon.bankfinance.domain.usecase.recharge

import dev.airon.bankfinance.domain.model.Recharge
import dev.airon.bankfinance.data.repository.recharge.RechargeRepositoryImpl
import javax.inject.Inject

class GetRechargeUseCase  @Inject constructor(
    private val rechargeRepositoryImpl: RechargeRepositoryImpl
){
    suspend operator fun invoke(id:String): Recharge {
        return rechargeRepositoryImpl.getRecharge(id)
    }
}