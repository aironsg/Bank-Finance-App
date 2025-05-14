package dev.airon.bankfinance.data.repository.auth

import com.google.firebase.auth.FirebaseUser
import dev.airon.bankfinance.data.model.User

interface AuthFirebaseDataSource {
    suspend fun login(email: String, password: String)
    suspend fun register(user: User) : User
    suspend fun recover(email: String)
}