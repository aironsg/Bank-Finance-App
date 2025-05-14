package dev.airon.bankfinance.domain.auth

import dev.airon.bankfinance.data.repository.auth.AuthFirebaseDataSourceImpl
import jakarta.inject.Inject

class RecoverUsecase @Inject constructor(
    private val auth: AuthFirebaseDataSourceImpl
) {
    suspend operator fun invoke(
        email: String
    ) {
        return auth.recover(email)
    }
}