package dev.airon.bankfinance.domain.account


import dev.airon.bankfinance.data.model.Account
import dev.airon.bankfinance.data.repository.account.AccountRepositoryImpl
import javax.inject.Inject

class InitAccountUseCase @Inject constructor(
    private val accountRepository: AccountRepositoryImpl
) {

    suspend operator fun invoke(
        account: Account
) {
        accountRepository.initAccount(account)
    }
}