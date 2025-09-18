package dev.airon.bankfinance.domain.usecase.creditCard

import dev.airon.bankfinance.domain.model.CreditCard

import dev.airon.bankfinance.domain.repository.creditCard.CreditCardRepository
import javax.inject.Inject

class AddCreditCardToUserUserCase @Inject constructor(
    private val creditCardRepositoryImpl: CreditCardRepository
) {
    suspend operator fun invoke(creditCard: CreditCard) {
        creditCardRepositoryImpl.addCreditCardToUser(creditCard)
    }
}