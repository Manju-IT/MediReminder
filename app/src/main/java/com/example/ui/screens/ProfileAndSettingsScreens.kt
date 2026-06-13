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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.SettingsEntity
import com.example.data.database.UserEntity
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.MedicineViewModel

// ==========================================
//               PROFILE SCREEN
// ==========================================
@Composable
fun ProfileScreen(
    loggedInUser: UserEntity?,
    authViewModel: AuthViewModel,
    onNavigateToSettings: () -> Unit,
    onLogoutFinished: () -> Unit
) {
    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var nameField by remember { mutableStateOf(loggedInUser?.fullName ?: "") }

    val successMessage by authViewModel.successMessage.collectAsState()

    LaunchedEffect(loggedInUser) {
        if (loggedInUser != null) {
            nameField = loggedInUser.fullName
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null && successMessage!!.contains("Profile")) {
            Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
            authViewModel.clearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "My Profile",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
            )

            IconButton(onClick = onNavigateToSettings) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Open Settings")
            }
        }

        // Photo circle placeholder
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(48.dp)
            )
        }

        // Profile Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isEditing) {
                    OutlinedTextField(
                        value = nameField,
                        onValueChange = { nameField = it },
                        label = { Text("Display Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ProfileRow(label = "Full Name", value = loggedInUser?.fullName ?: "Reminders User")
                }

                Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                ProfileRow(label = "Account Email", value = loggedInUser?.email ?: "user@example.com")
            }
        }

        // Edit Profile Trigger Bottom
        Button(
            onClick = {
                if (isEditing) {
                    if (nameField.isNotBlank()) {
                        authViewModel.updateProfile(nameField, loggedInUser?.email ?: "")
                        isEditing = false
                    } else {
                        Toast.makeText(context, "Full Name is required", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    isEditing = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(if (isEditing) "SAVE UPDATES" else "EDIT ACCOUNT PROFILE", fontWeight = FontWeight.Bold)
        }

        // Settings Quick Navigation card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToSettings() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Notification & Sound Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Snooze timings, volume, dark theme", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        // Active Session Logout Button
        Button(
            onClick = {
                authViewModel.logout()
                onLogoutFinished()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout account session")
                Text("SECURELY SHUT DOWN SESSION", fontWeight = FontWeight.Black)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}


// ==========================================
//               SETTINGS SCREEN
// ==========================================
@Composable
fun SettingsScreen(
    medicineViewModel: MedicineViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val settings by medicineViewModel.settings.collectAsState()

    var isDropdownOpen by remember { mutableStateOf(false) }
    var isLangDropdownOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Toolbar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back")
            }
            Text(
                "Preferences Settings",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        // 1. Notifications Switch
        SettingsSectionTitle("Alarms Trigger Notifications")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Enable Medication Alarms", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Triggers vibration alarms and system alerts", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Switch(
                    checked = settings.notificationsEnabled,
                    onCheckedChange = {
                        medicineViewModel.saveSettings(settings.copy(notificationsEnabled = it))
                    }
                )
            }
        }

        // 2. Volume Slide
        SettingsSectionTitle("Reminder Volume (${(settings.reminderVolume * 100).toInt()}% )")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Alarm Volume Scale", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }
                Slider(
                    value = settings.reminderVolume,
                    onValueChange = {
                        medicineViewModel.saveSettings(settings.copy(reminderVolume = it))
                    }
                )
            }
        }

        // 3. Dark Mode Choice
        SettingsSectionTitle("Visual Themes Mode")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val themes = listOf("System", "Light", "Dark")
            themes.forEach { t ->
                val isSelected = settings.darkModeState == t
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        .clickable {
                            medicineViewModel.saveSettings(settings.copy(darkModeState = t))
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 4. Snooze Timer Selection
        SettingsSectionTitle("Snooze Options")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDropdownOpen = !isDropdownOpen }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Snooze, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Snooze Duration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Active delay set: ${settings.snoozeDurationMinutes} Minutes", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }

            if (isDropdownOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                        .padding(bottom = 8.dp)
                ) {
                    val snoozeMinutes = listOf(5, 10, 15, 20)
                    snoozeMinutes.forEach { min ->
                        Text(
                            text = "$min Minutes",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    medicineViewModel.saveSettings(settings.copy(snoozeDurationMinutes = min))
                                    isDropdownOpen = false
                                }
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }

        // 5. Language Selection
        SettingsSectionTitle("Cabiner Language")
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isLangDropdownOpen = !isLangDropdownOpen }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Application Language", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("Display: ${settings.language}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
            }

            if (isLangDropdownOpen) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                        .padding(bottom = 8.dp)
                ) {
                    val languages = listOf("English", "Spanish", "Hindi", "Telugu")
                    languages.forEach { lang ->
                        Text(
                            text = lang,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    medicineViewModel.saveSettings(settings.copy(language = lang))
                                    isLangDropdownOpen = false
                                }
                                .padding(horizontal = 24.dp, vertical = 12.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(top = 12.dp)
    )
}
