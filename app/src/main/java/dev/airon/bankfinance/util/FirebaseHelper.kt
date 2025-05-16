package dev.airon.bankfinance.util

import com.google.firebase.auth.FirebaseAuth

class FirebaseHelper {
    companion object{
        fun isAuthenticated() = FirebaseAuth.getInstance().currentUser != null
    }
}