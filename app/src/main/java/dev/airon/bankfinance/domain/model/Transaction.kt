package dev.airon.bankfinance.domain.model

import android.os.Parcelable
import dev.airon.bankfinance.data.enum.PaymentMethod
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionSource
import dev.airon.bankfinance.data.enum.TransactionType
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Transaction(
    var id: String = "",
    val operation: @RawValue TransactionOperation? = null,
    val date: Long = 0L,
    val amount: Float = 0f,
    val type: @RawValue TransactionType? = null,
    val senderId: String = "",
    val recipientId: String = "",
    val relatedCardId: String? = null,
    val source: TransactionSource = TransactionSource.WALLET
) : Parcelable {

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "operation" to operation?.name,
            "date" to date,
            "amount" to amount,
            "type" to type?.name,
            "senderId" to senderId,
            "recipientId" to recipientId,
            "relatedCardId" to relatedCardId,
            "source" to source.name
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
