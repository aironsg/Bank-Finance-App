package dev.airon.bankfinance.data.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import dev.airon.bankfinance.data.model.User
import jakarta.inject.Inject
import kotlin.coroutines.suspendCoroutine

class AuthFirebaseDataSourceImpl @Inject constructor(
    private val auth: FirebaseAuth,
) : AuthFirebaseDataSource {

    override suspend fun login(email: String, password: String) {
        return suspendCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // navegação para tela home
                        continuation.resumeWith(Result.success(Unit))
                    } else {
                        // Login failed
                        task.exception?.let {
                            continuation.resumeWith(Result.failure(it))

                        }
                    }
                }
        }
    }

    override suspend fun register(
        user: User
    ): User {
        return suspendCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(user.email, user.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Registration successful
                        continuation.resumeWith(Result.success(user))

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
        return suspendCoroutine { continuation ->
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Password reset email sent
                        continuation.resumeWith(Result.success(Unit))
                    } else {
                        // Failed to send password reset email
                        task.exception?.let {
                            continuation.resumeWith(Result.failure(it))

                        }
                    }
                }
        }

    }

}