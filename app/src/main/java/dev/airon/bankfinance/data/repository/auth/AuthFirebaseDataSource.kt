package dev.airon.bankfinance.data.repository.auth

import com.google.firebase.auth.FirebaseUser
import dev.airon.bankfinance.data.model.User

interface AuthFirebaseDataSource {
    suspend fun login(email: String, password: String)
    suspend fun register(name: String, phone: String, email: String, password: String) : User
    suspend fun recover(email: String)
}