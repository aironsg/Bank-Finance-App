package dev.airon.bankfinance.presentation.ui.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.domain.model.Account
import dev.airon.bankfinance.domain.usecase.account.GetAccountUseCase
import dev.airon.bankfinance.domain.usecase.account.InitAccountUseCase
import dev.airon.bankfinance.core.util.StateView
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val initAccountUseCase: InitAccountUseCase,
    private val getAccountUseCase : GetAccountUseCase
) : ViewModel() {

    fun initAccount(account: Account) = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())
            initAccountUseCase(account)
            emit(StateView.Success(null))
        } catch (ex: Exception) {
            emit(StateView.Error(ex.message))
        }
    }

    fun getAccount() = liveData(Dispatchers.IO) {
        try {
            emit(StateView.Loading())
            val account = getAccountUseCase.invoke()
            emit(StateView.Success(account))
        } catch (ex: Exception) {
            emit(StateView.Error(ex.message))
        }
    }

}