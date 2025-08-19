package dev.airon.bankfinance.data.enum

enum class TransactionType {
    CASH_IN,
    CASH_OUT,
    PIX,
    CREDIT_CARD;


   companion object {
        fun getType(operation: TransactionOperation): Char {
            return when(operation){
                TransactionOperation.DEPOSIT -> {
                    'D'
                }

                TransactionOperation.RECHARGE -> {
                    'R'
                }
                TransactionOperation.CREDIT_CARD_PURCHASE -> {
                    'C'
                }
                TransactionOperation.PIX -> {
                    'P'}

            }
        }
    }
}