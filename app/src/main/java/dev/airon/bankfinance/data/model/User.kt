package dev.airon.bankfinance.data.model

import com.google.firebase.database.Exclude

data class User(
    val id : String = "",
    var name : String = "",
    val phone: String = "",
    val email: String = "",
    @get:Exclude
    var password: String = ""
)
