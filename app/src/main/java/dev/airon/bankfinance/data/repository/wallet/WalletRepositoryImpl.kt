package dev.airon.bankfinance.data.repository.wallet

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.domain.model.Wallet
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.domain.repository.wallet.WalletRepository
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class WalletRepositoryImpl @Inject constructor(
    database: FirebaseDatabase
) : WalletRepository {

    private val walletReference = database.reference.child("wallet")

    override suspend fun initWallet(wallet: Wallet) {
        val userId = FirebaseHelper.getUserId()
        wallet.id = userId        // 🔹 ID fixo da carteira = UID do usuário
        wallet.userId = userId

        return suspendCoroutine { continuation ->
            walletReference
                .child(userId)
                .setValue(wallet)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resumeWith(Result.success(Unit))
                    } else {
                        task.exception?.let {
                            continuation.resumeWith(Result.failure(it))
                        }
                    }
                }
        }
    }

    override suspend fun getWallet(): Wallet {
        val userId = FirebaseHelper.getUserId()
        return suspendCoroutine { continuation ->
            walletReference
                .child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val wallet = snapshot.getValue(Wallet::class.java)
                        if (wallet != null) {
                            continuation.resumeWith(Result.success(wallet))
                        } else {
                            continuation.resumeWith(
                                Result.failure(Exception("Wallet não encontrada"))
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWith(Result.failure(error.toException()))
                    }
                })
        }
    }
}
