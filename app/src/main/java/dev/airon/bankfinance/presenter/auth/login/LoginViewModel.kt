package dev.airon.bankfinance.presenter.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.domain.auth.LoginUsecase
import dev.airon.bankfinance.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUsecase: LoginUsecase
): ViewModel() {

    fun login(email: String, password: String) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            loginUsecase.invoke(email, password)
            emit(StateView.Success(null))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }
}