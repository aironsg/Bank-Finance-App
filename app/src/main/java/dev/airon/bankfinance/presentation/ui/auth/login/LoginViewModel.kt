package dev.airon.bankfinance.presentation.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.domain.usecase.auth.LoginUseCase
import dev.airon.bankfinance.core.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
): ViewModel() {

    fun login(email: String, password: String) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            loginUseCase.invoke(email, password)
            emit(StateView.Success(null))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }
}