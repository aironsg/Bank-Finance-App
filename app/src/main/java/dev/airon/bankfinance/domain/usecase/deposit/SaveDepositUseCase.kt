package dev.airon.bankfinance.domain.usecase.deposit

import dev.airon.bankfinance.domain.model.Deposit
import dev.airon.bankfinance.data.repository.deposit.DepositRepositoryImpl
import javax.inject.Inject

class SaveDepositUseCase @Inject constructor(
    private val depositRepositoryImpl: DepositRepositoryImpl
) {

    suspend operator fun invoke(deposit: Deposit): Deposit {
        return depositRepositoryImpl.saveDeposit(deposit)
    }
}