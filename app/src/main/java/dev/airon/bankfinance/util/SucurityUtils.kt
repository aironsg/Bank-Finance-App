package dev.airon.bankfinance.util

import android.os.Build
import androidx.annotation.RequiresApi
import java.security.MessageDigest
import java.util.Base64
import kotlin.random.Random

object SecurityUtils {
    @RequiresApi(Build.VERSION_CODES.O)
    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val saltedPassword = password + salt
        val digest = md.digest(saltedPassword.toByteArray())
        return Base64.getEncoder().encodeToString(digest)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateSalt(): String {
        val bytes = ByteArray(16)
        Random.nextBytes(bytes)
        return Base64.getEncoder().encodeToString(bytes)
    }
}
