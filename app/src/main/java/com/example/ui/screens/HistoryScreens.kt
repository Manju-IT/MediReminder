package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.HistoryEntity
import com.example.ui.viewmodels.MedicineViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    medicineViewModel: MedicineViewModel
) {
    val history by medicineViewModel.history.collectAsState()
    var selectedRange by remember { mutableStateOf("Today") }

    val filteredHistory = remember(history, selectedRange) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date())

        val calendar = Calendar.getInstance()
        val curTime = calendar.timeInMillis

        history.filter { item ->
            try {
                when (selectedRange) {
                    "Today" -> item.dateStr == todayStr
                    "This Week" -> {
                        val date = sdf.parse(item.dateStr)
                        if (date != null) {
                            val diff = curTime - date.time
                            diff <= 7 * 24 * 60 * 60 * 1000L // 7 Days in ms
                        } else false
                    }
                    "This Month" -> {
                        val date = sdf.parse(item.dateStr)
                        if (date != null) {
                            val diff = curTime - date.time
                            diff <= 30 * 24 * 60 * 60 * 1000L // 30 Days in ms
                        } else false
                    }
                    else -> true
                }
            } catch (e: Exception) {
                true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Medication History",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                )
                Text(
                    "Check daily compliance tracking statistics",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { medicineViewModel.clearAllHistory() }) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Clear logs history", tint = Color.Red)
            }
        }

        // Filter chips today, this week, this month
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val ranges = listOf("Today", "This Week", "This Month")
            ranges.forEach { range ->
                val isSelected = selectedRange == range
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedRange = range },
                    label = { Text(range) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // List display
        if (filteredHistory.isEmpty()) {
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
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = "Empty History",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = "No Medication History Found",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "History logs will record here when alarms trigger or when medicines are marked as Taken.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filteredHistory.forEach { log ->
                    HistoryLogCard(log = log)
                }
            }
        }
    }
}

@Composable
fun HistoryLogCard(log: HistoryEntity) {
    val isTaken = log.status == "TAKEN"
    val isSnoozed = log.status == "SNOOZED"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Circular status icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = when {
                                isTaken -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                isSnoozed -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                                else -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val iconVec = when {
                        isTaken -> Icons.Default.Check
                        isSnoozed -> Icons.Default.Snooze
                        else -> Icons.Default.Close
                    }
                    val iconColor = when {
                        isTaken -> MaterialTheme.colorScheme.primary
                        isSnoozed -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                    Icon(
                        imageVector = iconVec,
                        contentDescription = log.status,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        log.medicineName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Dosage: ${log.dosage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Date: ${log.dateStr} at ${log.timeStr}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Badge status text
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    isTaken -> MaterialTheme.colorScheme.primaryContainer
                    isSnoozed -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer
                }
            ) {
                Text(
                    text = log.status,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = when {
                        isTaken -> MaterialTheme.colorScheme.onPrimaryContainer
                        isSnoozed -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onErrorContainer
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp
                )
            }
        }
    }
}
