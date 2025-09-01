package dev.airon.bankfinance.data.repository.account

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.domain.model.Account
import dev.airon.bankfinance.core.util.FirebaseHelper
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class AccountRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase
) : AccountRepository {

    private val accountReference = database.reference
        .child("account")
        .child(FirebaseHelper.getUserId())


    override suspend fun initAccount(account: Account) {
        return suspendCoroutine { continuation ->
            accountReference.setValue(account)
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

    override suspend fun getAccount(): Account {

            return suspendCoroutine { continuation ->
                accountReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val account = snapshot.getValue(Account::class.java)
                        account?.let {
                            continuation.resumeWith(Result.success(it))

                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        continuation.resumeWith(Result.failure(error.toException()))
                    }

                })

            }

    }

    override suspend fun updateAccount(account: Account): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAccount(id: String): Boolean {
        TODO("Not yet implemented")
    }
}