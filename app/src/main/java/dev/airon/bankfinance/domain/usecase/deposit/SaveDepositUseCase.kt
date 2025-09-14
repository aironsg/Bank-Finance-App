package dev.airon.bankfinance.domain.usecase.deposit

import dev.airon.bankfinance.data.enum.TransactionOperation
import dev.airon.bankfinance.data.enum.TransactionType
import dev.airon.bankfinance.domain.model.Deposit
import dev.airon.bankfinance.data.repository.deposit.DepositRepositoryImpl
import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import dev.airon.bankfinance.data.repository.wallet.WalletRepositoryImpl
import dev.airon.bankfinance.domain.model.Transaction
import javax.inject.Inject

class SaveDepositUseCase @Inject constructor(
    private val depositRepositoryImpl: DepositRepositoryImpl,
    private val walletRepositoryImpl: WalletRepositoryImpl,
    private val transactionRepositoryImpl: TransactionRepositoryImpl
) {

    /**
     * Processa um novo depósito.
     * 1. Salva o registro do depósito.
     * 2. Atualiza o saldo da carteira (wallet) do usuário.
     * 3. Cria e salva uma transação correspondente do tipo CASH_IN.
     *
     * @param deposit O objeto Deposit a ser processado.
     * @return O objeto Deposit salvo (com data/hora do servidor).
     * @throws Exception se ocorrer algum erro durante o processo (ex: falha ao buscar wallet, erro no Firebase).
     *
     * Casos de Teste Unitário Sugeridos:
     * - `invoke_validDeposit_savesDepositCorrectly`: Verifica se depositRepository.saveDeposit é chamado com o deposit correto.
     * - `invoke_validDeposit_fetchesCurrentUserWallet`: Verifica se walletRepository.getWallet é chamado.
     * - `invoke_validDeposit_updatesWalletBalanceCorrectly`: Verifica se o saldo da wallet é incrementado pelo valor do depósito.
     * - `invoke_validDeposit_savesUpdatedWallet`: Verifica se walletRepository.initWallet é chamado com a wallet atualizada.
     * - `invoke_validDeposit_createsAndSavesCashInTransaction`: Verifica se uma transação do tipo DEPOSIT e CASH_IN é criada com os dados corretos e transactionRepository.saveTransaction é chamado.
     * - `invoke_depositRepositoryFails_throwsExceptionAndDoesNotUpdateWalletOrTransaction`: Se saveDeposit falhar, garante que as operações subsequentes não ocorram e a exceção seja propagada.
     * - `invoke_getWalletFails_throwsExceptionAndDoesNotSaveTransaction`: Se getWallet falhar, a transação não deve ser salva.
     * - `invoke_initWalletFails_throwsExceptionAndTransactionMayOrMayNotBeSaved`: Dependendo da política de rollback, mas idealmente, a transação não seria salva ou seria compensada. (Testes de rollback são mais complexos).
     */
    suspend operator fun invoke(deposit: Deposit): Deposit {
        val saveDeposit =  depositRepositoryImpl.saveDeposit(deposit)
        val currentWallet = walletRepositoryImpl.getWallet()
        val newBalance = currentWallet.balance + saveDeposit.amount
        val updatedWallet = currentWallet.copy(balance = newBalance)

        walletRepositoryImpl.initWallet(updatedWallet)
        val transaction = Transaction(
            id = saveDeposit.id,
            operation = TransactionOperation.DEPOSIT,
            date = saveDeposit.date,
            amount  = saveDeposit.amount,
            type = TransactionType.CASH_IN
        )
        transactionRepositoryImpl.saveTransaction(transaction)
        return saveDeposit
    }


}