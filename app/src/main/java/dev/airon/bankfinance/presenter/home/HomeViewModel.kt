package dev.airon.bankfinance.presenter.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.data.model.Transaction
import dev.airon.bankfinance.domain.Transaction.GetTransactionsUseCase
import dev.airon.bankfinance.domain.wallet.GetWalletUseCase
import dev.airon.bankfinance.presenter.wallet.WalletViewModel
import dev.airon.bankfinance.util.StateView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getWalletUseCase: GetWalletUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase

): ViewModel(){

//    private val _transactions  = MutableLiveData<List<Transaction>>()
//    val transactions : LiveData<List<Transaction>> = _transactions

    fun getWallet() = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            val wallet = getWalletUseCase.invoke()
            emit(StateView.Success(wallet))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }

    fun getTransactions() = liveData(Dispatchers.IO){

        try {

            emit(StateView.Loading())
            val transactions = getTransactionsUseCase.invoke()
            emit(StateView.Success(transactions))

        }catch (ex: Exception){
            emit(StateView.Error(ex.message))
        }
    }





}