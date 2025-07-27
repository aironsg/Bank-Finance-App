package dev.airon.bankfinance.data.model

import com.google.firebase.database.FirebaseDatabase
import dev.airon.bankfinance.util.FirebaseHelper

data class Extract(
    var id: String = "",
    var operation: String = "",
    val date: Long  = 0,
    val hour: Long = 0,
    val amount: Float = 0f,
    var type: String = ""
) {
    init {
        this.id = FirebaseHelper.getGeneratedId()
    }
}
