package dev.airon.bankfinance.presentation.di

import com.google.firebase.database.FirebaseDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.airon.bankfinance.data.repository.auth.AuthFirebaseRepositoryImpl
import dev.airon.bankfinance.data.repository.creditCard.CreditCardRepositoryImpl
import dev.airon.bankfinance.data.repository.deposit.DepositRepositoryImpl
import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import dev.airon.bankfinance.data.repository.wallet.WalletRepositoryImpl
import dev.airon.bankfinance.domain.repository.auth.AuthFirebaseRepository
import dev.airon.bankfinance.domain.repository.creditCard.CreditCardRepository
import dev.airon.bankfinance.domain.repository.deposit.DepositRepository
import dev.airon.bankfinance.domain.repository.transaction.TransactionRepository
import dev.airon.bankfinance.domain.repository.wallet.WalletRepository
import jakarta.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
 abstract class DomainModule {


     @Binds
     abstract fun bindsAuthRepository(
        authFirebaseRepositoryImpl: AuthFirebaseRepositoryImpl
     ): AuthFirebaseRepository


     @Binds
     abstract fun bindsDepositRepository(
         depositRepositoryImpl: DepositRepositoryImpl
     ): DepositRepository


    @Binds
    abstract fun bindsTransactionRepository(
        transactionRepositoryImpl: TransactionRepositoryImpl
    ): TransactionRepository
}