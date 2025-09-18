package dev.airon.bankfinance.domain.usecase.creditCard

import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.data.repository.creditcard.CreditCardRepositoryImpl
import dev.airon.bankfinance.domain.repository.creditCard.CreditCardRepository

import jakarta.inject.Inject

class PayCreditCardUseCase @Inject constructor(
    private val repository: CreditCardRepository
) {
    suspend operator fun invoke(cardId: String, amount: Float): Boolean {
        return repository.payCreditCard(cardId, amount)
    }
}