package com.example.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoFixHigh
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.screens.AgentsScreen
import com.example.ui.screens.AiToolsScreen
import com.example.ui.screens.CompanionScreen
import com.example.ui.screens.CoreMemoryScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.JarvisTheme

const val ROUTE_COMPANION = "companion"
const val ROUTE_TOOLS = "ai_tools"
const val ROUTE_AGENTS = "agents"
const val ROUTE_STUDIO = "studio"
const val ROUTE_CORE = "core"

private data class JarvisNavItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val navItems = listOf(
    JarvisNavItem(
        route = ROUTE_COMPANION,
        labelRes = R.string.nav_companion,
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        testTag = "nav_companion"
    ),
    JarvisNavItem(
        route = ROUTE_TOOLS,
        labelRes = R.string.nav_tools,
        selectedIcon = Icons.Filled.AutoFixHigh,
        unselectedIcon = Icons.Outlined.AutoFixHigh,
        testTag = "nav_tools"
    ),
    JarvisNavItem(
        route = ROUTE_AGENTS,
        labelRes = R.string.nav_agents,
        selectedIcon = Icons.Filled.Groups,
        unselectedIcon = Icons.Outlined.Groups,
        testTag = "nav_agents"
    ),
    JarvisNavItem(
        route = ROUTE_STUDIO,
        labelRes = R.string.nav_studio,
        selectedIcon = Icons.Filled.DashboardCustomize,
        unselectedIcon = Icons.Outlined.DashboardCustomize,
        testTag = "nav_studio"
    ),
    JarvisNavItem(
        route = ROUTE_CORE,
        labelRes = R.string.nav_core,
        selectedIcon = Icons.Filled.Psychology,
        unselectedIcon = Icons.Outlined.Psychology,
        testTag = "nav_core"
    )
)

@Composable
fun JarvisApp() {
    val context = LocalContext.current
    val viewModel: JarvisViewModel = viewModel(factory = JarvisViewModel.provideFactory(context))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_COMPANION
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    val onNavigate: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    JarvisTheme(themeMode = uiState.preferences.themeMode) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useRail) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 3.dp
                        ) {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                val label = stringResource(item.labelRes)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { onNavigate(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label, maxLines = 1) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useRail) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                val label = stringResource(item.labelRes)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { onNavigate(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label, maxLines = 1) },
                                    modifier = Modifier.testTag(item.testTag)
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = ROUTE_COMPANION,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable(ROUTE_COMPANION) {
                            CompanionScreen(
                                uiState = uiState,
                                onSendMessage = viewModel::sendMessage,
                                onNewConversation = viewModel::startNewConversation,
                                onSelectConversation = viewModel::selectConversation,
                                onDeleteConversation = viewModel::deleteConversation,
                                onConfirmDraft = viewModel::confirmDraft,
                                onDiscardDraft = viewModel::discardDraft,
                                onImportSuggestedTasks = viewModel::importSuggestedTasks,
                                onSelectLanguage = viewModel::setPreferredLanguage,
                                onSelectEngine = viewModel::selectEngine
                            )
                        }
                        composable(ROUTE_TOOLS) {
                            AiToolsScreen(
                                uiState = uiState,
                                onSelectEngine = viewModel::selectEngine,
                                onSelectTool = viewModel::selectTool,
                                onRunTool = viewModel::runAiTool
                            )
                        }
                        composable(ROUTE_AGENTS) {
                            AgentsScreen(
                                uiState = uiState,
                                onLaunchMission = viewModel::launchMission,
                                onToggleMilestone = viewModel::toggleMissionMilestone,
                                onUpdateMissionStatus = viewModel::updateMissionStatus,
                                onDeleteMission = viewModel::deleteMission
                            )
                        }
                        composable(ROUTE_STUDIO) {
                            StudioScreen(
                                uiState = uiState,
                                onAddTask = viewModel::addPlannerTask,
                                onToggleTask = viewModel::togglePlannerTask,
                                onDeleteTask = viewModel::deletePlannerTask
                            )
                        }
                        composable(ROUTE_CORE) {
                            CoreMemoryScreen(
                                uiState = uiState,
                                onToggleGlobalMemory = viewModel::setMemoryEnabled,
                                onAddMemory = viewModel::addMemory,
                                onToggleMemoryEntry = viewModel::toggleMemoryEntry,
                                onDeleteMemoryEntry = viewModel::deleteMemoryEntry,
                                onSelectLanguage = viewModel::setPreferredLanguage,
                                onSelectTone = viewModel::setPersonalityTone,
                                onSelectTheme = viewModel::setThemeMode
                            )
                        }
                    }
                }
            }
        }
    }
}
