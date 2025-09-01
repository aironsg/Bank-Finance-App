package dev.airon.bankfinance.domain.model

data class TransactionPix(
    val transaction: Transaction,
    val pixDetails: PixDetails
)
