package dev.airon.bankfinance.domain.profile

import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.data.repository.profile.ProfileRepositoryImpl
import javax.inject.Inject

class SaveProfileUseCase @Inject constructor(
    private val profileRepositoryImpl: ProfileRepositoryImpl
)  {

    suspend fun invoke(user: User) {
        return profileRepositoryImpl.saveProfile(user)
    }
}