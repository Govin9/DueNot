package io.github.govin9.duenot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.github.govin9.duenot.ui.MainViewModel
import io.github.govin9.duenot.ui.MainViewModelFactory
import io.github.govin9.duenot.ui.card.AddEditCardScreen
import io.github.govin9.duenot.ui.history.HistoryScreen
import io.github.govin9.duenot.ui.home.HomeScreen
import io.github.govin9.duenot.ui.settings.SettingsScreen
import io.github.govin9.duenot.ui.theme.HelloWorldTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MainApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HelloWorldTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(64.dp),
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val items = listOf("home", "history", "settings")
                val icons = listOf(Icons.Default.Home, Icons.Default.List, Icons.Default.Settings)
                val labels = listOf("Home", "History", "Settings")

                items.forEachIndexed { index, screen ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = labels[index]) },
                        label = { Text(labels[index]) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen } == true,
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                            indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary,
                            selectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSecondary,
                            selectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            unselectedIconColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            unselectedTextColor = androidx.compose.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        onClick = {
                            navController.navigate(screen) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(viewModel, navController)
            }
            composable("history") {
                HistoryScreen(viewModel, navController, cardId = -1) // Global History
            }
            composable("settings") {
                SettingsScreen()
            }
            composable(
                route = "add_edit_card/{cardId}",
                arguments = listOf(navArgument("cardId") { type = NavType.IntType; defaultValue = -1 })
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getInt("cardId") ?: -1
                AddEditCardScreen(viewModel, navController, cardId)
            }
            composable(
                route = "card_history/{cardId}",
                arguments = listOf(navArgument("cardId") { type = NavType.IntType })
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getInt("cardId") ?: -1
                HistoryScreen(viewModel, navController, cardId = cardId)
            }
        }
    }
}
