package dev.airon.bankfinance.data.enum
enum class TransactionType {
    CASH_IN,         // Entrada (Depósitos, Recebimentos, etc.)
    CASH_OUT,        // Saída (Saques)
    PIX_IN,          // Pix recebido
    PIX_OUT,         // Pix enviado
    CARD_PAYMENT,    // Pagamento de fatura do cartão
    CREDIT_CARD;     // Compra feita com cartão de crédito

    companion object {
        fun getType(operation: TransactionOperation, isOutgoing: Boolean = false): Char {
            return when (operation) {
                TransactionOperation.DEPOSIT -> 'D'
                TransactionOperation.RECHARGE -> 'R'
                TransactionOperation.CREDIT_CARD_PURCHASE -> 'C'
                TransactionOperation.PIX -> {
                    if (isOutgoing) 'O' else 'P' // Outgoing (enviado) / Incoming (recebido)
                }
                TransactionOperation.CARD_PAYMENT -> 'F' // Fatura
                TransactionOperation.CASH_OUT -> 'S' // Saque
            }
        }
    }
}
