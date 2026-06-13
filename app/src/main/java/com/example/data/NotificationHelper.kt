package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.dashboard.RiskItem

/**
 * Manages local notifications for risk alerts.
 * Checks are performed when the app opens (no background service).
 */
object NotificationHelper {
    private const val CHANNEL_ID = "creator_os_risk_alerts"
    private const val NOTIFICATION_BASE_ID = 1000

    /**
     * Create the notification channel (required for API 26+).
     * Call once from Application.onCreate().
     */
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            }
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
    }

    /**
     * Post notifications for active risks.
     * Debounces by checking SharedPreferences to avoid re-notifying.
     */
    fun postRiskNotifications(context: Context, risks: List<RiskItem>) {
        val prefs = context.getSharedPreferences("notification_debounce", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        var notificationId = NOTIFICATION_BASE_ID

        for (risk in risks) {
            val debounceKey = "notified_${risk.id}"
            val lastNotified = prefs.getLong(debounceKey, 0L)
            val now = System.currentTimeMillis()

            // Only notify if we haven't sent one for this risk in the last 24 hours
            if (now - lastNotified < 24L * 60 * 60 * 1000) continue

            val title: String
            val priority: Int
            when (risk.severity) {
                com.example.dashboard.RiskSeverity.CRITICAL -> {
                    title = context.getString(R.string.notification_overdue_title)
                    priority = NotificationCompat.PRIORITY_MAX
                }
                com.example.dashboard.RiskSeverity.HIGH -> {
                    title = context.getString(R.string.notification_expiring_title)
                    priority = NotificationCompat.PRIORITY_HIGH
                }
                com.example.dashboard.RiskSeverity.MEDIUM -> {
                    title = context.getString(R.string.notification_ghosted_title)
                    priority = NotificationCompat.PRIORITY_DEFAULT
                }
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(risk.details)
                .setPriority(priority)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            try {
                NotificationManagerCompat.from(context).notify(notificationId++, notification)
                editor.putLong(debounceKey, now)
            } catch (e: SecurityException) {
                // Permission not granted — silently skip
            }
        }
        editor.apply()
    }
}
