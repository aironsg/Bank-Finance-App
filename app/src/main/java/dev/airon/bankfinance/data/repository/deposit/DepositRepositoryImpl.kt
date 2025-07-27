package dev.airon.bankfinance.data.repository.deposit

import com.google.firebase.database.FirebaseDatabase
import dev.airon.bankfinance.data.model.Deposit
import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.util.FirebaseHelper
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine


class DepositRepositoryImpl @Inject constructor(
    database : FirebaseDatabase
) : DepositRepository {


    private val depositReference = database.reference
        .child("deposit")
        .child(FirebaseHelper.getUserId())




    override suspend fun saveDeposit(deposit: Deposit): String {
        return suspendCoroutine { continuation ->
            depositReference
                .child(deposit.id)
                .setValue(deposit)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Profile saved successfully
                        continuation.resumeWith(Result.success(deposit.id))
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