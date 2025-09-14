package dev.airon.bankfinance.data.repository.creditCard

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.domain.model.CreditCard
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.core.util.InsufficientBalanceException
import dev.airon.bankfinance.data.repository.wallet.WalletRepositoryImpl
import dev.airon.bankfinance.domain.repository.creditCard.CreditCardRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class CreditCardRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val walletRepository: WalletRepositoryImpl
) : CreditCardRepository {

    private val userId = FirebaseHelper.getUserId()
    private val creditCardReference = database.reference
        .child("creditCard")
        .child(userId)



    override suspend fun getCreditCard(): CreditCard {
        return suspendCoroutine { continuation ->
            creditCardReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val creditCard = snapshot.getValue(CreditCard::class.java)
                    creditCard?.let {
                        continuation.resumeWith(Result.success(it))
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWith(Result.failure(error.toException()))
                }

            })

        }
    }

    override suspend fun addCreditCardToUser(creditCard: CreditCard) {
        // Não necessário no seu fluxo atual
        throw UnsupportedOperationException("Não suportado para cartão único por usuário")
    }

    override suspend fun initCreditCard(creditCard: CreditCard) {
        return suspendCoroutine { continuation ->
            creditCardReference
                .setValue(creditCard)
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

    override suspend fun updateCreditCard(creditCard: String): Boolean {
        throw UnsupportedOperationException("Não implementado")
    }

    override suspend fun deleteCreditCard(id: String): Boolean {
        throw UnsupportedOperationException("Não suportado para cartão único por usuário")
    }


    override suspend fun payCreditCard(cardId: String, amount: Float): Boolean {
        return suspendCoroutine { continuation ->
            val creditCardRef = database.reference
                .child("creditCard")
                .child(FirebaseHelper.getUserId())

            creditCardRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        continuation.resumeWith(Result.failure(Exception("Cartão não encontrado")))
                        return
                    }

                    val card = snapshot.getValue(CreditCard::class.java)
                    if (card == null) {
                        continuation.resumeWith(Result.failure(Exception("Erro ao ler dados do cartão")))
                        return
                    }

                    val billAmount = card.balance ?: 0f
                    if (billAmount <= 0f) {
                        continuation.resumeWith(Result.failure(Exception("Nenhuma fatura pendente")))
                        return
                    }

                    if (billAmount != amount) {
                        continuation.resumeWith(Result.failure(Exception("Valor informado não confere com a fatura atual")))
                        return
                    }

                    // 🔹 Atualiza somente o campo balance, preservando o objeto
                    creditCardRef.child("balance").setValue(0f)
                        .addOnSuccessListener {
                            continuation.resumeWith(Result.success(true))
                        }
                        .addOnFailureListener { e ->
                            continuation.resumeWith(Result.failure(e))
                        }
                }

                override fun onCancelled(error: DatabaseError) {
                    continuation.resumeWith(Result.failure(error.toException()))
                }
            })
        }
    }





}
