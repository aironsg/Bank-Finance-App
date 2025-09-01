package dev.airon.bankfinance.data.repository.profile

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.domain.model.User
import dev.airon.bankfinance.core.util.FirebaseHelper
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

    override suspend fun getProfile(): User {
        return suspendCoroutine { continuation ->
            profileReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        continuation.resumeWith(Result.success(it))

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWith(Result.failure(error.toException()))
                }

            })

        }
    }
}
