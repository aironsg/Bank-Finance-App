package dev.airon.bankfinance.data.enum

enum class TransactionType {
    CASH_IN,
    CASH_OUT,
    PIX_IN,
    PIX_OUT,
    CREDIT_CARD;

    companion object {
        fun getType(operation: TransactionOperation, isOutgoing: Boolean = false): Char {
            return when (operation) {
                TransactionOperation.DEPOSIT -> 'D'
                TransactionOperation.RECHARGE -> 'R'
                TransactionOperation.CREDIT_CARD_PURCHASE -> 'C'
                TransactionOperation.PIX -> {
                    if (isOutgoing) 'O' else 'P' // Out / In
                }
            }
        }
    }
}
