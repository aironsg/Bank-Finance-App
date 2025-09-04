package dev.airon.bankfinance.core.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dev.airon.bankfinance.R
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseHelper {
    companion object {
        fun isAuthenticated() = FirebaseAuth.getInstance().currentUser != null

        fun getUserId() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        fun getUser() = FirebaseAuth.getInstance().currentUser

        suspend fun getUserName(): String? = suspendCancellableCoroutine { cont ->
            val uid = getUserId()
            if (uid.isEmpty()) {
                cont.resume(null)
                return@suspendCancellableCoroutine
            }

            val ref = FirebaseDatabase.getInstance().getReference("profile").child(uid)
            ref.child("name").get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cont.resume(task.result?.value as? String)
                } else {
                    cont.resume(null)
                }
            }
        }


        fun getGeneratedId() = FirebaseDatabase.getInstance().reference.push().key ?: ""

        fun validError(error: String): Int {
            return when {
                error.contains("The supplied auth credential is incorrect, malformed or has expired.") -> {
                    R.string.ERROR_INCORRECT_CREDENTIAL
                }

                error.contains("There is no user record corresponding to this identifier.") -> {
                    R.string.ERROR_USER_NOT_FOUND
                }

                error.contains("The email address is already in use by another account.") -> {
                    R.string.ERROR_EMAIL_ALREADY_IN_USE
                }

                error.contains("The email address is badly formatted.") -> {
                    R.string.ERROR_INVALID_EMAIL
                }

                error.contains("The password is invalid or the user does not have a password.") -> {
                    R.string.ERROR_WRONG_PASSWORD
                }

                error.contains("We have blocked all requests from this device due to unusual activity.") -> {
                    R.string.ERROR_TOO_MANY_REQUESTS
                }

                error.contains("The password must be 6 characters long or more.") -> {
                    R.string.ERROR_WEAK_PASSWORD
                }

                error.contains("A network error (such as timeout, interrupted connection or unreachable host) has occurred.") -> {
                    R.string.ERROR_NETWORK_REQUEST_FAILED
                }

                error.contains("The user account has been disabled by an administrator.") -> {
                    R.string.ERROR_INVALID_CREDENTIAL
                }

                else -> R.string.default_error_alert
            }
        }

        fun getPasswordTransaction(onResult: (String, String) -> Unit) {
            val userId = FirebaseHelper.getUserId()
            val userReference = FirebaseDatabase.getInstance()
                .getReference("profile")
                .child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val passwordHash = snapshot.child("passwordTransaction").getValue(String::class.java) ?: ""
                        val salt = snapshot.child("passwordSalt").getValue(String::class.java) ?: ""
                        onResult(passwordHash, salt)
                    } else {
                        onResult("", "")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult("", "")
                }
            })
        }

    }
}