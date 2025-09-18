package dev.airon.bankfinance.domain.usecase.recharge

import android.util.Log
import dev.airon.bankfinance.core.util.FirebaseHelper

import dev.airon.bankfinance.core.util.InsufficientBalanceException
import dev.airon.bankfinance.core.util.InsufficientLimitException
import dev.airon.bankfinance.data.enum.PaymentMethod
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.data.repository.creditcard.CreditCardRepositoryImpl
import dev.airon.bankfinance.data.repository.recharge.RechargeRepositoryImpl
import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import dev.airon.bankfinance.data.repository.wallet.WalletRepositoryImpl
import dev.airon.bankfinance.domain.model.Recharge
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.domain.repository.creditCard.CreditCardRepository
import javax.inject.Inject

class SaveRechargeUseCase @Inject constructor(
    private val rechargeRepositoryImpl: RechargeRepositoryImpl,
    private val walletRepositoryImpl: WalletRepositoryImpl,
    private val transactionRepositoryImpl: TransactionRepositoryImpl,
     private val creditCardRepository: CreditCardRepositoryImpl

) {


    suspend operator fun invoke(recharge: Recharge): Recharge {
        val operationType = TransactionOperation.RECHARGE
        val transactionType: TransactionType
        val userId = FirebaseHelper.getUserId()

        // Etapa 1: Processar pagamento e atualizar fonte de fundos
        when (recharge.typeRecharge) {
            PaymentMethod.BALANCE -> {
                val currentWallet = walletRepositoryImpl.getWallet() // Assume que busca pelo userId logado
                if (recharge.amount > currentWallet.balance) {
                    throw InsufficientBalanceException("Saldo em conta insuficiente para realizar a recarga. Saldo atual: R$${"%.2f".format(currentWallet.balance)}")
                }

                val newWalletBalance = currentWallet.balance - recharge.amount
                val updatedWallet = currentWallet.copy(balance = newWalletBalance)
                // Assumindo que initWallet sobrescreve ou atualiza a wallet pelo ID
                walletRepositoryImpl.initWallet(updatedWallet)

                transactionType = TransactionType.CASH_OUT
            }
            PaymentMethod.CREDIT_CARD -> {

                val currentCard = creditCardRepository.getCreditCard() // Busca o cartão do usuário

                val availableSpendingLimit = currentCard.limit - currentCard.balance

                if (recharge.amount > availableSpendingLimit) {

                    throw InsufficientLimitException(
                        "Limite do cartão de crédito insuficiente. Limite disponível: R$${
                            "%.2f".format(
                                availableSpendingLimit
                            )
                        }"
                    ) as Throwable
                }

                // ATUALIZA O CARTÃO: A fatura (balance) do cartão AUMENTA com o valor da recarga.
                // O limite TOTAL (currentCard.limit) geralmente não muda com uma compra.
                val newCardBalance = currentCard.balance + recharge.amount
                val newLimitCard  = currentCard.limit - recharge.amount
                val updatedCard = currentCard.copy(balance = newCardBalance, limit = newLimitCard)

                // Salva o cartão atualizado.
                // initCreditCard irá sobrescrever o cartão existente com os novos dados (incluindo o novo 'balance').
                creditCardRepository.initCreditCard(updatedCard)

                transactionType = TransactionType.CREDIT_CARD // Ou um tipo mais específico se desejar
            }
            else -> {

                throw IllegalArgumentException("Método de pagamento não suportado para recarga.")
            }
        }

        val savedRecharge = rechargeRepositoryImpl.saveRecharge(recharge)

        val transaction = Transaction(
            id = savedRecharge.id, // Usa o ID da recarga salva para vincular
            operation = operationType,
            date = savedRecharge.date, // Usa a data da recarga salva
            amount = savedRecharge.amount,
            type = transactionType,
            relatedCardId = if (recharge.typeRecharge == PaymentMethod.CREDIT_CARD) userId else null
        )
        transactionRepositoryImpl.saveTransaction(transaction)
        return savedRecharge
    }
}