package dev.airon.bankfinance.data.repository.transaction

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.data.enum.PaymentMethod
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.domain.model.PixDetails
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.domain.model.TransactionPix
import dev.airon.bankfinance.domain.repository.transaction.TransactionRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
class TransactionRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : TransactionRepository {

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

                        dateReference.setValue(ServerValue.TIMESTAMP)
                            .addOnCompleteListener {
                                continuation.resumeWith(Result.success(Unit))
                            }
                    } else {
                        task.exception?.let {
                            continuation.resumeWith(Result.failure(it))
                        }
                    }
                }
        }
    }

    override suspend fun getTransactions(): List<Transaction> {
        return suspendCoroutine { continuation ->
            transactionReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val transactions = mutableListOf<Transaction>()
                    for (ds in snapshot.children) {
                        try {
                            val map = ds.value as? Map<*, *>
                            val transaction = map?.let { parseTransaction(it) }
                            transaction?.let { transactions.add(it) }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    continuation.resumeWith(Result.success(transactions))
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWith(Result.failure(error.toException()))
                }
            })
        }
    }

    private fun parseTransaction(map: Map<*, *>): Transaction {
        return Transaction(
            id = map["id"] as? String ?: "",
            operation = (map["operation"] as? String)?.let { TransactionOperation.valueOf(it) },
            date = (map["date"] as? Long) ?: 0L,
            amount = when (val raw = map["amount"]) {
                is Long -> raw.toFloat()
                is Double -> raw.toFloat()
                is String -> raw.toFloatOrNull() ?: 0f
                else -> 0f
            },
            type = (map["type"] as? String)?.let { TransactionType.valueOf(it) },
            senderId = map["senderId"] as? String ?: "",
            recipientId = map["recipientId"] as? String ?: ""
        )
    }

    override suspend fun getTransactionsById(userId: String): List<Transaction> {
        val snapshot = transactionReference
            .orderByChild("senderId")
            .equalTo(userId)
            .get().await()

        return snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
    }

    override suspend fun sendTransactionByPix(transactionPix: TransactionPix): TransactionPix {
        // Gera id se vazio
        val transactionId =
            transactionPix.transaction.id.ifEmpty { transactionReference.push().key ?: "" }

        // Criar as duas versões: remetente (PIX_OUT) e destinatário (PIX_IN)
        val senderTransaction = transactionPix.transaction.copy(
            id = transactionId,
            type = TransactionType.PIX_OUT
        )
        val recipientTransaction = transactionPix.transaction.copy(
            id = transactionId,
            type = TransactionType.PIX_IN
        )

        // Mapa comum dos detalhes PIX
        val commonPixDetails = mapOf(
            "sendName" to transactionPix.pixDetails.sendName,
            "recipientName" to transactionPix.pixDetails.recipientName,
            "recipientPix" to transactionPix.pixDetails.recipientPix,
            "fee" to transactionPix.pixDetails.fee
        )

        // Monta os mapas a salvar (inserimos ServerValue.TIMESTAMP para "date")
        val senderMap = senderTransaction.toMap().toMutableMap().apply {
            put("pixDetails", commonPixDetails)
            put("paymentMethod", transactionPix.paymentMethod.name)
            put("date", ServerValue.TIMESTAMP)
        }

        val recipientMap = recipientTransaction.toMap().toMutableMap().apply {
            put("pixDetails", commonPixDetails)
            put("paymentMethod", transactionPix.paymentMethod.name)
            put("date", ServerValue.TIMESTAMP)
        }

        // Referências onde salvar
        val senderRef = database.reference
            .child("transaction")
            .child(senderTransaction.senderId)
            .child(transactionId)

        val recipientRef = database.reference
            .child("transaction")
            .child(recipientTransaction.recipientId)
            .child(transactionId)

        // Salva ambos (await para garantir persistência)
        senderRef.setValue(senderMap).await()
        recipientRef.setValue(recipientMap).await()

        // Agora buscamos de volta a versão salva no remetente para garantir que "date" foi preenchido pelo servidor
        val snapshot = senderRef.get().await()

        // Tenta desserializar para Transaction; se falhar, tenta usar parseTransaction(map)
        val savedTransaction = snapshot.getValue(Transaction::class.java)
            ?: run {
                val map = snapshot.value as? Map<*, *> ?: emptyMap<Any, Any>()
                parseTransaction(map) // parseTransaction já existe em sua classe (reaproveite)
            }

        // Recupera pixDetails do snapshot (robusto contra tipos Long/Double)
        val pixDetailsMap = snapshot.child("pixDetails").value as? Map<String, Any> ?: emptyMap()
        val feeValue = when (val fee = pixDetailsMap["fee"]) {
            is Double -> fee
            is Long -> fee.toDouble()
            else -> 0.0
        }

        val pixDetails = PixDetails(
            sendName = pixDetailsMap["sendName"].toString(),
            recipientName = pixDetailsMap["recipientName"].toString(),
            recipientPix = pixDetailsMap["recipientPix"].toString(),
            fee = feeValue
        )

        val paymentMethod = snapshot.child("paymentMethod").getValue(String::class.java)?.let {
            try { PaymentMethod.valueOf(it) } catch (e: IllegalArgumentException) { PaymentMethod.BALANCE }
        } ?: PaymentMethod.BALANCE

        // Retorna o objeto completo, pronto para navegar ao recibo
        return TransactionPix(
            transaction = savedTransaction,
            pixDetails = pixDetails,
            paymentMethod = paymentMethod
        )
    }



    override suspend fun getTransactionPixById(id: String): TransactionPix? {
        val snapshot = transactionReference.child(id).get().await()

        val transaction = snapshot.getValue(Transaction::class.java) ?: return null

        val pixDetailsMap = snapshot.child("pixDetails").value as? Map<String, Any> ?: return null
        val feeValue = when (val fee = pixDetailsMap["fee"]) {
            is Double -> fee
            is Long -> fee.toDouble()
            else -> 0.0
        }

        val pixDetails = PixDetails(
            sendName = pixDetailsMap["sendName"].toString(),
            recipientName = pixDetailsMap["recipientName"].toString(),
            recipientPix = pixDetailsMap["recipientPix"].toString(),
            fee = feeValue
        )

        val paymentMethod = snapshot.child("paymentMethod").getValue(String::class.java)?.let {
            try {
                PaymentMethod.valueOf(it)
            } catch (e: IllegalArgumentException) {
                PaymentMethod.BALANCE
            }
        } ?: PaymentMethod.BALANCE

        return TransactionPix(
            transaction = transaction,
            pixDetails = pixDetails,
            paymentMethod = paymentMethod
        )
    }
}
