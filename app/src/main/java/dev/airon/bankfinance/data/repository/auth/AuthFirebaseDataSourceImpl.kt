package dev.airon.bankfinance.data.repository.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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

    override suspend fun register(name: String, phone: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Registration successful
                } else {
                    // Registration failed
                }
            }
    }

    override suspend fun recover(
        email: String
    ) {

    }

}