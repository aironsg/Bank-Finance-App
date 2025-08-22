package dev.airon.bankfinance.data.repository.creditCard

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.data.model.Account
import dev.airon.bankfinance.data.model.CreditCard
import dev.airon.bankfinance.util.FirebaseHelper
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class CreditCardRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase) : CreditCardRepository {

    private val creditCardReference = database.reference
        .child("creditCard")
        .child(FirebaseHelper.getUserId())


    override suspend fun getAllCreditCards(

    ): List<String> {
        TODO("Not yet implemented")
    }

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
        TODO("Not yet implemented")
    }

    override suspend fun initCreditCard(creditCard: CreditCard) {
        return suspendCoroutine { continuation ->
            creditCardReference
                .child(FirebaseHelper.getUserId())
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
        TODO("Not yet implemented")
    }

    override suspend fun deleteCreditCard(id: String): Boolean {
        TODO("Not yet implemented")
    }


}