package dev.airon.bankfinance.domain.usecase.recharge

import dev.airon.bankfinance.data.repository.recharge.RechargeRepositoryImpl
import javax.inject.Inject

class GetPasswordTransactionUseCase @Inject constructor(
    private val repositoryImpl: RechargeRepositoryImpl
) {

    suspend operator fun invoke(): String{
        return repositoryImpl.getPasswordTransaction()
    }
}