package dev.airon.bankfinance.domain.profile

import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.data.repository.profile.ProfileDataSourceImpl
import javax.inject.Inject

class SaveProfileUsecase @Inject constructor(
    private val profileRepositoryImpl: ProfileDataSourceImpl
)  {

    suspend fun invoke(user: User) {
        return profileRepositoryImpl.saveProfile(user)
    }
}