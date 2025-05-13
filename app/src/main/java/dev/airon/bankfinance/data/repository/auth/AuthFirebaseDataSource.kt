package dev.airon.bankfinance.data.repository.auth

import com.google.firebase.auth.FirebaseUser

interface AuthFirebaseDataSource {
    suspend fun login(email: String, password: String)
    suspend fun register(name: String, phone: String, email: String, password: String) : FirebaseUser
    suspend fun recover(email: String)
}