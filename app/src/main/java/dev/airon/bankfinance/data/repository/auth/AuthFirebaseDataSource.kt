package dev.airon.bankfinance.data.repository.auth

interface AuthFirebaseDataSource {
    suspend fun login(email: String, password: String)
    suspend fun register(name: String, phone: String, email: String, password: String)
    suspend fun recover(email: String)
}