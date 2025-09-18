package dev.airon.bankfinance.domain.usecase.creditCard

import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.domain.repository.creditCard.CreditCardRepository
import javax.inject.Inject

class GetCreditCardUseCase @Inject constructor(
    private val repository: CreditCardRepository
) {

    suspend fun getCreditCard(): CreditCard{
        return repository.getCreditCard()
    }
}