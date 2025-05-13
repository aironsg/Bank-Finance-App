package dev.airon.bankfinance.data.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlin.coroutines.suspendCoroutine

class AuthFirebaseDataSourceImpl(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : AuthFirebaseDataSource {

    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // navegação para tela home
                } else {
                    // Login failed
                }
            }
    }

    override suspend fun register(name: String, phone: String, email: String, password: String) : FirebaseUser {
        return suspendCoroutine {continuation ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Registration successful
                        val user = task.result.user
                        user?.let {
                        continuation.resumeWith(Result.success(it))

                        }

                    } else {
                        // Registration failed
                        task.exception?.let {
                            continuation.resumeWith(Result.failure(it))

                        }
                    }
                }
        }
    }

    override suspend fun recover(
        email: String
    ) {

    }

}