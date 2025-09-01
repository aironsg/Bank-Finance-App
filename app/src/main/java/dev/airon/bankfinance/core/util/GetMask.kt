package dev.airon.bankfinance.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GetMask {

    companion object {

        const val DAY_MONTH = 0
        const val DAY_MONTH_YEAR = 1
        const val HOUR_MINUTE = 2
        const val DAY_MONTH_YEAR_HOUR_MINUTE = 3

        fun getFormatedDate(date: Long, type: Int): String {
            val locale = Locale("pt", "BR")
            val timeZone = TimeZone.getDefault() // Usa o fuso horário do dispositivo

            val pattern = when (type) {
                DAY_MONTH_YEAR -> "dd/MM/yyyy"
                HOUR_MINUTE -> "HH:mm"
                DAY_MONTH_YEAR_HOUR_MINUTE -> "dd/MM/yyyy HH:mm"
                DAY_MONTH -> "dd/MM"
                else -> return "Erro"
            }

            val sdf = SimpleDateFormat(pattern, locale)
            sdf.timeZone = timeZone

            return sdf.format(Date(date))
        }

        fun getFormatedValue(value: Any): String {
            val nf: NumberFormat = DecimalFormat(
                "#,##0.00", DecimalFormatSymbols(Locale("pt", "BR"))
            )
            return nf.format(value)
        }
    }


}