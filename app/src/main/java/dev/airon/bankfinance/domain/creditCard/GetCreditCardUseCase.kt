package dev.airon.bankfinance.domain.creditCard

import dev.airon.bankfinance.data.model.CreditCard
import dev.airon.bankfinance.data.repository.creditCard.CreditCardRepositoryImpl
import javax.inject.Inject

class GetCreditCardUseCase @Inject constructor(
    private val repository: CreditCardRepositoryImpl
) {

    suspend fun getCreditCard(): CreditCard{
        return repository.getCreditCard()
    }
}