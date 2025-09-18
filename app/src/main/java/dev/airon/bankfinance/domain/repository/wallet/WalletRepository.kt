package dev.airon.bankfinance.domain.repository.wallet

import dev.airon.bankfinance.domain.model.Wallet

interface WalletRepository {
    suspend fun initWallet(wallet: Wallet)
    suspend fun getWallet(): Wallet
    suspend fun getWallet(id: String): Wallet
}