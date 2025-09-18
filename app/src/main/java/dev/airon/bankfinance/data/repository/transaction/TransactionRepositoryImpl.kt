package dev.airon.bankfinance.data.repository.transaction

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.InsufficientBalanceException
import dev.airon.bankfinance.core.util.InsufficientLimitException
import dev.airon.bankfinance.data.enum.PaymentMethod
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionSource
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.domain.model.PixDetails
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.domain.model.TransactionPix
import dev.airon.bankfinance.domain.repository.creditCard.CreditCardRepository
import dev.airon.bankfinance.domain.repository.transaction.TransactionRepository
import dev.airon.bankfinance.domain.repository.wallet.WalletRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.collections.get
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TransactionRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val walletRepository: WalletRepository,
    private val creditCardRepository: CreditCardRepository,
    // walletRepository etc são usados apenas em sendTransactionByPix, mas mantemos fora aqui — injete se necessário
) : TransactionRepository {

    private val TAG = "TransactionRepository"
    private fun userTransactionRef() =
        database.reference.child("transaction").child(FirebaseHelper.getUserId())

    override suspend fun saveTransaction(transaction: Transaction) {
        // Gera id se vazio e salva como map (com date ServerValue.TIMESTAMP)
        val txRef = userTransactionRef()
        val txId = transaction.id.ifEmpty {
            txRef.push().key ?: System.currentTimeMillis().toString()
        }

        val map = transaction.toMap().toMutableMap()
        map["id"] = txId
        map["date"] = ServerValue.TIMESTAMP

        txRef.child(txId).setValue(map).await()

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
            recipientId = map["recipientId"] as? String ?: "",
            relatedCardId = map["relatedCardId"] as? String,
            source = (map["source"] as? String)?.let {
                try { TransactionSource.valueOf(it) } catch (_: Exception) { TransactionSource.WALLET }
            } ?: TransactionSource.WALLET

        )
    }

    override suspend fun getTransactions(): List<Transaction> {
        return suspendCoroutine { continuation ->
            userTransactionRef().addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val transactions = mutableListOf<Transaction>()
                    for (ds in snapshot.children) {
                        try {
                            val t = ds.getValue(Transaction::class.java)
                            if (t != null) {
                                transactions.add(t)
                            } else {
                                val map = ds.value as? Map<*, *>
                                if (map != null) transactions.add(parseTransaction(map))
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Erro parseando transação ${ds.key}", e)
                        }
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
        val snapshot = database.reference.child("transaction").child(userId).get().await()
        return snapshot.children.mapNotNull { it.getValue(Transaction::class.java) }
    }


    override suspend fun sendTransactionByPix(transactionPix: TransactionPix): TransactionPix {
        val originalTransaction = transactionPix.transaction
        val pixDetails = transactionPix.pixDetails
        val paymentMethod = transactionPix.paymentMethod

        val transactionId = originalTransaction.id.ifEmpty {
            database.reference.child("transaction").push().key
                ?: System.currentTimeMillis().toString()
        }

        val senderId = originalTransaction.senderId
        val recipientId = originalTransaction.recipientId
        val amount = originalTransaction.amount

        val updates = hashMapOf<String, Any?>()

        // 🔹 Atualiza saldo conforme método de pagamento
        if (paymentMethod == PaymentMethod.BALANCE) {
            val senderWallet = walletRepository.getWallet(senderId)
            if (senderWallet.balance < amount) throw InsufficientBalanceException("Saldo insuficiente")
            updates["wallet/$senderId/balance"] = senderWallet.balance - amount
        } else if (paymentMethod == PaymentMethod.CREDIT_CARD) {
            val senderCard = creditCardRepository.getCreditCard()
            if (senderCard.limit < amount) throw InsufficientLimitException("Limite insuficiente")
            updates["creditCard/$senderId/balance"] = senderCard.balance + amount
            updates["creditCard/$senderId/limit"] = senderCard.limit - amount
        }

        // 🔹 Sempre credita na wallet do destinatário
        val recipientWallet = walletRepository.getWallet(recipientId)
        updates["wallet/$recipientId/balance"] = recipientWallet.balance + amount

        // 🔹 Transações (out e in)
        val senderTransaction = originalTransaction.copy(
            id = transactionId,
            type = TransactionType.PIX_OUT,
            date = 0L,
            source = if (paymentMethod == PaymentMethod.CREDIT_CARD) TransactionSource.CREDIT_CARD else TransactionSource.WALLET
        )

        val recipientTransaction = originalTransaction.copy(
            id = transactionId,
            type = TransactionType.PIX_IN,
            date = 0L,
            source = TransactionSource.WALLET // 🔹 Sempre cai na wallet
        )


        val commonPixDetails = mapOf(
            "sendName" to pixDetails.sendName,
            "recipientName" to pixDetails.recipientName,
            "recipientPix" to pixDetails.recipientPix,
            "fee" to pixDetails.fee
        )

        val senderMap = senderTransaction.toMap().toMutableMap().apply {
            put("pixDetails", commonPixDetails)
            put("paymentMethod", paymentMethod.name)
            put("date", ServerValue.TIMESTAMP)
        }

        val recipientMap = recipientTransaction.toMap().toMutableMap().apply {
            put("pixDetails", commonPixDetails)
            put("paymentMethod", paymentMethod.name)
            put("date", ServerValue.TIMESTAMP)
        }

        updates["transaction/$senderId/$transactionId"] = senderMap
        updates["transaction/$recipientId/$transactionId"] = recipientMap

        // 🔹 Executa todas as atualizações em batch
        database.reference.updateChildren(updates).await()

        // 🔹 Buscar transação salva para devolver completa
        val savedSnapshot = database.reference
            .child("transaction")
            .child(senderId)
            .child(transactionId)
            .get().await()

        val finalSavedTransaction = savedSnapshot.getValue(Transaction::class.java)
            ?: run {
                val map = savedSnapshot.value as? Map<*, *> ?: emptyMap<Any, Any>()
                parseTransaction(map)
            }

        val finalPixDetailsMap = savedSnapshot.child("pixDetails").value as? Map<String, Any> ?: emptyMap()
        val finalFeeValue = when (val fee = finalPixDetailsMap["fee"]) {
            is Double -> fee
            is Long -> fee.toDouble()
            else -> 0.0
        }

        val finalPixDetails = PixDetails(
            sendName = finalPixDetailsMap["sendName"].toString(),
            recipientName = finalPixDetailsMap["recipientName"].toString(),
            recipientPix = finalPixDetailsMap["recipientPix"].toString(),
            fee = finalFeeValue
        )

        val finalPaymentMethod = savedSnapshot.child("paymentMethod").getValue(String::class.java)?.let {
            try { PaymentMethod.valueOf(it) } catch (e: IllegalArgumentException) { paymentMethod }
        } ?: paymentMethod

        return TransactionPix(
            transaction = finalSavedTransaction,
            pixDetails = finalPixDetails,
            paymentMethod = finalPaymentMethod
        )
    }



    override suspend fun getTransactionPixById(id: String): TransactionPix? {
        val userId = FirebaseHelper.getUserId()
        val snapshot = database.reference.child("transaction").child(userId).child(id).get().await()
        if (!snapshot.exists()) return null

        val transaction = snapshot.getValue(Transaction::class.java) ?: run {
            val map = snapshot.value as? Map<*, *> ?: return null
            parseTransaction(map)
        }

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
            try { PaymentMethod.valueOf(it) } catch (e: IllegalArgumentException) { PaymentMethod.BALANCE }
        } ?: PaymentMethod.BALANCE

        return TransactionPix(transaction = transaction, pixDetails = pixDetails, paymentMethod = paymentMethod)
    }
}
