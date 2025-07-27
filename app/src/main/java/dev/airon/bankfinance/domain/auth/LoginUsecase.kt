package dev.airon.bankfinance.domain.auth

import dev.airon.bankfinance.data.repository.auth.AuthFirebaseRepositoryImpl
import javax.inject.Inject


class LoginUsecase @Inject constructor(
    private val auth: AuthFirebaseRepositoryImpl
) {

    suspend operator fun invoke(
        email: String,
        password: String
    ) {
       return  auth.login(email, password)
    }
}