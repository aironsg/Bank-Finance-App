package dev.airon.bankfinance.util

import com.google.firebase.auth.FirebaseAuth
import dev.airon.bankfinance.R

class FirebaseHelper {
    companion object {
        fun isAuthenticated() = FirebaseAuth.getInstance().currentUser != null

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
    }
}