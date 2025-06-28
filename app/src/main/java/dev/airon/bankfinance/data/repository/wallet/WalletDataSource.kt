package dev.airon.bankfinance.data.repository.wallet

import dev.airon.bankfinance.data.model.Wallet

interface WalletDataSource {

    suspend fun initWallet(wallet: Wallet)

    suspend fun getWallet(): Wallet


}