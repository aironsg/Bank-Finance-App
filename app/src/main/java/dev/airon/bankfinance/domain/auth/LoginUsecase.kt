package dev.airon.bankfinance.domain.auth

import dev.airon.bankfinance.data.repository.auth.AuthFirebaseDataSourceImpl
import javax.inject.Inject

class LoginUsecase @Inject constructor(
    private val auth: AuthFirebaseDataSourceImpl
) {

    suspend operator fun invoke(
        email: String,
        password: String
    ) {
       return  auth.login(email, password)
    }
}