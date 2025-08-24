package dev.airon.bankfinance.data.enum

import dev.airon.bankfinance.data.enum.TransactionOperation.CREDIT_CARD_PURCHASE
import dev.airon.bankfinance.data.enum.TransactionOperation.DEPOSIT
import dev.airon.bankfinance.data.enum.TransactionOperation.PIX
import dev.airon.bankfinance.data.enum.TransactionOperation.RECHARGE

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