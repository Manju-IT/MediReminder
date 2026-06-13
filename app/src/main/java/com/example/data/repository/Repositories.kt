package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest

class AuthRepository(private val dao: RemindersDao) {

    fun getLoggedInUserFlow(): Flow<UserEntity?> {
        return dao.getLoggedInUserFlow()
    }

    suspend fun getLoggedInUser(): UserEntity? {
        return dao.getLoggedInUser()
    }

    suspend fun signUp(name: String, email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val existing = dao.getUserByEmail(email)
            if (existing != null) {
                return@withContext Result.failure(Exception("An account with this email already exists"))
            }
            val hash = hashPassword(password)
            val newUser = UserEntity(
                email = email,
                fullName = name,
                passwordHash = hash,
                isLoggedIn = true
            )
            // Logout any previous users first to ensure single logged-in user session
            dao.logOutAllUsers()
            dao.insertUser(newUser)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val user = dao.getUserByEmail(email)
                ?: return@withContext Result.failure(Exception("Invalid email or password"))
            
            val hash = hashPassword(password)
            if (user.passwordHash != hash) {
                return@withContext Result.failure(Exception("Invalid email or password"))
            }

            dao.logOutAllUsers()
            val loggedInUser = user.copy(isLoggedIn = true)
            dao.insertUser(loggedInUser)
            Result.success(loggedInUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val user = dao.getUserByEmail(email)
                ?: return@withContext Result.failure(Exception("No account found with this email"))
            
            // Simulates sending reset email
            Result.success("Password reset instructions sent successfully to $email")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        dao.logOutAllUsers()
    }

    suspend fun updateUser(user: UserEntity) = withContext(Dispatchers.IO) {
        dao.updateUser(user)
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

class MedicineRepository(private val dao: RemindersDao) {

    fun getAllMedicines(): Flow<List<MedicineEntity>> = dao.getAllMedicinesFlow()

    suspend fun getMedicineById(id: Long): MedicineEntity? = withContext(Dispatchers.IO) {
        dao.getMedicineById(id)
    }

    suspend fun insertMedicine(medicine: MedicineEntity): Long = withContext(Dispatchers.IO) {
        dao.insertMedicine(medicine)
    }

    suspend fun updateMedicine(medicine: MedicineEntity) = withContext(Dispatchers.IO) {
        dao.updateMedicine(medicine)
    }

    suspend fun deleteMedicine(medicine: MedicineEntity) = withContext(Dispatchers.IO) {
        dao.deleteMedicine(medicine)
    }

    suspend fun insertHistory(history: HistoryEntity) = withContext(Dispatchers.IO) {
        dao.insertHistory(history)
    }

    fun getAllHistory(): Flow<List<HistoryEntity>> = dao.getAllHistoryFlow()

    suspend fun deleteHistory(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteHistoryById(id)
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        dao.clearAllHistory()
    }

    fun getSettingsFlow(): Flow<SettingsEntity?> = dao.getSettingsFlow()

    suspend fun getSettings(): SettingsEntity? = withContext(Dispatchers.IO) {
        val current = dao.getSettings()
        if (current == null) {
            val defaultSettings = SettingsEntity()
            dao.insertSettings(defaultSettings)
            defaultSettings
        } else {
            current
        }
    }

    suspend fun saveSettings(settings: SettingsEntity) = withContext(Dispatchers.IO) {
        dao.insertSettings(settings)
    }
}
