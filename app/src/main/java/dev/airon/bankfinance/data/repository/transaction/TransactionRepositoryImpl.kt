package dev.airon.bankfinance.data.repository.transaction

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import dev.airon.bankfinance.data.model.Transaction
import dev.airon.bankfinance.util.FirebaseHelper
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class TransactionRepositoryImpl @Inject constructor(
    database: FirebaseDatabase
) : TransactionRepository{

    private val transactionReference = database.reference
        .child("transaction")
        .child(FirebaseHelper.getUserId())

    override suspend fun saveTransaction(transaction: Transaction) {
        return suspendCoroutine { continuation ->
            transactionReference
                .child(transaction.id)
                .setValue(transaction)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val dateReference = transactionReference
                            .child(transaction.id)
                            .child("date")
                        dateReference.setValue(
                            ServerValue.TIMESTAMP)
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