package dev.airon.bankfinance.presentation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.airon.bankfinance.data.repository.auth.AuthFirebaseRepositoryImpl
import dev.airon.bankfinance.data.repository.deposit.DepositRepositoryImpl
import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import dev.airon.bankfinance.domain.repository.auth.AuthFirebaseRepository
import dev.airon.bankfinance.domain.repository.deposit.DepositRepository
import dev.airon.bankfinance.domain.repository.transaction.TransactionRepository

@Module
@InstallIn(ViewModelComponent::class)
 abstract class DomainModule {

    /**
     * Vincula o [AuthFirebaseRepositoryImpl] à interface [AuthFirebaseRepository].
     * Isso permite a injeção de dependência da classe de implementação.
     *
     * @param authFirebaseRepositoryImpl A implementação da interface AuthFirebaseDataSource.
     * @return A interface AuthFirebaseDataSource.
     */
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