package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val fullName: String,
    val passwordHash: String,
    val profilePhotoUri: String? = null,
    val isLoggedIn: Boolean = false
)

@Entity(tableName = "medicines")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String, // Tablet, Syrup, Capsule, Injection
    val dosage: String,
    val reminderTime: String, // HH:mm e.g. "08:00"
    val startDate: String, // yyyy-MM-dd
    val endDate: String, // yyyy-MM-dd
    val notes: String = "",
    val isCompleted: Boolean = false
)

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medicineId: Long,
    val medicineName: String,
    val dosage: String,
    val dateStr: String, // yyyy-MM-dd
    val timeStr: String, // HH:mm
    val status: String // TAKEN, MISSED, SNOOZED, DISMISSED
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 1,
    val notificationsEnabled: Boolean = true,
    val alarmSound: String = "Default",
    val darkModeState: String = "System", // System, Light, Dark
    val reminderVolume: Float = 0.8f,
    val snoozeDurationMinutes: Int = 5,
    val language: String = "English",
    val onboardingCompleted: Boolean = false
)
