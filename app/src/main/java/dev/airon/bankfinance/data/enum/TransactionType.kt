package dev.airon.bankfinance.data.enum

enum class TransactionType {
    CASH_IN,
    CASH_OUT;

   companion object {
        fun getType(operation: TransactionOperation): Char {
            return when(operation){
                TransactionOperation.DEPOSIT -> {
                    'D'
                }
            }
        }
    }
}