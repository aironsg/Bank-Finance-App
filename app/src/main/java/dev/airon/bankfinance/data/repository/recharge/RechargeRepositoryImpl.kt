package dev.airon.bankfinance.data.repository.recharge

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.data.model.Recharge
import dev.airon.bankfinance.util.FirebaseHelper
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine


class RechargeRepositoryImpl @Inject constructor(
    database: FirebaseDatabase
) : RechargeRepository {


    private val rechargeReferences = database.reference
        .child("recharge")
        .child(FirebaseHelper.getUserId())


    override suspend fun saveRecharge(recharge: Recharge): Recharge {
        return suspendCoroutine { continuation ->
            rechargeReferences
                .child(recharge.id)
                .setValue(recharge)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Recharge saved successfully
                        val dateReference = rechargeReferences
                            .child(recharge.id)
                            .child(recharge.phoneNumber)
                            .child("date")
                        dateReference.setValue(
                            ServerValue.TIMESTAMP
                        ).addOnCompleteListener { taskUpdate ->

                            if (taskUpdate.isSuccessful) {
                                continuation.resumeWith(Result.success(recharge))

                            } else {
                                // Failed to update date
                                taskUpdate.exception?.let {
                                    continuation.resumeWith(Result.failure(it))
                                }
                            }
                        }
                    } else {
                        // Failed to save Recharge
                        task.exception?.let {
                            continuation.resumeWith(Result.failure(it))
                        }
                    }
                }
        }
    }

    override suspend fun getRecharge(id:String): Recharge {
        return suspendCoroutine { continuation ->
            rechargeReferences.child(id).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val recharge = snapshot.getValue(Recharge::class.java)
                    recharge?.let {
                        continuation.resumeWith(Result.success(it))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().let {
                        continuation.resumeWith(Result.failure(it))
                    }
                }
            })
        }
    }


}