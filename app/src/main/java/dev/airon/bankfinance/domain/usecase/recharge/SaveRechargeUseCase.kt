package dev.airon.bankfinance.domain.usecase.recharge

import dev.airon.bankfinance.core.util.InsufficientBalanceException
import dev.airon.bankfinance.data.enum.PaymentMethod
import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.data.repository.recharge.RechargeRepositoryImpl
import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import dev.airon.bankfinance.data.repository.wallet.WalletRepositoryImpl
import dev.airon.bankfinance.domain.model.Recharge
import dev.airon.bankfinance.domain.model.Transaction
import javax.inject.Inject

class SaveRechargeUseCase @Inject constructor(
    private val rechargeRepositoryImpl: RechargeRepositoryImpl,
    private val walletRepositoryImpl: WalletRepositoryImpl,
    private val transactionRepositoryImpl: TransactionRepositoryImpl
    // private val creditCardRepository: CreditCardRepository // Necessário se for processar pagamento com cartão aqui

) {
    /**
     * Processa uma nova recarga de celular.
     * 1. Salva o registro da recarga.
     * 2. Se o pagamento for com SALDO EM CONTA:
     *    a. Busca a carteira (wallet) atual do usuário.
     *    b. Verifica se há saldo suficiente.
     *    c. Debita o valor da recarga do saldo da wallet.
     *    d. Salva a wallet atualizada.
     *    e. Cria e salva uma transação de RECHARGE / CASH_OUT.
     * 3. Se o pagamento for com CARTÃO DE CRÉDITO:
     *    a. (Lógica de atualização do limite/fatura do cartão - como no RechargeFragment ou movida para cá)
     *    b. Cria e salva uma transação de RECHARGE / CREDIT_CARD.
     *
     * @param recharge O objeto Recharge a ser processado.
     * @return O objeto Recharge salvo (com data/hora preenchidos).
     * @throws InsufficientBalanceException se o saldo da wallet for insuficiente (para pagamento com saldo).
     * @throws Exception para outros erros.
     *
     * Casos de Teste Unitário Sugeridos:
     * - `invoke_validRecharge_savesRechargeObjectCorrectly`: Verifica se rechargeRepository.saveRecharge é chamado.
     * - `invoke_paymentWithBalance_fetchesCurrentUserWallet`: Se typeRecharge for BALANCE, verifica se walletRepository.getWallet é chamado.
     * - `invoke_paymentWithBalance_insufficientWalletBalance_throwsInsufficientBalanceException`: Testa o lançamento da exceção.
     * - `invoke_paymentWithBalance_sufficientWalletBalance_updatesWalletBalanceCorrectly`: Verifica se o saldo da wallet é decrementado.
     * - `invoke_paymentWithBalance_sufficientWalletBalance_savesUpdatedWallet`: Verifica se walletRepository.initWallet é chamado com a wallet correta.
     * - `invoke_paymentWithBalance_createsAndSavesCashOutTransaction`: Verifica se a transação RECHARGE/CASH_OUT é salva.
     * - `invoke_paymentWithCreditCard_createsAndSavesCreditCardTransaction`: Verifica se a transação RECHARGE/CREDIT_CARD é salva.
     * - `invoke_paymentWithCreditCard_updatesCreditCardDetails`: (Se a lógica do cartão for movida para cá) Verifica a atualização do cartão.
     */

    suspend operator fun invoke(recharge: Recharge): Recharge {
        // Teste: "invoke_validRecharge_savesRechargeObjectCorrectly"
        // O RechargeRepositoryImpl já define date e hour, então passamos o recharge como está.
        val savedRecharge = rechargeRepositoryImpl.saveRecharge(recharge)

        val transactionType: TransactionType
        val operationType = TransactionOperation.RECHARGE

        when (savedRecharge.typeRecharge) {
            PaymentMethod.BALANCE -> {
                // Teste: "invoke_paymentWithBalance_fetchesCurrentUserWallet"
                val currentWallet = walletRepositoryImpl.getWallet()

                // Teste: "invoke_paymentWithBalance_insufficientWalletBalance_throwsInsufficientBalanceException"
                if (savedRecharge.amount > currentWallet.balance) {
                    throw InsufficientBalanceException("Saldo em conta insuficiente para realizar a recarga. Saldo atual: R$${currentWallet.balance}")
                }

                // Teste: "invoke_paymentWithBalance_sufficientWalletBalance_updatesWalletBalanceCorrectly"
                val newWalletBalance = currentWallet.balance - savedRecharge.amount
                // Teste: "invoke_paymentWithBalance_sufficientWalletBalance_savesUpdatedWallet"
                val updatedWallet = currentWallet.copy(balance = newWalletBalance)
                walletRepositoryImpl.initWallet(updatedWallet)

                transactionType = TransactionType.CASH_OUT
            }
            PaymentMethod.CREDIT_CARD -> {


                transactionType = TransactionType.CREDIT_CARD
            }

        }


        val transaction = Transaction(
            id = savedRecharge.id, // Usa o ID da recarga salva
            operation = operationType,
            date = savedRecharge.date, // Usa a data da recarga salva
            amount = savedRecharge.amount,
            type = transactionType

        )
        transactionRepositoryImpl.saveTransaction(transaction)

        return savedRecharge
    }
}