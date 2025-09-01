package dev.airon.bankfinance.domain.usecase.creditCard

import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.data.repository.creditCard.CreditCardRepositoryImpl
import javax.inject.Inject

class AddCreditCardToUserUserCase @Inject constructor(
    private val creditCardRepositoryImpl: CreditCardRepositoryImpl
) {
    suspend operator fun invoke(creditCard: CreditCard) {
        creditCardRepositoryImpl.addCreditCardToUser(creditCard)
    }
}