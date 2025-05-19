package dev.airon.bankfinance.presenter.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.data.model.Wallet
import dev.airon.bankfinance.domain.wallet.InitWalletUsecase
import dev.airon.bankfinance.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletUsecase: InitWalletUsecase
) : ViewModel(){

    fun initWallet(wallet: Wallet) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            walletUsecase.invoke(wallet)
            emit(StateView.Success(null))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }
}