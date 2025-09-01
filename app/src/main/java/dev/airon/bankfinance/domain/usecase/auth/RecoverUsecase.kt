package dev.airon.bankfinance.domain.usecase.auth

import dev.airon.bankfinance.data.repository.auth.AuthFirebaseRepositoryImpl
import jakarta.inject.Inject

class RecoverUsecase @Inject constructor(
    private val auth: AuthFirebaseRepositoryImpl
) {
    suspend operator fun invoke(
        email: String
    ) {
        return auth.recover(email)
    }
}