package com.example.ui.screens.alarm

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MediReminderApplication
import com.example.data.database.HistoryEntity
import com.example.receiver.AlarmScheduler
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AlarmActivity : ComponentActivity() {

    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val medicineId = intent.getLongExtra("MEDICINE_ID", -1L)
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicine"
        val dosage = intent.getStringExtra("MEDICINE_DOSAGE") ?: "1 Dose"
        val actionType = intent.getStringExtra("ACTION_TYPE")

        // 1. Check if direct tray actions are called, execute and terminate
        if (actionType != null && medicineId != -1L) {
            handleNotificationAction(medicineId, medicineName, dosage, actionType)
            finish()
            return
        }

        if (medicineId == -1L) {
            finish()
            return
        }

        // Start premium persistent haptic alarm feedback
        startVibrator()

        setContent {
            MyApplicationTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                ) {
                    AlarmScreenContent(
                        medicineName = medicineName,
                        dosage = dosage,
                        onTaken = {
                            executeTakeAction(medicineId, medicineName, dosage)
                            stopVibrator()
                            cancelSystemNotification(medicineId)
                            finish()
                        },
                        onSnooze = { minutes ->
                            executeSnoozeAction(medicineId, medicineName, dosage, minutes)
                            stopVibrator()
                            cancelSystemNotification(medicineId)
                            finish()
                        },
                        onDismiss = {
                            executeDismissAction(medicineId, medicineName, dosage)
                            stopVibrator()
                            cancelSystemNotification(medicineId)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun startVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } catch (e: Exception) {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val pattern = longArrayOf(0, 400, 300, 400, 300)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopVibrator() {
        vibrator?.cancel()
        vibrator = null
    }

    private fun cancelSystemNotification(id: Long) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id.toInt())
    }

    override fun onDestroy() {
        stopVibrator()
        super.onDestroy()
    }

    private fun handleNotificationAction(id: Long, name: String, dosage: String, action: String) {
        cancelSystemNotification(id)
        if (action == "TAKEN") {
            executeTakeAction(id, name, dosage)
        } else if (action == "SNOOZE") {
            executeSnoozeAction(id, name, dosage, 5)
        }
    }

    private fun executeTakeAction(id: Long, name: String, dosage: String) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)

        CoroutineScope(Dispatchers.IO).launch {
            val repo = MediReminderApplication.get().medicineRepository
            repo.insertHistory(
                HistoryEntity(
                    medicineId = id,
                    medicineName = name,
                    dosage = dosage,
                    dateStr = dateStr,
                    timeStr = timeStr,
                    status = "TAKEN"
                )
            )
        }
        Toast.makeText(this, "Marked as Taken. Good health!", Toast.LENGTH_SHORT).show()
    }

    private fun executeSnoozeAction(id: Long, name: String, dosage: String, minutes: Int) {
        // Calculate target snooze time
        val cal = Calendar.getInstance().apply {
            add(Calendar.MINUTE, minutes)
        }
        val targetTimeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(cal.time)

        // Schedule next exact snooze alarm
        AlarmScheduler.scheduleAlarm(this, id, targetTimeStr, name, dosage)

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)

        CoroutineScope(Dispatchers.IO).launch {
            val repo = MediReminderApplication.get().medicineRepository
            repo.insertHistory(
                HistoryEntity(
                    medicineId = id,
                    medicineName = name,
                    dosage = dosage,
                    dateStr = dateStr,
                    timeStr = timeStr,
                    status = "SNOOZED"
                )
            )
        }
        Toast.makeText(this, "Snoozed for $minutes minutes", Toast.LENGTH_SHORT).show()
    }

    private fun executeDismissAction(id: Long, name: String, dosage: String) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)

        CoroutineScope(Dispatchers.IO).launch {
            val repo = MediReminderApplication.get().medicineRepository
            repo.insertHistory(
                HistoryEntity(
                    medicineId = id,
                    medicineName = name,
                    dosage = dosage,
                    dateStr = dateStr,
                    timeStr = timeStr,
                    status = "MISSED"
                )
            )
        }
        Toast.makeText(this, "Dismissed Medication Alarm", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun AlarmScreenContent(
    medicineName: String,
    dosage: String,
    onTaken: () -> Unit,
    onSnooze: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Pulsing animation for alarm icon
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Alarms Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(scalePulse)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = "Alarm Clock Icon",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(56.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "MEDICINE ALARM",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                ),
                color = MaterialTheme.colorScheme.error
            )
        }

        // Medicine Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = medicineName,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = dosage,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Time to consume your prescription. Please maintain your routine strictly for optimal recovery.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Action Buttons Row
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onTaken,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Take Done Icon")
                    Text("MARK AS TAKEN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilledTonalButton(
                    onClick = { onSnooze(5) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = "Snooze 5m Icon",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Snooze 5m", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = { onSnooze(10) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = "Snooze 10m Icon",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Snooze 10m", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss Icon")
                    Text("DISMISS ALERT", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
