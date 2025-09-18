package dev.airon.bankfinance.domain.repository.creditCard

import dev.airon.bankfinance.domain.model.CreditCard

interface CreditCardRepository {
    suspend fun getCreditCard(): CreditCard
    suspend fun initCreditCard(creditCard: CreditCard)
    suspend fun payCreditCard(cardId: String, amount: Float): Boolean
    suspend fun addCreditCardToUser(creditCard: CreditCard)
    suspend fun deleteCreditCard(id: String): Boolean
}
