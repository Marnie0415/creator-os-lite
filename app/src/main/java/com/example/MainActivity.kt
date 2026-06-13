package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import kotlin.math.abs
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ai.AiScreen
import com.example.ai.AiViewModel
import com.example.client.ClientScreen
import com.example.client.ClientViewModel
import com.example.dashboard.DashboardScreen
import com.example.dashboard.DashboardViewModel
import com.example.dashboard.RiskItem
import com.example.data.AppDatabase
import com.example.invoice.InvoiceViewModel
import com.example.project.ProjectScreen
import com.example.project.ProjectViewModel
import com.example.settings.SettingsScreen
import com.example.ui.SplashScreen
import com.example.ui.assistedViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val app = application as CreatorOSApplication

    setContent {
      var showSplash by remember { mutableStateOf(true) }

      if (showSplash) {
        SplashScreen(onSplashFinished = { showSplash = false })
      } else {
          val isDarkMode by app.settingsManager.isDarkMode.collectAsState()
          MyApplicationTheme(isDarkTheme = isDarkMode) {
            val navController = rememberNavController()
  
          val clientViewModel = assistedViewModel {
            ClientViewModel(app.clientRepository, app.projectRepository, app.invoiceRepository)
          }
        val projectViewModel = assistedViewModel {
          ProjectViewModel(
            app.projectRepository,
            app.clientRepository,
            app.invoiceRepository
          )
        }
        val invoiceViewModel = assistedViewModel {
          InvoiceViewModel(app.invoiceRepository, app.projectRepository, app.clientRepository)
        }
        val aiViewModel = assistedViewModel {
          AiViewModel(this@MainActivity.application, app.settingsManager)
        }

        val clientsList by clientViewModel.clientsList.collectAsState()

        val riskFeedViewModel = assistedViewModel {
          DashboardViewModel(this@MainActivity.application, app.clientRepository, app.projectRepository, app.invoiceRepository, app.settingsManager)
        }
        val dashboardState by riskFeedViewModel.uiState.collectAsState()

        val ghostedAndOverdue = remember(dashboardState.riskFeed) {
          val map = mutableListOf<Pair<String, Pair<Double, String>>>()
          for (risk in dashboardState.riskFeed) {
            val (name, amount, situation) = when (risk) {
              is RiskItem.CriticalClient -> Triple(risk.clientName, risk.pendingAmount, "${risk.daysSinceContact}d ghosted + overdue")
              is RiskItem.OverdueInvoice -> Triple(risk.projectTitle, risk.amount, "overdue ${risk.daysOverdue}d")
              is RiskItem.GhostedClient -> Triple(risk.clientName, 0.0, "${risk.hoursSinceContact}hr no contact")
              is RiskItem.ExpiringProject -> Triple(risk.projectTitle, 0.0, if (risk.isOverdue) "${abs(risk.hoursRemaining)}hr overdue" else "${risk.hoursRemaining}hr left")
            }
            map.add(name to (amount to situation))
          }
          map
        }

        var triggerAddClient by remember { mutableStateOf(false) }
        var triggerAddProject by remember { mutableStateOf(false) }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "dashboard"

            NavigationBar(
              containerColor = MaterialTheme.colorScheme.background,
              tonalElevation = 8.dp,
              modifier = Modifier.testTag("app_bottom_nav_bar")
            ) {
              NavigationBarItem(
                selected = currentRoute == "dashboard",
                onClick = { navController.navigate("dashboard") { popUpTo("dashboard") { saveState = true }; launchSingleTop = true; restoreState = true } },
                icon = { Icon(imageVector = Icons.Default.Home, contentDescription = stringResource(R.string.cd_dashboard)) },
                label = { Text(stringResource(R.string.nav_dashboard), style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  unselectedIconColor = MaterialTheme.colorScheme.secondary,
                  indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("nav_dashboard")
              )
              NavigationBarItem(
                selected = currentRoute == "clients",
                onClick = { navController.navigate("clients") { popUpTo("dashboard") { saveState = true }; launchSingleTop = true; restoreState = true } },
                icon = { Icon(imageVector = Icons.Default.Person, contentDescription = stringResource(R.string.cd_clients)) },
                label = { Text(stringResource(R.string.nav_clients), style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  unselectedIconColor = MaterialTheme.colorScheme.secondary,
                  indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("nav_clients")
              )
              NavigationBarItem(
                selected = currentRoute == "projects",
                onClick = { navController.navigate("projects") { popUpTo("dashboard") { saveState = true }; launchSingleTop = true; restoreState = true } },
                icon = { Icon(imageVector = Icons.Default.Assignment, contentDescription = stringResource(R.string.cd_projects)) },
                label = { Text(stringResource(R.string.nav_projects), style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  unselectedIconColor = MaterialTheme.colorScheme.secondary,
                  indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("nav_projects")
              )
              NavigationBarItem(
                selected = currentRoute == "ai_tools",
                onClick = { navController.navigate("ai_tools") { popUpTo("dashboard") { saveState = true }; launchSingleTop = true; restoreState = true } },
                icon = { Icon(imageVector = Icons.Default.Chat, contentDescription = stringResource(R.string.cd_ai_tools)) },
                label = { Text(stringResource(R.string.nav_ai_tools), style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  unselectedIconColor = MaterialTheme.colorScheme.secondary,
                  indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("nav_ai")
              )
              NavigationBarItem(
                selected = currentRoute == "settings",
                onClick = { navController.navigate("settings") { popUpTo("dashboard") { saveState = true }; launchSingleTop = true; restoreState = true } },
                icon = { Icon(imageVector = Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings)) },
                label = { Text(stringResource(R.string.nav_settings), style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  unselectedIconColor = MaterialTheme.colorScheme.secondary,
                  indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.testTag("nav_settings")
              )
            }
          }
        ) { innerPadding ->
          NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
          ) {
            composable("dashboard") {
              DashboardScreen(
                viewModel = riskFeedViewModel,
                onNavigateToClients = { navController.navigate("clients") },
                onAddClientClick = {
                  triggerAddClient = true
                  navController.navigate("clients")
                },
                onAddProjectClick = {
                  if (clientsList.isNotEmpty()) {
                    triggerAddProject = true
                    navController.navigate("projects")
                  } else {
                    navController.navigate("clients")
                  }
                }
              )
            }
            composable("clients") {
              ClientScreen(
                viewModel = clientViewModel,
                triggerShowAddClientDialog = triggerAddClient,
                onAddClientDialogDismissed = { triggerAddClient = false }
              )
            }
            composable("projects") {
              ProjectScreen(
                projectViewModel = projectViewModel,
                invoiceViewModel = invoiceViewModel,
                clients = clientsList.map { it.client },
                onNavigateToClients = { navController.navigate("clients") },
                triggerShowAddProjectDialog = triggerAddProject,
                onAddProjectDialogDismissed = { triggerAddProject = false }
              )
            }
            composable("ai_tools") {
              AiScreen(
                viewModel = aiViewModel,
                ghostedOrOverdueClients = ghostedAndOverdue,
                onNavigateToSettings = { navController.navigate("settings") }
              )
            }
            composable("settings") {
              SettingsScreen(
                settingsManager = app.settingsManager,
                clientRepository = app.clientRepository,
                projectRepository = app.projectRepository,
                invoiceRepository = app.invoiceRepository,
                onResetDatabase = {
                  lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getDatabase(this@MainActivity).clearAllTables()
                    launch(Dispatchers.Main) {
                      navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = true }
                      }
                    }
                  }
                }
              )
            }
          }
        }
      }
      }
    }
  }
}
