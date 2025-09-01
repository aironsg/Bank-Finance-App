package dev.airon.bankfinance.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.domain.model.User
import dev.airon.bankfinance.domain.usecase.profile.GetProfileUseCase
import dev.airon.bankfinance.domain.usecase.profile.SaveProfileUseCase
import dev.airon.bankfinance.core.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val saveProfileUseCase: SaveProfileUseCase,
    private val getProfileUseCase: GetProfileUseCase

) : ViewModel() {

    fun saveProfile(user: User) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            saveProfileUseCase.invoke(user)
            emit(StateView.Success(null))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }

    fun getProfile() = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            val user = getProfileUseCase.invoke()
            emit(StateView.Success(user))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }

}