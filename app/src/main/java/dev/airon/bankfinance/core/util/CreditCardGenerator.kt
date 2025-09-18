package dev.airon.bankfinance.core.util

import kotlin.random.Random

object CreditCardGenerator {

    fun generateNumber(): String {
        val numero = (1..16)
            .map { Random.nextInt(0, 10) }
            .joinToString("")
        return numero.chunked(4).joinToString(" ")
    }

    fun generateValidDate(): String {
        val mes = Random.nextInt(1, 13).toString().padStart(2, '0')
        val ano = Random.nextInt(25, 31) // validade entre 2025 e 2030
        return "$mes/$ano"
    }

    fun generateSecurityCode(): String {
        return (100..999).random().toString()
    }

    fun generateLimit(): Float {
        return listOf(500f, 1000f, 2000f, 3000f).random()
    }
}
