package dev.airon.bankfinance.data.repository.recharge

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.domain.model.Recharge
import dev.airon.bankfinance.core.util.FirebaseHelper
import dev.airon.bankfinance.domain.repository.recharge.RechargeRepository
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine


class RechargeRepositoryImpl @Inject constructor(
    database: FirebaseDatabase
) : RechargeRepository {


    private val rechargeReferences = database.reference
        .child("recharge")
        .child(FirebaseHelper.getUserId())


    override suspend fun saveRecharge(recharge: Recharge): Recharge {
        return suspendCoroutine { continuation ->
            // Garantir que data e hora sejam definidas antes de salvar
            val currentTime = System.currentTimeMillis()
            recharge.date = currentTime
            recharge.hour = currentTime

            rechargeReferences
                .child(recharge.id)
                .setValue(recharge)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        continuation.resumeWith(Result.success(recharge))
                    } else {
                        task.exception?.let {
                            continuation.resumeWith(Result.failure(it))
                        }
                    }
                }
        }
    }


    override suspend fun getRecharge(id:String): Recharge {
        return suspendCoroutine { continuation ->
            rechargeReferences.child(id).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val recharge = snapshot.getValue(Recharge::class.java)
                    recharge?.let {
                        continuation.resumeWith(Result.success(it))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().let {
                        continuation.resumeWith(Result.failure(it))
                    }
                }
            })
        }
    }

    override suspend fun getPasswordTransaction(): String {
        return suspendCoroutine { continuation ->
            val userId = FirebaseHelper.getUserId()
            val passwordRef = FirebaseDatabase.getInstance()
                .getReference("profile")
                .child(userId)
                .child("passwordTransaction")

            passwordRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val password = snapshot.getValue(String::class.java) ?: ""
                    continuation.resumeWith(Result.success(password))
                }

                override fun onCancelled(error: DatabaseError) {
                    error.toException().let {
                        continuation.resumeWith(Result.failure(it))
                    }
                }
            })
        }

    }


}