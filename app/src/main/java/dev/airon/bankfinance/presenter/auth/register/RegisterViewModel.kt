package dev.airon.bankfinance.presenter.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.domain.auth.RegisterUseCase
import dev.airon.bankfinance.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    fun register(
        name: String,
        accountNumber: String,
        cpf: String,
        rg: String,
        phone: String,
        email: String,
        password: String,
        passwordTransaction: String
    ) = liveData(Dispatchers.IO) {

        try {

            emit(StateView.Loading())
            val user = registerUseCase.invoke(name,accountNumber,cpf,rg, phone, email, password, passwordTransaction)
            emit(StateView.Success(user))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message))
        }
    }
}