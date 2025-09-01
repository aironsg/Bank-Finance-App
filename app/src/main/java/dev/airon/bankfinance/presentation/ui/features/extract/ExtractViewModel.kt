package dev.airon.bankfinance.presentation.ui.features.extract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.airon.bankfinance.domain.usecase.transaction.GetTransactionsUseCase
import dev.airon.bankfinance.core.util.StateView
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


@HiltViewModel
class ExtractViewModel @Inject constructor(

    private val getTransactionsUseCase: GetTransactionsUseCase

): ViewModel(){

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