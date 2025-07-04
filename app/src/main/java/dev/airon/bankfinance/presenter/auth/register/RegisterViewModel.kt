package dev.airon.bankfinance.presenter.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.domain.auth.RegisterUsecase
import dev.airon.bankfinance.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUsecase: RegisterUsecase
) : ViewModel() {

    fun register(name: String, phone: String, email: String, password: String) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            val user = registerUsecase.invoke(name, phone, email, password)
            emit(StateView.Success(user))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }
}