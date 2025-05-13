package dev.airon.bankfinance.domain.auth

import dev.airon.bankfinance.data.repository.auth.AuthFirebaseDataSourceImpl

class RecoverUsecase(
    private val auth: AuthFirebaseDataSourceImpl
) {
    suspend operator fun invoke(
        email: String
    ) {
        return auth.recover(email)
    }
}