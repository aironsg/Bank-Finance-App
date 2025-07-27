package dev.airon.bankfinance.data.repository.wallet

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.data.model.Wallet
import dev.airon.bankfinance.util.FirebaseHelper
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class WalletRepositoryImpl @Inject constructor(
    database: FirebaseDatabase
) : WalletRepository {


    private val walletReference = database.reference
        .child("wallet")


    override suspend fun initWallet(wallet: Wallet) {
        return suspendCoroutine { continuation ->
            walletReference
                .child(FirebaseHelper.getUserId())
                .setValue(wallet)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Profile saved successfully
                        continuation.resumeWith(Result.success(Unit))
                    } else {
                        // Failed to save profile
                        task.exception?.let {
                            continuation.resumeWith(Result.failure(it))
                        }
                    }
                }
        }
    }

    override suspend fun getWallet(): Wallet {
        return suspendCoroutine { continuation ->
            walletReference
                .child(FirebaseHelper.getUserId())
            walletReference.addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {
                    val wallet = snapshot.getValue(Wallet::class.java)
                    wallet?.let {
                        continuation.resumeWith(Result.success(it))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWith(Result.failure(error.toException()))
                }
            })
        }
    }

}