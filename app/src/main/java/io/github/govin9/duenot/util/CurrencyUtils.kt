package io.github.govin9.duenot.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyUtils {
    fun formatAmount(amount: Double): String {
        val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
        
        return if (amount % 1.0 == 0.0) {
            // Whole number
            numberFormat.minimumFractionDigits = 0
            numberFormat.maximumFractionDigits = 0
            numberFormat.format(amount)
        } else {
            // Fractional part exists - enforce 2 decimal places
            numberFormat.minimumFractionDigits = 2
            numberFormat.maximumFractionDigits = 2
            numberFormat.format(amount)
        }
    }
}
