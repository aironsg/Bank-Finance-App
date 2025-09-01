package dev.airon.bankfinance.data.repository.deposit

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.domain.model.Deposit
import dev.airon.bankfinance.core.util.FirebaseHelper
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine


class DepositRepositoryImpl @Inject constructor(
    database: FirebaseDatabase
) : DepositRepository {


    private val depositReference = database.reference
        .child("deposit")
        .child(FirebaseHelper.getUserId())


    override suspend fun saveDeposit(deposit: Deposit): Deposit {
        return suspendCoroutine { continuation ->
            depositReference
                .child(deposit.id)
                .setValue(deposit)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Deposit saved successfully
                        val dateReference = depositReference
                            .child(deposit.id)
                            .child("date")
                        dateReference.setValue(
                            ServerValue.TIMESTAMP
                        ).addOnCompleteListener { taskUpdate ->

                            if (taskUpdate.isSuccessful) {
                                continuation.resumeWith(Result.success(deposit))

                            } else {
                                // Failed to update date
                                taskUpdate.exception?.let {
                                    continuation.resumeWith(Result.failure(it))
                                }
                            }
                        }
                    } else {
                        // Failed to save Deposit
                        task.exception?.let {
                            continuation.resumeWith(Result.failure(it))
                        }
                    }
                }
        }
    }

    override suspend fun getDeposit(id:String): Deposit {
        return suspendCoroutine { continuation ->
            depositReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val deposit = snapshot.getValue(Deposit::class.java)
                    deposit?.let {
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