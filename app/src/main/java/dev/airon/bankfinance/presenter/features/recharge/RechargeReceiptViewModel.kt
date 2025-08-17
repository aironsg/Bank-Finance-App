package dev.airon.bankfinance.presenter.features.recharge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.data.model.Deposit
import dev.airon.bankfinance.domain.deposit.GetDepositUseCase
import dev.airon.bankfinance.domain.recharge.GetRechargeUseCase
import dev.airon.bankfinance.util.StateView
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