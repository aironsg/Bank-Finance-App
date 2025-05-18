package dev.airon.bankfinance.data.repository.profile

import com.google.firebase.database.FirebaseDatabase
import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.util.FirebaseHelper
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class ProfileRepositoryImpl @Inject constructor(
    database: FirebaseDatabase,
) : ProfileRepository {

    private val profileReference = database.reference
        .child("profile")
        .child(FirebaseHelper.getUserId())


    override suspend fun saveProfile(
        user: User
    ) {
        return suspendCoroutine { continuation ->
            profileReference.setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Profile saved successfully
                        continuation.resumeWith(Result.success(Unit))
                    } else {
                        // Failed to save profile
                        task.exception?.let {
                            continuation.resumeWith(Result.failure(it))
                        }
                    }
                }
        }
    }
}