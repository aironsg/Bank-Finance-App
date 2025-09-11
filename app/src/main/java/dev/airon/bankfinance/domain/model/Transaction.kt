package dev.airon.bankfinance.domain.model

import android.os.Parcelable
import dev.airon.bankfinance.data.enum.PaymentMethod
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Transaction(
    var id: String = "",
    val operation: @RawValue TransactionOperation? = null, // ✅ @RawValue para enum
    val date: Long = 0,
    val amount: Float = 0f,
    val type: @RawValue TransactionType? = null,           // ✅ @RawValue para enum
    val senderId: String = "",
    val recipientId: String = ""
) : Parcelable {
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

@Parcelize
data class PixDetails(
    val sendName: String = "",
    val recipientName: String = "",
    val recipientPix: String = "",
    val fee: Double = 0.0
) : Parcelable

@Parcelize
data class TransactionPix(
    val transaction: Transaction,
    val pixDetails: PixDetails,
    val paymentMethod: PaymentMethod
) : Parcelable
