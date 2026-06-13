package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MediReminderApplication
import com.example.ui.screens.alarm.AlarmActivity

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        
        if (action == "android.intent.action.BOOT_COMPLETED") {
            // Custom boot-rescheduling logic can go here in production
            return
        }

        if (action == "com.example.ACTION_MEDICINE_REMINDER") {
            val medicineId = intent.getLongExtra("MEDICINE_ID", -1L)
            val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"
            val dosage = intent.getStringExtra("MEDICINE_DOSAGE") ?: "1 Dose"
            val timeStr = intent.getStringExtra("MEDICINE_TIME") ?: ""

            if (medicineId == -1L) return

            // 1. Launch full screen AlarmActivity
            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("MEDICINE_ID", medicineId)
                putExtra("MEDICINE_NAME", medicineName)
                putExtra("MEDICINE_DOSAGE", dosage)
                putExtra("MEDICINE_TIME", timeStr)
            }
            context.startActivity(alarmIntent)

            // 2. Show notification
            showNotification(context, medicineId, medicineName, dosage)
        }
    }

    private fun showNotification(context: Context, medicineId: Long, name: String, dosage: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Intent to launch the main activity
        val mainIntent = Intent(context, com.example.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            medicineId.toInt() * 10,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Mark Taken
        val takenIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("MEDICINE_ID", medicineId)
            putExtra("ACTION_TYPE", "TAKEN")
            putExtra("MEDICINE_NAME", name)
            putExtra("MEDICINE_DOSAGE", dosage)
        }
        val takenPendingIntent = PendingIntent.getActivity(
            context,
            medicineId.toInt() * 10 + 1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Snooze
        val snoozeIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("MEDICINE_ID", medicineId)
            putExtra("ACTION_TYPE", "SNOOZE")
            putExtra("MEDICINE_NAME", name)
            putExtra("MEDICINE_DOSAGE", dosage)
        }
        val snoozePendingIntent = PendingIntent.getActivity(
            context,
            medicineId.toInt() * 10 + 2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MediReminderApplication.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Time to take your medicine")
            .setContentText("$name - $dosage")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(mainPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.checkbox_on_background, "Taken", takenPendingIntent)
            .addAction(android.R.drawable.ic_menu_recent_history, "Snooze", snoozePendingIntent)
            .build()

        notificationManager.notify(medicineId.toInt(), notification)
    }
}
