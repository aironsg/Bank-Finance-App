package dev.airon.bankfinance.domain.auth

import dev.airon.bankfinance.data.repository.auth.AuthFirebaseDataSourceImpl

class LoginUsecase(
    private val auth: AuthFirebaseDataSourceImpl
) {

    suspend operator fun invoke(
        email: String,
        password: String
    ) {
       return  auth.login(email, password)
    }
}