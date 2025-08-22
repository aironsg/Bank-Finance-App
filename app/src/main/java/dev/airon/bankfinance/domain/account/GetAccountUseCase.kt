package dev.airon.bankfinance.domain.account

import dev.airon.bankfinance.data.model.Account
import dev.airon.bankfinance.data.repository.account.AccountRepositoryImpl
import javax.inject.Inject

class GetAccountUseCase @Inject constructor(
    private val repository : AccountRepositoryImpl
) {
    suspend fun invoke() : Account{
        return repository.getAccount()
    }

}