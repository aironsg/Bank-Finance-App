package dev.airon.bankfinance.data.model

import android.os.Parcelable
import dev.airon.bankfinance.util.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class CreditCard(
    var id: String = "",
    val number: String = "",
    val account: Account ? = null,
    val securityCode: String = "",
    val officialUser: String = "",
    val limit: Float = 0f,
    val validDate: Long = 0,
    val balance: Float = 0f

) : Parcelable {
    init {
        this.id = FirebaseHelper.getGeneratedId()
    }


}
