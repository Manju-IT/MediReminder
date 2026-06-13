package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.database.AppDatabase
import com.example.data.repository.AuthRepository
import com.example.data.repository.MedicineRepository

class MediReminderApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var medicineRepository: MedicineRepository
        private set

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        // Initialize SQLite AppDatabase
        database = AppDatabase.getDatabase(this)
        val dao = database.remindersDao()

        // Create Repository singletons
        authRepository = AuthRepository(dao)
        medicineRepository = MedicineRepository(dao)

        // Create Notification Channel for medicine alarms
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Medicine Reminders"
            val descriptionText = "Displays medicine intake alarms and notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setBypassDnd(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private var INSTANCE: MediReminderApplication? = null
        const val CHANNEL_ID = "medireminder_alarms_channel"

        fun get(): MediReminderApplication {
            return INSTANCE ?: throw IllegalStateException("Application class not initialized yet")
        }
    }
}
