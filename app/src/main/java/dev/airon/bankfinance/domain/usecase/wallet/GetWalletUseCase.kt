package dev.airon.bankfinance.domain.usecase.wallet

import dev.airon.bankfinance.domain.model.Wallet
import dev.airon.bankfinance.data.repository.wallet.WalletRepositoryImpl
import javax.inject.Inject

class GetWalletUseCase @Inject constructor(
    private val walletRepositoryImpl: WalletRepositoryImpl
) {
    suspend operator fun invoke(): Wallet {
        return walletRepositoryImpl.getWallet()
    }
}
