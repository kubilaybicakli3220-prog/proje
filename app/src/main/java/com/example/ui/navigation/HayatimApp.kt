package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.ui.ai.AiChatScreen
import com.example.ui.auth.AuthViewModel
import com.example.ui.career.CareerScreen
import com.example.ui.components.AddDocumentDialog
import com.example.ui.components.AddGoalDialog
import com.example.ui.components.AddHabitDialog
import com.example.ui.components.AddJobDialog
import com.example.ui.components.AddNoteDialog
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.finance.FinanceScreen
import com.example.ui.home.HomeScreen
import com.example.ui.home.MainViewModel
import com.example.ui.notes.NotesAndDocsScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.tasks.TasksScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface

import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Insights
import com.example.ui.appcenter.AppCenterScreen
import com.example.ui.intelligence.SmartLifeScreen

enum class Screen(val title: String, val icon: ImageVector) {
    HOME("Ana Sayfa", Icons.Default.Home),
    AI("AI Asistan", Icons.Default.AutoAwesome),
    TASKS("Planlama", Icons.Default.CalendarMonth),
    APP_CENTER("Uygulamalar", Icons.Default.Apps),
    SMART_LIFE("Yaşam", Icons.Default.Insights),
    FINANCE("Finans", Icons.Default.Payments),
    PROFILE("Profil", Icons.Default.Person),
    NOTES("Notlar", Icons.Default.Home),
    CAREER("Kariyer", Icons.Default.Home),
    DOCS("Belgeler", Icons.Default.Home)
}

@Composable
fun HayatimApp(
    mainViewModel: MainViewModel,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }
    val statusMessage by mainViewModel.statusMessage.collectAsState()

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddHabitDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var showAddJobDialog by remember { mutableStateOf(false) }
    var showAddDocumentDialog by remember { mutableStateOf(false) }

    // Show status snackbar
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            mainViewModel.clearStatusMessage()
        }
    }

    // BackHandler for secondary screens
    if (currentScreen != Screen.HOME) {
        BackHandler {
            currentScreen = Screen.HOME
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = NavySurface,
                tonalElevation = 6.dp
            ) {
                val mainNavItems = listOf(
                    Screen.HOME,
                    Screen.AI,
                    Screen.TASKS,
                    Screen.APP_CENTER,
                    Screen.PROFILE
                )
                mainNavItems.forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) CyanAccent else NavyDark.copy(alpha = 0.5f)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                color = if (isSelected) CyanAccent else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CyanAccent.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    viewModel = mainViewModel,
                    onNavigateToAi = { currentScreen = Screen.AI },
                    onNavigateToTasks = { currentScreen = Screen.TASKS },
                    onNavigateToFinance = { currentScreen = Screen.FINANCE },
                    onNavigateToNotes = { currentScreen = Screen.NOTES },
                    onNavigateToCareer = { currentScreen = Screen.CAREER },
                    onNavigateToDocuments = { currentScreen = Screen.DOCS },
                    onNavigateToAppCenter = { currentScreen = Screen.APP_CENTER },
                    onNavigateToSmartLife = { currentScreen = Screen.SMART_LIFE },
                    onOpenAddTaskDialog = { showAddTaskDialog = true },
                    onOpenAddTransactionDialog = { showAddTransactionDialog = true }
                )
                Screen.AI -> AiChatScreen(
                    viewModel = mainViewModel
                )
                Screen.TASKS -> TasksScreen(
                    viewModel = mainViewModel,
                    onOpenAddTaskDialog = { showAddTaskDialog = true },
                    onOpenAddHabitDialog = { showAddHabitDialog = true },
                    onOpenAddGoalDialog = { showAddGoalDialog = true }
                )
                Screen.APP_CENTER -> AppCenterScreen(
                    viewModel = mainViewModel
                )
                Screen.SMART_LIFE -> SmartLifeScreen(
                    viewModel = mainViewModel
                )
                Screen.FINANCE -> FinanceScreen(
                    viewModel = mainViewModel,
                    onOpenAddTransactionDialog = { showAddTransactionDialog = true }
                )
                Screen.PROFILE -> ProfileScreen(
                    mainViewModel = mainViewModel,
                    authViewModel = authViewModel
                )
                Screen.NOTES, Screen.DOCS -> NotesAndDocsScreen(
                    viewModel = mainViewModel,
                    onOpenAddNoteDialog = { showAddNoteDialog = true },
                    onOpenAddDocumentDialog = { showAddDocumentDialog = true }
                )
                Screen.CAREER -> CareerScreen(
                    viewModel = mainViewModel,
                    onOpenAddJobDialog = { showAddJobDialog = true }
                )
            }
        }
    }

    // Dialogs
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, cat, priority, date, time, reminder ->
                mainViewModel.addTask(title, desc, cat, priority, date, time, reminder)
            }
        )
    }

    if (showAddHabitDialog) {
        AddHabitDialog(
            onDismiss = { showAddHabitDialog = false },
            onConfirm = { title, frequency, target ->
                mainViewModel.addHabit(title, frequency, target)
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { title, desc, targetDate, category ->
                mainViewModel.addGoal(title, desc, targetDate, category)
            }
        )
    }

    if (showAddTransactionDialog) {
        AddTransactionDialog(
            onDismiss = { showAddTransactionDialog = false },
            onConfirm = { title, amount, type, cat, date, note ->
                mainViewModel.addTransaction(title, amount, type, cat, date, note)
            }
        )
    }

    if (showAddNoteDialog) {
        AddNoteDialog(
            onDismiss = { showAddNoteDialog = false },
            onConfirm = { title, content, cat ->
                mainViewModel.addNote(title, content, cat)
            }
        )
    }

    if (showAddJobDialog) {
        AddJobDialog(
            onDismiss = { showAddJobDialog = false },
            onConfirm = { company, position, status, date, salary, notes ->
                mainViewModel.addJob(company, position, status, date, salary, notes)
            }
        )
    }

    if (showAddDocumentDialog) {
        AddDocumentDialog(
            onDismiss = { showAddDocumentDialog = false },
            onConfirm = { title, cat, ocr, isPrivate ->
                mainViewModel.addDocument(title, cat, ocr, notes = "", isPrivate = isPrivate)
            }
        )
    }
}
