package dev.airon.bankfinance.presenter.features.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.data.model.Account
import dev.airon.bankfinance.domain.account.InitAccountUseCase
import dev.airon.bankfinance.util.StateView
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val initAccountUseCase: InitAccountUseCase
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

}