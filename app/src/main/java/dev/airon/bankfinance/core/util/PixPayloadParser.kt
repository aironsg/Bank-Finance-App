package dev.airon.bankfinance.core.util

object PixPayloadParser {

    data class PixData(
        val keyValue: String // Apenas o email (chave Pix)
    )

    fun parse(payload: String): PixData? {
        // Garantir que o payload não esteja vazio e seja um email válido
        val cleanPayload = payload.trim()

        return if (cleanPayload.contains("@") && cleanPayload.contains(".")) {
            PixData(keyValue = cleanPayload)
        } else {
            null // QR inválido
        }
    }
}
