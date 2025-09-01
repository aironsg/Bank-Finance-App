package dev.airon.bankfinance.domain.model

import com.google.firebase.database.Exclude

data class User(
    val id : String = "",
    var name : String = "",
    val cpf: String = "",
    val rg: String = "",
    val phone: String = "",
    val email: String = "",
    @get:Exclude
    var password: String = "",
    var passwordTransaction: String = "",
    var passwordSalt: String = ""
)

