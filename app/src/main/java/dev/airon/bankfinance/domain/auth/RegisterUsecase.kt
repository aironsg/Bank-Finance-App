package dev.airon.bankfinance.domain.auth

import dev.airon.bankfinance.data.repository.auth.AuthFirebaseDataSourceImpl

class RegisterUsecase(
    private val auth: AuthFirebaseDataSourceImpl
) {

    suspend operator fun invoke(
        name: String,
        phone: String,
        email: String,
        password: String
    ) {
        return auth.register(name, phone, email, password)
    }
}