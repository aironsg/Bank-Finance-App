package dev.airon.bankfinance.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CreditCard(
    var id: String = "",
    val number: String = "",
    val account: Account? = null,
    val securityCode: String = "",
    val officialUser: String = "",
    val limit: Float = 0f,
    val validDate: String = "",
    val balance: Float = 0f
) : Parcelable
