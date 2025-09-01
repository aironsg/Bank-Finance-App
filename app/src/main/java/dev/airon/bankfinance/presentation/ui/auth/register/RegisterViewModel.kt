package dev.airon.bankfinance.presentation.ui.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.domain.usecase.auth.RegisterUseCase
import dev.airon.bankfinance.core.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    fun register(
        name: String,
        cpf: String,
        rg: String,
        phone: String,
        email: String,
        password: String,
        passwordTransaction: String,
        passwordSalt: String
    ) = liveData(Dispatchers.IO) {

        try {

            emit(StateView.Loading())
            val user = registerUseCase.invoke(name,cpf,rg, phone, email, password, passwordTransaction, passwordSalt)
            emit(StateView.Success(user))

        } catch (ex: Exception) {
            emit(StateView.Error(ex.message))
        }
    }
}