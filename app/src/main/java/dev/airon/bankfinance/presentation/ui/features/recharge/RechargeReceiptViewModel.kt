package dev.airon.bankfinance.presentation.ui.features.recharge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.domain.usecase.recharge.GetRechargeUseCase
import dev.airon.bankfinance.core.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class RechargeReceiptViewModel @Inject constructor(
    private val getRechargeUseCase: GetRechargeUseCase
) : ViewModel(){

    fun getRecharge(id:String) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            val result = getRechargeUseCase.invoke(id)
            emit(StateView.Success(result))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }
}