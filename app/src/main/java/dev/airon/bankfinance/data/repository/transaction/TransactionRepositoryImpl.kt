package dev.airon.bankfinance.data.repository.transaction

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.domain.model.TransactionPix
import dev.airon.bankfinance.domain.repository.transaction.TransactionRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TransactionRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : TransactionRepository {

    private val transactionReference = database.reference
        .child("transaction")
        .child(FirebaseHelper.getUserId())

    // Salva uma transação de deposito ou recarga de celular
    override suspend fun saveTransaction(transaction: Transaction) {
        return suspendCancellableCoroutine { continuation ->
            val newRef = transactionReference.child(transaction.id)

            val transactionMap = transaction.toMap().toMutableMap()
            transactionMap["date"] = ServerValue.TIMESTAMP

            newRef.setValue(transactionMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Unit)
                    } else {
                        task.exception?.let { continuation.resumeWithException(it) }
                    }
                }
        }
    }

    // Salva uma transação do tipo PIX
    suspend fun saveTransactionPix(transactionPix: TransactionPix) {
        return suspendCancellableCoroutine { continuation ->
            val newRef = transactionReference.child(transactionPix.transaction.id)


            val transactionMap = transactionPix.transaction.toMap().toMutableMap()
            transactionMap["pixDetails"] = mapOf(
                "sendName" to transactionPix.pixDetails.sendName,
                "recipientName" to transactionPix.pixDetails.recipientName,
                "recipientPix" to transactionPix.pixDetails.recipientPix,
                "fee" to transactionPix.pixDetails.fee
            )

            // Garante que a data será registrada pelo servidor
            transactionMap["date"] = ServerValue.TIMESTAMP

            newRef.setValue(transactionMap)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resume(Unit)
                    } else {
                        task.exception?.let { continuation.resumeWithException(it) }
                    }
                }
        }
    }

    override suspend fun getTransactions(): List<Transaction> {
        return suspendCancellableCoroutine { continuation ->
            transactionReference
                .orderByChild("date")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val transactions = mutableListOf<Transaction>()
                        for (ds in snapshot.children) {
                            val transaction = ds.getValue<Transaction>()
                            transaction?.let { transactions.add(it) }
                        }
                        continuation.resume(transactions)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWithException(error.toException())
                    }
                })
        }
    }

    override suspend fun getTransactionsById(userId: String): List<Transaction> {
        val snapshot = transactionReference
            .orderByChild("senderId")
            .equalTo(userId)
            .get().await()

        return snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
    }

    override suspend fun sendTransactionByPix(transactionPix: TransactionPix): Boolean {
        val transactionId = transactionPix.transaction.id.ifEmpty { transactionReference.push().key ?: "" }

        // Dados da transação
        val transactionMap = transactionPix.transaction.toMap().toMutableMap()
        transactionMap["pixDetails"] = mapOf(
            "sendName" to transactionPix.pixDetails.sendName,
            "recipientName" to transactionPix.pixDetails.recipientName,
            "recipientPix" to transactionPix.pixDetails.recipientPix,
            "fee" to transactionPix.pixDetails.fee
        )
        transactionMap["date"] = ServerValue.TIMESTAMP

        // Referências separadas
        val senderRef = database.reference.child("transaction").child(transactionPix.transaction.senderId).child(transactionId)
        val recipientRef = database.reference.child("transaction").child(transactionPix.transaction.recipientId).child(transactionId)

        // Salva para remetente e destinatário
        senderRef.setValue(transactionMap).await()
        recipientRef.setValue(transactionMap).await()

        return true
    }


}