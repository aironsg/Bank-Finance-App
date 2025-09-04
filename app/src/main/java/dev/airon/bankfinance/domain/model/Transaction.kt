package dev.airon.bankfinance.domain.model

import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType

data class Transaction(
    var id: String = "",
    val operation: TransactionOperation? = null,
    val date: Long = 0,
    val amount: Float = 0f,
    val type: TransactionType? = null,
    val senderId: String = "",       // novo
    val recipientId: String = ""     // novo
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "operation" to operation,
            "date" to date,
            "amount" to amount,
            "type" to type,
            "senderId" to senderId,
            "recipientId" to recipientId
        )
    }
}

data class PixDetails (
    val sendName: String = "",
    val recipientName: String = "",
    val recipientPix: String = "",
    val fee: Float = 0f
)

data class TransactionPix(
    val transaction: Transaction,
    val pixDetails: PixDetails
)
