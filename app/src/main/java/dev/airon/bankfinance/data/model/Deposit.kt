package dev.airon.bankfinance.data.model

import android.os.Parcelable
import com.google.firebase.database.FirebaseDatabase
import dev.airon.bankfinance.util.FirebaseHelper
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
