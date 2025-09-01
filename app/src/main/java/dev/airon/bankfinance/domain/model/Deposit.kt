package dev.airon.bankfinance.domain.model

import android.os.Parcelable
import dev.airon.bankfinance.core.util.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class Deposit(
    var id: String = "",
    val date: Long  = 0,
    val amount: Float = 0f,
)  : Parcelable{
    init {
        this.id = FirebaseHelper.getGeneratedId()
    }
}
