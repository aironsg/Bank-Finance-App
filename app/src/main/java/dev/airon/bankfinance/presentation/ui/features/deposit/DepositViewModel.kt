package dev.airon.bankfinance.presentation.ui.features.deposit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.domain.model.Deposit
import dev.airon.bankfinance.domain.model.Transaction
import dev.airon.bankfinance.domain.usecase.transaction.SaveTransactionUseCase
import dev.airon.bankfinance.domain.usecase.deposit.SaveDepositUseCase
import dev.airon.bankfinance.core.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class DepositViewModel @Inject constructor(
    private val saveDepositUseCase: SaveDepositUseCase,
    private val saveTransactionUseCase: SaveTransactionUseCase
) : ViewModel(){



    /**
     * Inicia o processo de salvar um depósito.
     * O SaveDepositUseCase agora também lida com a atualização da wallet e o registro da transação.
     *
     * Casos de Teste Unitário (para o ViewModel):
     * - `saveDeposit_callsSaveDepositUseCase_withCorrectDeposit`: Verifica se saveDepositUseCase é chamado.
     * - `saveDeposit_whenUseCaseSucceeds_emitsSuccessWithDeposit`: Verifica a emissão de StateView.Success.
     * - `saveDeposit_whenUseCaseFails_emitsErrorWithMessage`: Verifica a emissão de StateView.Error.
     */
    fun processNewDeposit(deposit: Deposit) = liveData(Dispatchers.IO) { // Renomeado para clareza
        try {
            emit(StateView.Loading())
            // Teste: "saveDeposit_callsSaveDepositUseCase_withCorrectDeposit"
            val savedDepositWithDate = saveDepositUseCase.invoke(deposit) // UseCase agora faz todo o trabalho
            // Teste: "saveDeposit_whenUseCaseSucceeds_emitsSuccessWithDeposit"
            emit(StateView.Success(savedDepositWithDate)) // Retorna o depósito salvo (útil para o recibo)
        } catch (ex: Exception) {
            // Teste: "saveDeposit_whenUseCaseFails_emitsErrorWithMessage"
            emit(StateView.Error(ex.message))
        }
    }
}