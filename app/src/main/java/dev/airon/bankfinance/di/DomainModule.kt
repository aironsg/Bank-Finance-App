package dev.airon.bankfinance.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.airon.bankfinance.data.repository.auth.AuthFirebaseDataSource
import dev.airon.bankfinance.data.repository.auth.AuthFirebaseDataSourceImpl

@Module
@InstallIn(ViewModelComponent::class)
 abstract class DomainModule {

    /**
     * Vincula o [AuthFirebaseDataSourceImpl] à interface [AuthFirebaseDataSource].
     * Isso permite a injeção de dependência da classe de implementação.
     *
     * @param authFirebaseDataSourceImpl A implementação da interface AuthFirebaseDataSource.
     * @return A interface AuthFirebaseDataSource.
     */
     @Binds
     abstract fun bindsAuthRepository(
         authFirebaseDataSourceImpl: AuthFirebaseDataSourceImpl
     ): AuthFirebaseDataSource
}