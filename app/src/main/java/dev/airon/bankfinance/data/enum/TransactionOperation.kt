package dev.airon.bankfinance.data.enum

enum class TransactionOperation {
    DEPOSIT,
    RECHARGE;

  companion object {
    fun getOperation(operation: TransactionOperation): String {
      return when (operation) {
        DEPOSIT -> "Depósito"
        RECHARGE -> "Recarga"
      }
    }
  }
}