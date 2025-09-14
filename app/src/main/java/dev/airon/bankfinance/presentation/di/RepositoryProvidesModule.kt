// dev/airon/bankfinance/presentation/di/RepositoryProvidesModule.kt
package dev.airon.bankfinance.presentation.di

import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent // <- Mude para SingletonComponent se WalletRepository e CreditCardRepository forem singletons
// Se eles devem seguir o escopo do ViewModel, mantenha ViewModelComponent, mas
// SingletonComponent é mais comum para Repositórios.
// Ajuste o @InstallIn aqui e nos seus @Singleton se necessário.
import dev.airon.bankfinance.data.repository.creditCard.CreditCardRepositoryImpl
import dev.airon.bankfinance.data.repository.wallet.WalletRepositoryImpl
import dev.airon.bankfinance.domain.repository.creditCard.CreditCardRepository
import dev.airon.bankfinance.domain.repository.wallet.WalletRepository
import jakarta.inject.Singleton // <- Verifique se este é o import correto para @Singleton (geralmente javax.inject.Singleton)


// Se WalletRepository e CreditCardRepository são para serem singletons em toda a aplicação:
@Module
@InstallIn(SingletonComponent::class) // Use SingletonComponent para singletons de aplicação
object RepositoryProvidesModule { // Módulos com apenas @Provides podem ser 'object'


    @Provides
    @Singleton // Garante que apenas uma instância seja criada e reutilizada
    fun provideWalletRepository(database: FirebaseDatabase): WalletRepository {
        return WalletRepositoryImpl(database)
    }

    @Provides
    @Singleton // Garante que apenas uma instância seja criada e reutilizada
    fun provideCreditCardRepository(
        database: FirebaseDatabase,
        // Hilt injetará a implementação concreta de WalletRepository aqui
        // se WalletRepositoryImpl tiver um construtor @Inject ou for provido em outro lugar.
        // Como estamos provendo WalletRepository acima, Hilt usará essa provisão.
        walletRepository: WalletRepository // Injete a INTERFACE, Hilt resolverá
    ): CreditCardRepository {
        // Se CreditCardRepositoryImpl espera WalletRepositoryImpl, você pode precisar ajustar
        // o construtor de CreditCardRepositoryImpl para aceitar a interface WalletRepository,
        // ou fazer um cast (menos ideal), ou prover WalletRepositoryImpl diretamente.
        // A melhor prática é CreditCardRepositoryImpl depender da interface WalletRepository.
        // Assumindo que CreditCardRepositoryImpl espera WalletRepositoryImpl:
        // return CreditCardRepositoryImpl(database, walletRepository as WalletRepositoryImpl) // Cast se necessário e você tem certeza
        // OU, se CreditCardRepositoryImpl pode tomar a interface:
        // return CreditCardRepositoryImpl(database, walletRepository)

        // *** A sua implementação original injeta WalletRepositoryImpl, então vamos manter isso
        //     para a correção mínima, mas idealmente CreditCardRepositoryImpl dependeria da interface.
        //     Para que isso funcione, Hilt precisa saber como prover WalletRepositoryImpl.
        //     Como `provideWalletRepository` retorna a interface `WalletRepository`,
        //     e aqui você pede `WalletRepositoryImpl`, Hilt não vai conectar diretamente.

        // SOLUÇÃO 1: Fazer CreditCardRepositoryImpl depender da interface WalletRepository
        //    No construtor de CreditCardRepositoryImpl:
        //    constructor(private val database: FirebaseDatabase, private val walletRepository: WalletRepository)
        //    E então aqui:
        //    return CreditCardRepositoryImpl(database, walletRepository)

        // SOLUÇÃO 2 (Mantendo a dependência de WalletRepositoryImpl em CreditCardRepositoryImpl):
        //    Você precisa prover WalletRepositoryImpl explicitamente ou garantir que seu construtor é @Inject.
        //    Se WalletRepositoryImpl tem um construtor @Inject constructor(private val database: FirebaseDatabase)
        //    então Hilt pode criá-lo.
        //    E no provideCreditCardRepository, você pediria WalletRepositoryImpl:
        //    walletRepositoryImpl: WalletRepositoryImpl -> Esta é a sua implementação original.

        // Para corresponder à sua injeção original em CreditCardRepository:
        // (Isso assume que WalletRepositoryImpl tem um construtor que Hilt pode satisfazer,
        // por exemplo, @Inject constructor(database: FirebaseDatabase) )
        // Ou, se você quer usar a instância provida por `provideWalletRepository` e ela é uma `WalletRepositoryImpl`:

        // DADO O CÓDIGO ORIGINAL:
        // Você está provendo `WalletRepository` como uma interface.
        // E injetando `WalletRepositoryImpl` em `provideCreditCardRepository`.
        // Isso é um leve desalinhamento. Hilt resolverá `WalletRepositoryImpl` se ele
        // tiver um construtor `@Inject` ou se for explicitamente provido.

        // CORREÇÃO MAIS DIRETA MANTENDO A LÓGICA DE INJEÇÃO ATUAL:
        // Se CreditCardRepositoryImpl DEVE receber WalletRepositoryImpl, e
        // WalletRepositoryImpl tem um construtor @Inject, está ok.
        // Se não, você precisaria de:
        // @Provides
        // fun provideWalletRepositoryImpl(database: FirebaseDatabase): WalletRepositoryImpl {
        // return WalletRepositoryImpl(database)
        // }
        // E então usá-lo em provideCreditCardRepository.

        // ASSUMINDO QUE `WalletRepositoryImpl` tem um construtor injetável ou você quer
        // instanciá-lo diretamente aqui (o que é menos flexível que usar a provisão de interface).
        // Vamos manter a sua injeção original por enquanto.
        return CreditCardRepositoryImpl(database, WalletRepositoryImpl(database))
        // ^^ OU se WalletRepositoryImpl pode ser injetado por Hilt:
        // return CreditCardRepositoryImpl(database, walletRepositoryImplInstance) onde walletRepositoryImplInstance
        // é um parâmetro WalletRepositoryImpl walletRepositoryImplInstance
    }
}
