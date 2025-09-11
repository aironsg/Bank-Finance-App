package dev.airon.bankfinance.domain.model

import com.google.firebase.database.FirebaseDatabase

data class Wallet(
    var id: String = "",
    var userId: String = "",
    var balance: Float = 0f
)
