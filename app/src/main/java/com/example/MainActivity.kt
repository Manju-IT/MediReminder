package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodels.AuthViewModel
import com.example.ui.viewmodels.MedicineViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // Instantiate our MVVM ViewModels
            val authViewModel: AuthViewModel = viewModel()
            val medicineViewModel: MedicineViewModel = viewModel()

            // Observe the user settings state
            val settings by medicineViewModel.settings.collectAsState()
            
            // Resolve custom light/dark/system mode dynamically
            val darkTheme = when (settings.darkModeState) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(
                darkTheme = darkTheme,
                dynamicColor = false // Force custom color palette across devices
            ) {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                val loggedInUser by authViewModel.loggedInUser.collectAsState()

                // Route routing setup: automatic login redirection
                LaunchedEffect(settings.onboardingCompleted, loggedInUser) {
                    if (currentRoute == null) {
                        val destination = when {
                            !settings.onboardingCompleted -> "onboarding"
                            loggedInUser == null -> "login"
                            else -> "home"
                        }
                        navController.navigate(destination) {
                            popUpTo(0)
                        }
                    }
                }

                // Decide whether to show bottom M3 navigation bar
                val bottomBarScreens = listOf("home", "medicine_list", "history", "profile")
                val shouldShowBottomBar = currentRoute in bottomBarScreens

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (shouldShowBottomBar) {
                            NavigationBar {
                                // Home Tab
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        if (currentRoute != "home") {
                                            navController.navigate("home") {
                                                popUpTo("home") { inclusive = true }
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == "home") Icons.Default.Home else Icons.Outlined.Home,
                                            contentDescription = "Home icon"
                                        )
                                    },
                                    label = { Text("Home") }
                                )

                                // Medicine Cabinet Tab
                                NavigationBarItem(
                                    selected = currentRoute == "medicine_list",
                                    onClick = {
                                        if (currentRoute != "medicine_list") {
                                            navController.navigate("medicine_list") {
                                                popUpTo("home")
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == "medicine_list") Icons.Default.MedicalServices else Icons.Default.MedicalServices,
                                            contentDescription = "Cabinet Icon"
                                        )
                                    },
                                    label = { Text("Cabinet") }
                                )

                                // History Logs Tab
                                NavigationBarItem(
                                    selected = currentRoute == "history",
                                    onClick = {
                                        if (currentRoute != "history") {
                                            navController.navigate("history") {
                                                popUpTo("home")
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == "history") Icons.Default.History else Icons.Outlined.History,
                                            contentDescription = "History icon"
                                        )
                                    },
                                    label = { Text("History") }
                                )

                                // Profile Tab
                                NavigationBarItem(
                                    selected = currentRoute == "profile",
                                    onClick = {
                                        if (currentRoute != "profile") {
                                            navController.navigate("profile") {
                                                popUpTo("home")
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentRoute == "profile") Icons.Default.Person else Icons.Outlined.Person,
                                            contentDescription = "Profile icon"
                                        )
                                    },
                                    label = { Text("Profile") }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = if (settings.onboardingCompleted) "login" else "onboarding",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    ) {
                        // Onboarding screens
                        composable("onboarding") {
                            OnboardingScreen(
                                onFinished = {
                                    medicineViewModel.saveSettings(settings.copy(onboardingCompleted = true))
                                    navController.navigate("login") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Login screen
                        composable("login") {
                            LoginScreen(
                                authViewModel = authViewModel,
                                onNavigateToSignup = { navController.navigate("signup") },
                                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Register screen
                        composable("signup") {
                            SignupScreen(
                                authViewModel = authViewModel,
                                onNavigateToLogin = { navController.navigate("login") },
                                onSignupSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Reset password screen
                        composable("forgot_password") {
                            ForgotPasswordScreen(
                                authViewModel = authViewModel,
                                onNavigateBackToLogin = { navController.popBackStack() }
                            )
                        }

                        // Home dashboard dashboard
                        composable("home") {
                            HomeScreen(
                                loggedInUser = loggedInUser,
                                medicineViewModel = medicineViewModel,
                                onNavigateToAddMedicine = { navController.navigate("add_medicine") },
                                onNavigateToSchedule = { navController.navigate("medicine_list") },
                                onNavigateToHistory = { navController.navigate("history") },
                                onNavigateToProfile = { navController.navigate("profile") }
                            )
                        }

                        // Add Alarm screen
                        composable("add_medicine") {
                            AddMedicineScreen(
                                medicineViewModel = medicineViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Medicines list screen
                        composable("medicine_list") {
                            MedicineListScreen(
                                medicineViewModel = medicineViewModel,
                                onNavigateToAddMedicine = { navController.navigate("add_medicine") },
                                onNavigateToDetails = { medicineId ->
                                    navController.navigate("medicine_details/$medicineId")
                                }
                            )
                        }

                        // Medicine details screen
                        composable(
                            route = "medicine_details/{medicineId}",
                            arguments = listOf(navArgument("medicineId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val medicineId = backStackEntry.arguments?.getLong("medicineId") ?: -1L
                            MedicineDetailsScreen(
                                medicineId = medicineId,
                                medicineViewModel = medicineViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Histories screen
                        composable("history") {
                            HistoryScreen(
                                medicineViewModel = medicineViewModel
                            )
                        }

                        // User profile screen
                        composable("profile") {
                            ProfileScreen(
                                loggedInUser = loggedInUser,
                                authViewModel = authViewModel,
                                onNavigateToSettings = { navController.navigate("settings") },
                                onLogoutFinished = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Settings settings screen
                        composable("settings") {
                            SettingsScreen(
                                medicineViewModel = medicineViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
