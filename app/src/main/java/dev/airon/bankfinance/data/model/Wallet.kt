package dev.airon.bankfinance.data.model

import com.google.firebase.database.FirebaseDatabase

data class Wallet(
    var id: String = "",
    var userId: String = "",
    var balance: Float = 0f,
){
    init {
        // Generate a unique ID for the wallet using Firebase's push() method
        //Gera automaticamente um ID único para a carteira usando o método push() do Firebase
        this.id = FirebaseDatabase.getInstance().reference.push().key ?: ""

    }
}
