package dev.airon.bankfinance.data.enum

enum class TransactionOperation {
    DEPOSIT,
    CREDIT_CARD_PURCHASE,
    PIX,
    RECHARGE;

  companion object {
    fun getOperation(operation: TransactionOperation): String {
      return when (operation) {
        DEPOSIT -> "Depósito"
        RECHARGE -> "Recarga"
        CREDIT_CARD_PURCHASE -> "Compra no Cartão de Crédito"
        PIX -> "Transferência  PIX"
      }
    }
  }
}