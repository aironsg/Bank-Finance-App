package dev.airon.bankfinance.domain.wallet

import dev.airon.bankfinance.data.model.Wallet
import dev.airon.bankfinance.data.repository.wallet.WalletDataSourceImpl
import javax.inject.Inject

class InitWalletUsecase @Inject constructor(
    private val walletDataSourceImpl: WalletDataSourceImpl
) {

    suspend fun invoke(wallet: Wallet) {
        return walletDataSourceImpl.initWallet(wallet)
    }


}