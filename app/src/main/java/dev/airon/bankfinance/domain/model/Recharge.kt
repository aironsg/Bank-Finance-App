package dev.airon.bankfinance.domain.model

import android.os.Parcelable
import dev.airon.bankfinance.data.enum.PaymentMethod
import dev.airon.bankfinance.core.util.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class Recharge(
    var id: String = "",
    var date: Long  = 0,
    var hour: Long = 0,
    val amount: Float = 0f,
    val phoneNumber: String = "",
    val typeRecharge: PaymentMethod  = PaymentMethod.BALANCE
)  : Parcelable {
    init {
        this.id = FirebaseHelper.getGeneratedId()
    }
}