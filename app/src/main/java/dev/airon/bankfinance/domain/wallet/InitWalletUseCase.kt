package dev.airon.bankfinance.domain.wallet

import dev.airon.bankfinance.data.model.Wallet
import dev.airon.bankfinance.data.repository.wallet.WalletRepositoryImpl
import javax.inject.Inject

class InitWalletUseCase @Inject constructor(
    private val walletDataSourceImpl: WalletRepositoryImpl
) {

    suspend fun invoke(wallet: Wallet) {
        return walletDataSourceImpl.initWallet(wallet)
    }


}