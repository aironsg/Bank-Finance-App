package dev.airon.bankfinance.domain.usecase.recharge

import dev.airon.bankfinance.domain.model.Recharge
import dev.airon.bankfinance.data.repository.recharge.RechargeRepositoryImpl
import javax.inject.Inject

class SaveRechargeUseCase @Inject constructor(
    private val rechargeRepositoryImpl: RechargeRepositoryImpl
) {

    suspend operator fun invoke(recharge: Recharge): Recharge {
        return rechargeRepositoryImpl.saveRecharge(recharge)
    }
}