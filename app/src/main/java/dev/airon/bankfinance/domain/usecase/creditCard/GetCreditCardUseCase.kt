package dev.airon.bankfinance.domain.usecase.creditCard

import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.data.repository.creditCard.CreditCardRepositoryImpl
import javax.inject.Inject

class GetCreditCardUseCase @Inject constructor(
    private val repository: CreditCardRepositoryImpl
) {

    suspend fun getCreditCard(): CreditCard{
        return repository.getCreditCard()
    }
}