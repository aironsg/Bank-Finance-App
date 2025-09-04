package dev.airon.bankfinance.core.util

object PixPayloadParser {
    data class PixData(
        val keyType: String,
        val keyValue: String,
        val beneficiaryName: String,
        val amount: Float,
        val txId: String?
    )

    fun parse(payload: String): PixData? {
        val map = payload.lines().associate {
            val parts = it.split(":")
            parts[0] to parts[1]
        }
        return PixData(
            keyType = map["KEY_TYPE"] ?: "desconhecido",
            keyValue = map["KEY_VALUE"] ?: "",
            beneficiaryName = map["BENEFICIARY_NAME"] ?: "",
            amount = map["AMOUNT"]?.toFloatOrNull() ?: 0.0f,
            txId = map["TX_ID"] ?: FirebaseHelper.getUserId()
        )
    }
}