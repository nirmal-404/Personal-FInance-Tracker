package com.company.personalfinancetracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.company.personalfinancetracker.BudgetSettingsActivity
import com.company.personalfinancetracker.MainActivity
import com.company.personalfinancetracker.R

class NotificationHelper(private val context: Context) {

    companion object {
        private const val BUDGET_CHANNEL_ID = "BUDGET_CHANNEL_ID"
        private const val TRANSACTION_CHANNEL_ID = "TRANSACTION_CHANNEL_ID"
        private const val BUDGET_NOTIFICATION_ID = 101
        private const val TRANSACTION_NOTIFICATION_ID = 102
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Budget alerts channel
            val budgetChannelName = "Budget Alerts"
            val budgetChannelDescription = "Notifications for budget alerts"
            val budgetImportance = NotificationManager.IMPORTANCE_DEFAULT
            val budgetChannel = NotificationChannel(BUDGET_CHANNEL_ID, budgetChannelName, budgetImportance).apply {
                description = budgetChannelDescription
            }

            // Transaction channel
            val transactionChannelName = "Transaction Updates"
            val transactionChannelDescription = "Notifications for transaction updates"
            val transactionImportance = NotificationManager.IMPORTANCE_DEFAULT
            val transactionChannel = NotificationChannel(TRANSACTION_CHANNEL_ID, transactionChannelName, transactionImportance).apply {
                description = transactionChannelDescription
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(transactionChannel)
        }
    }

    // Show budget notification with title and message
    fun showBudgetNotification(title: String, message: String) {
        val intent = Intent(context, BudgetSettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, BUDGET_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(BUDGET_NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Handle notification permission not granted
            }
        }
    }

    // Show transaction notification
    fun showTransactionNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, TRANSACTION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(TRANSACTION_NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Handle notification permission not granted
            }
        }
    }

    // Show detailed budget notification with spent/budget amounts
    fun showBudgetDetailNotification(spent: Double, budget: Double, percentSpent: Int) {
        val intent = Intent(context, BudgetSettingsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = when {
            percentSpent >= 100 -> "Budget Exceeded!"
            percentSpent >= 80 -> "Budget Warning"
            else -> "Budget Status"
        }

        val message = String.format("You've used %.2f of %.2f (%d%%)", spent, budget, percentSpent)

        val notification = NotificationCompat.Builder(context, BUDGET_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(BUDGET_NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Handle notification permission not granted
            }
        }
    }
}