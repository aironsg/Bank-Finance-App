package dev.airon.bankfinance.domain.usecase.account


import dev.airon.bankfinance.domain.model.Account
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