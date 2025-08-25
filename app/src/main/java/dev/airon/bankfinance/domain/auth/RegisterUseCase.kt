package dev.airon.bankfinance.domain.auth

import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.data.repository.auth.AuthFirebaseRepositoryImpl
import jakarta.inject.Inject

class RegisterUseCase @Inject constructor(
    private val auth: AuthFirebaseRepositoryImpl
) {

    suspend operator fun invoke(
        name: String,
        cpf: String,
        rg: String,
        phone: String,
        email: String,
        password: String,
        passwordTransaction: String,
        passwordSalt: String
    ): User {
        return auth.register(
            name,
            cpf,
            rg,
            phone,
            email,
            password,
            passwordTransaction,
            passwordSalt
        )
    }
}