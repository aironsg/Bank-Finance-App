package dev.airon.bankfinance.domain.auth

import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.data.repository.auth.AuthFirebaseDataSourceImpl
import jakarta.inject.Inject

class RegisterUsecase @Inject constructor(
    private val auth: AuthFirebaseDataSourceImpl
) {

    suspend operator fun invoke(name: String, phone: String, email: String, password: String): User {
        return auth.register(name, phone, email, password)
    }
}