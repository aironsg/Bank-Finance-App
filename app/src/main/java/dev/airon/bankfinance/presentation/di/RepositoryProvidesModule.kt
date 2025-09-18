package dev.airon.bankfinance.presentation.di // Ou o pacote correto onde seu módulo está

import com.google.firebase.database.FirebaseDatabase // Certifique-se que este import está presente
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.airon.bankfinance.data.repository.creditcard.CreditCardRepositoryImpl

import dev.airon.bankfinance.data.repository.transaction.TransactionRepositoryImpl
import dev.airon.bankfinance.data.repository.wallet.WalletRepositoryImpl
import dev.airon.bankfinance.domain.repository.creditCard.CreditCardRepository
import dev.airon.bankfinance.domain.repository.transaction.TransactionRepository
import dev.airon.bankfinance.domain.repository.wallet.WalletRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Correto para repositórios que devem ser singletons na aplicação
object RepositoryProvidesModule {


    @Provides
    @Singleton // Garante que apenas uma instância seja criada e reutilizada em toda a aplicação
    fun provideWalletRepository(database: FirebaseDatabase): WalletRepository {
        // FirebaseDatabase será injetado aqui por Hilt se provido em algum módulo SingletonComponent.
        return WalletRepositoryImpl(database)
    }


    @Provides
    @Singleton
    fun provideTransactionRepository(
        database: FirebaseDatabase,
        walletRepository: WalletRepository,
        cardRepository: CreditCardRepository// Hilt injetará a instância de WalletRepository provida acima
    ): TransactionRepository {
        return TransactionRepositoryImpl(database, walletRepository, cardRepository)
    }


    @Provides
    @Singleton
    fun provideCreditCardRepository(
        database: FirebaseDatabase,
        walletRepository: WalletRepository // Hilt injetará a instância de WalletRepository provida acima
    ): CreditCardRepository {
        return CreditCardRepositoryImpl(database, walletRepository)
    }


}

