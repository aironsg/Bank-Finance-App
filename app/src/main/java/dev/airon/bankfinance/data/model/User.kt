package dev.airon.bankfinance.data.model

import com.google.firebase.database.Exclude

data class User(
    val id : String = "",
    val name : String = "",
    val phone: String = "",
    val email: String = "",
    @get:Exclude
    val password: String = ""
)
