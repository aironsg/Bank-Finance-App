package dev.airon.bankfinance.data.repository.account

import com.google.firebase.database.FirebaseDatabase
import dev.airon.bankfinance.data.model.Account
import dev.airon.bankfinance.util.FirebaseHelper
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

    override suspend fun getAccountById(id: String): Account? {
        TODO("Not yet implemented")
    }

    override suspend fun updateAccount(account: Account): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAccount(id: String): Boolean {
        TODO("Not yet implemented")
    }
}