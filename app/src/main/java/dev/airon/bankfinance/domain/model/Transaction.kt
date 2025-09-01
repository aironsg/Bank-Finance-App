package dev.airon.bankfinance.domain.model

import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType

data class Transaction(
    // TODO: adicionar campos de origem e destino 
    var id: String = "",
    val operation: TransactionOperation? = null,
    val date: Long = 0,
    val amount: Float = 0f,
    val type: TransactionType? = null
)