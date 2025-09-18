package dev.airon.bankfinance.presentation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.airon.bankfinance.data.repository.auth.AuthFirebaseRepositoryImpl
import dev.airon.bankfinance.data.repository.deposit.DepositRepositoryImpl
import dev.airon.bankfinance.domain.repository.auth.AuthFirebaseRepository
import dev.airon.bankfinance.domain.repository.deposit.DepositRepository

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



}