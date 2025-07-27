package dev.airon.bankfinance.domain.auth

import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.data.repository.auth.AuthFirebaseRepositoryImpl
import jakarta.inject.Inject

class RegisterUsecase @Inject constructor(
    private val auth: AuthFirebaseRepositoryImpl
) {

    suspend operator fun invoke(name: String, phone: String, email: String, password: String): User {
        return auth.register(name, phone, email, password)
    }
}