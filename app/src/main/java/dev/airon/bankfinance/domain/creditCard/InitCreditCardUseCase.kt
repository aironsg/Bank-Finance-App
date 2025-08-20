package dev.airon.bankfinance.domain.creditCard

import dev.airon.bankfinance.data.model.CreditCard
import dev.airon.bankfinance.data.repository.creditCard.CreditCardRepositoryImpl
import javax.inject.Inject

class InitCreditCardUseCase @Inject constructor(
    private val creditCardRepository: CreditCardRepositoryImpl
) {

    suspend operator fun invoke(creditCard: CreditCard) {
        creditCardRepository.initCreditCard(creditCard)
    }
}