package dev.airon.bankfinance.presentation.ui.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.domain.model.Wallet
import dev.airon.bankfinance.domain.usecase.wallet.InitWalletUseCase
import dev.airon.bankfinance.core.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletUseCase: InitWalletUseCase
) : ViewModel(){

    fun initWallet(wallet: Wallet) = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            walletUseCase.invoke(wallet)
            emit(StateView.Success(null))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }
}