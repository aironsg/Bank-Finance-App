package dev.airon.bankfinance.data.model

import com.google.firebase.database.FirebaseDatabase
import dev.airon.bankfinance.util.FirebaseHelper

data class Deposit(
    var id: String = "",
    val date: Long  = 0,
    val amount: Float = 0f,
) {
    init {
        this.id = FirebaseHelper.getGeneratedId()
    }
}
