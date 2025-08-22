package dev.airon.bankfinance.data.model
import android.os.Parcelable
import com.google.firebase.database.FirebaseDatabase
import dev.airon.bankfinance.util.FirebaseHelper
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
