package com.example.ui

import android.text.format.DateUtils
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

object TimeFormatter {

    fun getRelativeTime(timestamp: Long, currentTime: Long = System.currentTimeMillis()): String {
        val diff = currentTime - timestamp
        return when {
            diff < 60L * 1000 -> "Just now"
            diff < 60L * 60 * 1000 -> {
                val minutes = (diff / (60L * 1000)).toInt()
                if (minutes <= 1) "1 min ago" else "$minutes min ago"
            }
            diff < 24L * 60 * 60 * 1000 -> {
                val hours = (diff / (60L * 60 * 1000)).toInt()
                if (hours <= 1) "1 hr ago" else "$hours hr ago"
            }
            else -> {
                DateUtils.getRelativeTimeSpanString(
                    timestamp,
                    currentTime,
                    DateUtils.DAY_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString()
            }
        }
    }

    fun getDeadlineCountdown(deadline: Long, currentTime: Long = System.currentTimeMillis()): String {
        val diff = deadline - currentTime
        val isOverdue = diff < 0
        val absDiff = abs(diff)

        val totalHours = (absDiff / (1000L * 60 * 60)).toInt()
        val days = totalHours / 24
        val hours = totalHours % 24
        return if (isOverdue) {
            if (totalHours < 1) "Less than an hr overdue"
            else if (totalHours < 24) "$totalHours hr overdue"
            else if (hours == 0) "$days days overdue"
            else "$days days $hours hr overdue"
        } else {
            if (totalHours < 1) "Less than an hr left"
            else if (totalHours < 48) "$totalHours hr left"
            else "$days days left"
        }
    }

    /**
     * Thread-safe date formatting using java.time (API 26+).
     * For API < 26, falls back to thread-local SimpleDateFormat.
     */
    fun formatStaticDate(timestamp: Long): String {
        return try {
            val instant = java.time.Instant.ofEpochMilli(timestamp)
            val zdt = java.time.ZonedDateTime.ofInstant(instant, ZoneId.systemDefault())
            zdt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } catch (e: Exception) {
            @Suppress("DEPRECATION")
            DateFormatThreadLocal.get().format(Date(timestamp))
        }
    }
}

/**
 * Thread-local SimpleDateFormat for backward compatibility (API < 26).
 */
private object DateFormatThreadLocal {
    private val tl = ThreadLocal.withInitial {
        java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }

    fun get(): java.text.SimpleDateFormat = tl.get()
}

/**
 * Currency formatting utility — centralized to avoid duplication.
 * Usage: "$45.00" → CurrencyUtils.format(45.0)
 * Rounds to 2 decimal places. Negative amounts prefixed with "-$".
 */
object CurrencyUtils {
    fun format(amount: Double): String {
        val absAmount = kotlin.math.abs(amount)
        val sign = if (amount < 0) "-" else ""
        val rounded = kotlin.math.round(absAmount * 100.0) / 100.0
        return if (rounded == rounded.toLong().toDouble()) {
            "$${rounded.toLong()}.00"
        } else {
            "$${"%.2f".format(rounded)}"
        }
    }
}
