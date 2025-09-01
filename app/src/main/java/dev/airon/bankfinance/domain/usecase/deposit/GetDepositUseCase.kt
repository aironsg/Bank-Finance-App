package dev.airon.bankfinance.domain.usecase.deposit

import dev.airon.bankfinance.domain.model.Deposit
import dev.airon.bankfinance.data.repository.deposit.DepositRepositoryImpl
import javax.inject.Inject

class GetDepositUseCase  @Inject constructor(
    private val depositRepositoryImpl: DepositRepositoryImpl
){
    suspend operator fun invoke(id:String): Deposit {
        return depositRepositoryImpl.getDeposit(id)
    }
}