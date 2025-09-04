package dev.airon.bankfinance.data.enum

enum class PaymentMethod {

    BALANCE,
    CREDIT_CARD;


    companion object {
        fun getOperation(operation: PaymentMethod): String {
            return when (operation) {
                BALANCE -> "Saldo Bancário"
                CREDIT_CARD -> "Cartão de Crédito"

            }
        }
    }
}