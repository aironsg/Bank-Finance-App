package dev.airon.bankfinance.domain.auth

import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.data.repository.auth.AuthFirebaseRepositoryImpl
import jakarta.inject.Inject

class RegisterUseCase @Inject constructor(
    private val auth: AuthFirebaseRepositoryImpl
) {

    suspend operator fun invoke(
        name: String,
        accountNumber: String,
        cpf: String,
        rg: String,
        phone: String,
        email: String,
        password: String,
        passwordTransaction: String
    ): User {
        return auth.register(
            name,
            accountNumber,
            cpf,
            rg,
            phone,
            email,
            password,
            passwordTransaction
        )
    }
}