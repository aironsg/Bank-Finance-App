package dev.airon.bankfinance.domain.repository.profile

import dev.airon.bankfinance.domain.model.User

interface ProfileRepository {

    suspend fun saveProfile(user: User)
    suspend fun getProfile() : User
}