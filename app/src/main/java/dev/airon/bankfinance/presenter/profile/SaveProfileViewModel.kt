package dev.airon.bankfinance.presenter.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.data.model.User
import dev.airon.bankfinance.domain.profile.SaveProfileUsecase
import dev.airon.bankfinance.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class SaveProfileViewModel @Inject constructor(
    private val saveProfileUsecase: SaveProfileUsecase

) : ViewModel() {

    fun saveProfile(user: User) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            saveProfileUsecase.invoke(user)
            emit(StateView.Success(null))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }

}