package dev.airon.bankfinance.domain.usecase.profile

import dev.airon.bankfinance.domain.model.User
import dev.airon.bankfinance.data.repository.profile.ProfileRepositoryImpl
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val profileRepositoryImpl: ProfileRepositoryImpl
)  {

    suspend fun invoke() : User {
        return profileRepositoryImpl.getProfile()
    }
}