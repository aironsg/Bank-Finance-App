package dev.airon.bankfinance.domain.usecase.creditCard

import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.domain.repository.creditCard.CreditCardRepository
import javax.inject.Inject

class InitCreditCardUseCase @Inject constructor(
    private val creditCardRepository: CreditCardRepository
) {

    suspend operator fun invoke(creditCard: CreditCard) {
        creditCardRepository.initCreditCard(creditCard)
    }
}