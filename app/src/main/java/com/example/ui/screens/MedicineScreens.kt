package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.MedicineEntity
import com.example.ui.viewmodels.MedicineViewModel
import java.text.SimpleDateFormat
import java.util.*

// ==========================================
//               ADD MEDICINE SCREEN
// ==========================================
@Composable
fun AddMedicineScreen(
    medicineViewModel: MedicineViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Tablet") }
    var dosage by remember { mutableStateOf("") }
    var reminderTime by remember { mutableStateOf("08:00") } // HH:mm format
    var startDate by remember { mutableStateOf("") } // yyyy-MM-dd
    var endDate by remember { mutableStateOf("") } // yyyy-MM-dd
    var notes by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var dosageError by remember { mutableStateOf<String?>(null) }
    var timeError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    // Use current date as default format
    LaunchedEffect(Unit) {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = formatter.format(Date())
        startDate = today
        // Set default end date to 2 weeks from now
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, 2)
        endDate = formatter.format(calendar.time)
    }

    val medicineTypes = listOf("Tablet", "Syrup", "Capsule", "Injection")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toolbar header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back")
            }
            Text(
                text = "Add Medication",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Medicine Name
        Text("Medicine Name", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                nameError = null
            },
            placeholder = { Text("e.g. Paracetamol") },
            leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null) },
            isError = nameError != null,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (nameError != null) {
            Text(nameError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Medicine Type (Tablet, Syrup, Capsule, etc.)
        Text("Prescription Type", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            medicineTypes.forEach { type ->
                val isSelected = selectedType == type
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable { selectedType = type }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        type,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Dosage field (1 Tablet, 5ml, etc.)
        Text("Dosage prescription", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = dosage,
            onValueChange = {
                dosage = it
                dosageError = null
            },
            placeholder = { Text("e.g. 1 Tablet, 10 ml, 1 Capsule") },
            leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null) },
            isError = dosageError != null,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (dosageError != null) {
            Text(dosageError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Time picker field HH:mm
        Text("Daily Alarm Time (24h clock format)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = reminderTime,
            onValueChange = {
                reminderTime = it
                timeError = null
            },
            placeholder = { Text("e.g. 08:30, 22:00") },
            leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null) },
            isError = timeError != null,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        if (timeError != null) {
            Text(timeError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Star Date / End Date
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Start Date", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = startDate,
                    onValueChange = {
                        startDate = it
                        dateError = null
                    },
                    placeholder = { Text("yyyy-MM-dd") },
                    leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text("End Date", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = endDate,
                    onValueChange = {
                        endDate = it
                        dateError = null
                    },
                    placeholder = { Text("yyyy-MM-dd") },
                    leadingIcon = { Icon(Icons.Default.EventNote, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        if (dateError != null) {
            Text(dateError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        // Notes description
        Text("Additional Notes (Optional)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            placeholder = { Text("Take with warm water before meals.") },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Saving Submit
        Button(
            onClick = {
                var hasError = false
                if (name.isBlank()) {
                    nameError = "Medicine name is required"
                    hasError = true
                }
                if (dosage.isBlank()) {
                    dosageError = "Dosage information is required"
                    hasError = true
                }

                // Standard hour/minute format check
                val regex = Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
                if (!regex.matches(reminderTime)) {
                    timeError = "Please use valid 24h clock HH:mm format"
                    hasError = true
                }

                if (!hasError) {
                    medicineViewModel.addMedicine(
                        name = name,
                        type = selectedType,
                        dosage = dosage,
                        reminderTime = reminderTime,
                        startDate = startDate,
                        endDate = endDate,
                        notes = notes
                    )
                    Toast.makeText(context, "Medicine Schedule Configured!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("SAVE MEDICATION REMINDER", fontWeight = FontWeight.Black)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}


// ==========================================
//               MEDICINE LIST SCREEN
// ==========================================
@Composable
fun MedicineListScreen(
    medicineViewModel: MedicineViewModel,
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToDetails: (Long) -> Unit
) {
    val medicines by medicineViewModel.medicines.collectAsState()
    val filter by medicineViewModel.selectedFilter.collectAsState()
    val searchQuery by medicineViewModel.searchQuery.collectAsState()

    val filteredList = remember(medicines, filter, searchQuery) {
        medicines.filter { med ->
            // Search filter
            val matchesSearch = med.name.contains(searchQuery, ignoreCase = true) ||
                    med.type.contains(searchQuery, ignoreCase = true)

            // Category tag filter
            val matchesFilter = when (filter) {
                "Completed" -> med.isCompleted
                "Active" -> !med.isCompleted
                else -> true
            }

            matchesSearch && matchesFilter
        }
    }

    val filterChips = listOf("All", "Active", "Completed")

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddMedicine,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add medicine schedule")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "My Medicine Cabinet",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { medicineViewModel.setSearchQuery(it) },
                placeholder = { Text("Search prescription name or type...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { medicineViewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                }
            )

            // Categorization Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterChips.forEach { chipName ->
                    val isSelected = filter == chipName
                    FilterChip(
                        selected = isSelected,
                        onClick = { medicineViewModel.setFilter(chipName) },
                        label = { Text(chipName) },
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }

            // ListView
            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inbox,
                            contentDescription = "Empty Box",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(72.dp)
                        )
                        Text(
                            text = "No Medication Scheduled",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Add medicines using the '+' panel button below to receive triggers.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(4.dp))
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    filteredList.forEach { medicine ->
                        MedicineItemCard(
                            medicine = medicine,
                            onCardClick = { onNavigateToDetails(medicine.id) },
                            onMarkComplete = { medicineViewModel.toggleComplete(medicine) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineItemCard(
    medicine: MedicineEntity,
    onCardClick: () -> Unit,
    onMarkComplete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = if (medicine.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Customized Type icon background circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVec = when (medicine.type) {
                        "Syrup" -> Icons.Default.Liquor
                        "Capsule" -> Icons.Default.HourglassEmpty
                        "Injection" -> Icons.Default.Vaccines
                        else -> Icons.Default.Medication
                    }
                    Icon(
                        imageVector = iconVec,
                        contentDescription = medicine.type,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        medicine.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (medicine.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "${medicine.type} • ${medicine.dosage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Trigger time",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Daily at ${medicine.reminderTime}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Quick Mark Action
            IconButton(onClick = onMarkComplete) {
                Icon(
                    imageVector = if (medicine.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Complete toggler",
                    tint = if (medicine.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


// ==========================================
//               MEDICINE DETAILS SCREEN
// ==========================================
@Composable
fun MedicineDetailsScreen(
    medicineId: Long,
    medicineViewModel: MedicineViewModel,
    onNavigateBack: () -> Unit
) {
    val medicines by medicineViewModel.medicines.collectAsState()
    val history by medicineViewModel.history.collectAsState()
    val context = LocalContext.current

    val medicine = remember(medicines, medicineId) {
        medicines.find { it.id == medicineId }
    }

    val intakeHistory = remember(history, medicineId) {
        history.filter { it.medicineId == medicineId }
    }

    if (medicine == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CircularProgressIndicator()
                Text("Searching schedule database...")
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back")
                }
                Text(
                    text = "Medication Details",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            // Delete command
            IconButton(
                onClick = {
                    medicineViewModel.deleteMedicine(medicine)
                    Toast.makeText(context, "${medicine.name} Alert deleted successfully", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete alarm schedule", tint = Color.Red)
            }
        }

        // Card Panel Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Medicine Type Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVec = when (medicine.type) {
                        "Syrup" -> Icons.Default.Liquor
                        "Capsule" -> Icons.Default.HourglassEmpty
                        "Injection" -> Icons.Default.Vaccines
                        else -> Icons.Default.Medication
                    }
                    Icon(
                        imageVector = iconVec,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "${medicine.type} • ${medicine.dosage}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Metadata grid details
        Text("Information Grid", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailItemCard(
                label = "Trigger Time",
                value = medicine.reminderTime,
                icon = Icons.Default.Alarm,
                modifier = Modifier.weight(1f)
            )

            DetailItemCard(
                label = "Current State",
                value = if (medicine.isCompleted) "Completed" else "Active Reminders",
                icon = if (medicine.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DetailItemCard(
                label = "Start Date",
                value = medicine.startDate,
                icon = Icons.Default.Event,
                modifier = Modifier.weight(1f)
            )

            DetailItemCard(
                label = "End Date",
                value = medicine.endDate,
                icon = Icons.Default.EventNote,
                modifier = Modifier.weight(1f)
            )
        }

        // Additional notes card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Doctor Notes & Guidelines",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = medicine.notes.ifBlank { "No customized doctor notes provided for this medicine alarm." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }

        // Intake Logs
        Text(
            text = "Medication History logs",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        if (intakeHistory.isEmpty()) {
            Text(
                "No logs recorded yet. Daily alarms will populate histories here.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                intakeHistory.forEach { h ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (h.status == "TAKEN") Icons.Default.TaskAlt else Icons.Default.ErrorOutline,
                                contentDescription = h.status,
                                tint = if (h.status == "TAKEN") MaterialTheme.colorScheme.primary else Color.Red
                            )
                            Column {
                                Text(
                                    text = h.status,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Date: ${h.dateStr}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "Time: ${h.timeStr}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DetailItemCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
