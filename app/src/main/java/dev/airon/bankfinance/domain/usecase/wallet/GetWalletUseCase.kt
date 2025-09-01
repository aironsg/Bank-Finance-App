package dev.airon.bankfinance.domain.usecase.wallet

import dev.airon.bankfinance.domain.model.Wallet
import dev.airon.bankfinance.data.repository.wallet.WalletRepositoryImpl
import javax.inject.Inject

class GetWalletUseCase @Inject constructor(
    private val walletDataSourceImpl: WalletRepositoryImpl
) {

    suspend fun invoke() : Wallet {
        return walletDataSourceImpl.getWallet()
    }


}