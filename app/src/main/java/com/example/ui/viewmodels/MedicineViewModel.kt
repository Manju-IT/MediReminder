package com.example.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MediReminderApplication
import com.example.data.database.HistoryEntity
import com.example.data.database.MedicineEntity
import com.example.data.database.SettingsEntity
import com.example.receiver.AlarmScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicineViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = (application as MediReminderApplication).medicineRepository

    val medicines: StateFlow<List<MedicineEntity>> = repo.getAllMedicines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = repo.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _settings = MutableStateFlow(SettingsEntity())
    val settings: StateFlow<SettingsEntity> = _settings.asStateFlow()

    private val _selectedFilter = MutableStateFlow("All")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            repo.getSettingsFlow().collect { s ->
                if (s != null) {
                    _settings.value = s
                }
            }
        }
    }

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveSettings(settingsEntity: SettingsEntity) {
        viewModelScope.launch {
            repo.saveSettings(settingsEntity)
            _settings.value = settingsEntity
        }
    }

    fun addMedicine(name: String, type: String, dosage: String, reminderTime: String, startDate: String, endDate: String, notes: String) {
        viewModelScope.launch {
            val entity = MedicineEntity(
                name = name,
                type = type,
                dosage = dosage,
                reminderTime = reminderTime,
                startDate = startDate,
                endDate = endDate,
                notes = notes
            )
            val generatedId = repo.insertMedicine(entity)
            
            // Schedule the alarm trigger inside system Receiver
            if (_settings.value.notificationsEnabled) {
                AlarmScheduler.scheduleAlarm(
                    context = getApplication(),
                    medicineId = generatedId,
                    timeStr = reminderTime,
                    medicineName = name,
                    dosage = dosage
                )
            }
        }
    }

    fun updateMedicine(entity: MedicineEntity) {
        viewModelScope.launch {
            repo.updateMedicine(entity)
            // Cancel and Reschedule alarm trigger
            AlarmScheduler.cancelAlarm(getApplication(), entity.id)
            if (_settings.value.notificationsEnabled && !entity.isCompleted) {
                AlarmScheduler.scheduleAlarm(
                    context = getApplication(),
                    medicineId = entity.id,
                    timeStr = entity.reminderTime,
                    medicineName = entity.name,
                    dosage = entity.dosage
                )
            }
        }
    }

    fun deleteMedicine(entity: MedicineEntity) {
        viewModelScope.launch {
            AlarmScheduler.cancelAlarm(getApplication(), entity.id)
            repo.deleteMedicine(entity)
        }
    }

    fun markAsTaken(entity: MedicineEntity, dateStr: String, timeStr: String) {
        viewModelScope.launch {
            // Log in local history
            val historyItem = HistoryEntity(
                medicineId = entity.id,
                medicineName = entity.name,
                dosage = entity.dosage,
                dateStr = dateStr,
                timeStr = timeStr,
                status = "TAKEN"
            )
            repo.insertHistory(historyItem)
        }
    }

    fun toggleComplete(entity: MedicineEntity) {
        viewModelScope.launch {
            val updated = entity.copy(isCompleted = !entity.isCompleted)
            repo.updateMedicine(updated)
            if (updated.isCompleted) {
                AlarmScheduler.cancelAlarm(getApplication(), entity.id)
            } else if (_settings.value.notificationsEnabled) {
                AlarmScheduler.scheduleAlarm(
                    context = getApplication(),
                    medicineId = entity.id,
                    timeStr = entity.reminderTime,
                    medicineName = entity.name,
                    dosage = entity.dosage
                )
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repo.clearHistory()
        }
    }
}
