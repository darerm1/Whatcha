package com.darerm1.whatcha.utils

import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

object RatingFormatter {
    fun formatPersonalRating(rating: Float): String {
        val rounded = rating.roundToInt().toFloat()
        return if (abs(rating - rounded) < 0.05f) {
            rounded.toInt().toString()
        } else {
            String.format(Locale.US, "%.1f", rating)
        }
    }
}
