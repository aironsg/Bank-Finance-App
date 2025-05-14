package dev.airon.bankfinance.domain.auth

import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.data.repository.auth.AuthFirebaseDataSourceImpl
import jakarta.inject.Inject

class RegisterUsecase @Inject constructor(
    private val auth: AuthFirebaseDataSourceImpl
) {

    suspend operator fun invoke(user: User): User {
        return auth.register(user)
    }
}