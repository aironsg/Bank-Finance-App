package dev.airon.bankfinance.domain.model
import android.os.Parcelable
import dev.airon.bankfinance.core.util.FirebaseHelper
import kotlinx.parcelize.Parcelize

@Parcelize
data class Account(
    var id: String = "",
    val name: String = "",
    val branch:String = "",
    val accountNumber: String = "",
    val balance : Float = 0f,

    ) : Parcelable {
    init {
        this.id = FirebaseHelper.getGeneratedId()
    }


}
