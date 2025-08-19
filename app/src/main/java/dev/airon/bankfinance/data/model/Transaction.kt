package dev.airon.bankfinance.data.model

import com.google.firebase.database.FirebaseDatabase
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.util.FirebaseHelper
import java.time.LocalDate

data class Transaction(
    // TODO: adicionar campos de origem e destino 
    var id: String = "",
    val operation: TransactionOperation? = null,
    val date: Long = 0,
    val amount: Float = 0f,
    val type: TransactionType? = null
)