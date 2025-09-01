package dev.airon.bankfinance.domain.model

data class PixDetails (
    val sendName: String = "",
    val recipientName: String = "",
    val recipentPix: String = "",
    val fee : Float = 0f,
)
