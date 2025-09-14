package dev.airon.bankfinance.domain.repository.creditCard

import dev.airon.bankfinance.domain.model.CreditCard

interface CreditCardRepository {



    suspend fun getCreditCard(): CreditCard
    suspend fun addCreditCardToUser(creditCard: CreditCard)


    suspend fun initCreditCard(creditCard: CreditCard)

    suspend fun updateCreditCard(creditCard: String): Boolean

    suspend fun deleteCreditCard(id: String): Boolean

    suspend fun payCreditCard(id: String, amount: Float): Boolean
}