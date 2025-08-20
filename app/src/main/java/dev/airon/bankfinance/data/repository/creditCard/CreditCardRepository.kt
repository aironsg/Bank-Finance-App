package dev.airon.bankfinance.data.repository.creditCard

import dev.airon.bankfinance.data.model.CreditCard

interface CreditCardRepository {

    suspend fun getAllCreditCards(): List<String>

    suspend fun getCreditCardById(id: String): CreditCard?
    suspend fun addCreditCardToUser(creditCard: CreditCard)


    suspend fun initCreditCard(creditCard: CreditCard)

    suspend fun updateCreditCard(creditCard: String): Boolean

    suspend fun deleteCreditCard(id: String): Boolean
}