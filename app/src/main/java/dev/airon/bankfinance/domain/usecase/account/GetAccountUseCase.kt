package dev.airon.bankfinance.domain.usecase.account

import dev.airon.bankfinance.domain.model.Account
import dev.airon.bankfinance.data.repository.account.AccountRepositoryImpl
import javax.inject.Inject

class GetAccountUseCase @Inject constructor(
    private val repository : AccountRepositoryImpl
) {
    suspend fun invoke() : Account{
        return repository.getAccount()
    }

}