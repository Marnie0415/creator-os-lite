package com.example.ui

import android.text.format.DateUtils
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

object TimeFormatter {

    fun getRelativeTime(timestamp: Long, currentTime: Long = System.currentTimeMillis()): String {
        val diff = currentTime - timestamp
        return if (diff < 60L * 1000) {
            "Just now"
        } else if (diff < 24L * 60 * 60 * 1000) {
            val hours = (diff / (60L * 60 * 1000)).toInt()
            if (hours <= 0) "1 hr ago" else "$hours hr ago"
        } else {
            DateUtils.getRelativeTimeSpanString(
                timestamp,
                currentTime,
                DateUtils.DAY_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()
        }
    }

    fun getDeadlineCountdown(deadline: Long, currentTime: Long = System.currentTimeMillis()): String {
        val diff = deadline - currentTime
        val isOverdue = diff < 0
        val absDiff = abs(diff)

        val hours = (absDiff / (1000L * 60 * 60)).toInt()
        return if (isOverdue) {
            if (hours < 24) "$hours hr overdue" else "${hours / 24} days overdue"
        } else {
            if (hours < 1) "Less than an hr left"
            else if (hours < 48) "$hours hr left"
            else "${hours / 24} days left"
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
 */
object CurrencyUtils {
    fun format(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) {
            "$${amount.toLong()}.00"
        } else {
            "$${"%.2f".format(amount)}"
        }
    }
}
