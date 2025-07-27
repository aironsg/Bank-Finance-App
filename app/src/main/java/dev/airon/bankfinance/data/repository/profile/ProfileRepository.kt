package dev.airon.bankfinance.data.repository.profile

import dev.airon.bankfinance.data.model.User

interface ProfileRepository {

    suspend fun saveProfile(user: User)
}