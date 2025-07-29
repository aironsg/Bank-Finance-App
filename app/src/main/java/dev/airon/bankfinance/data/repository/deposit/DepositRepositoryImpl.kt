package dev.airon.bankfinance.data.repository.deposit

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import dev.airon.bankfinance.data.model.Deposit
import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.util.FirebaseHelper
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


}