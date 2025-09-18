package dev.airon.bankfinance.data.repository.wallet

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.domain.model.Wallet
import dev.airon.bankfinance.domain.repository.wallet.WalletRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
class WalletRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : WalletRepository {

    private val walletReference = database.reference.child("wallet")

    override suspend fun initWallet(wallet: Wallet) {
        val userId = FirebaseHelper.getUserId()
        wallet.id = userId
        wallet.userId = userId
        return suspendCancellableCoroutine { continuation ->
            walletReference.child(userId).setValue(wallet).addOnCompleteListener { task ->
                if (task.isSuccessful) continuation.resume(Unit)
                else task.exception?.let { continuation.resumeWithException(it) } ?: continuation.resumeWithException(Exception("Erro ao salvar wallet"))
            }
        }
    }

    override suspend fun getWallet(): Wallet {
        val userId = FirebaseHelper.getUserId()
        return suspendCancellableCoroutine { continuation ->
            walletReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val wallet = snapshot.getValue(Wallet::class.java)
                    if (wallet != null) {
                        val consistentWallet = wallet.copy(id = userId, userId = userId)
                        continuation.resume(consistentWallet)
                    } else {
                        continuation.resumeWithException(Exception("Carteira não encontrada"))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            })
        }
    }

    override suspend fun getWallet(id: String): Wallet {
        if (id.isBlank()) throw IllegalArgumentException("id não pode ser vazio")
        return suspendCancellableCoroutine { continuation ->
            walletReference.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val wallet = snapshot.getValue(Wallet::class.java)
                    if (wallet != null) {
                        val consistentWallet = wallet.copy(id = id, userId = id)
                        continuation.resume(consistentWallet)
                    } else {
                        continuation.resumeWithException(Exception("Carteira não encontrada para o usuário: $id"))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWithException(error.toException())
                }
            })
        }
    }
}