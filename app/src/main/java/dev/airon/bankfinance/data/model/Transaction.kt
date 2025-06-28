package dev.airon.bankfinance.data.model

import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate

data class Transaction(
     var id: String = "",
     val description: String = "",
     val value: Float = 0f,
     val date: Long
){
    init {
        this.id = FirebaseDatabase.getInstance().reference.push().key ?: ""
    }
}
