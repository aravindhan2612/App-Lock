package com.pg.lockapp.presentation.navigation

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pg.lockapp.domain.models.enitites.Screen
import com.pg.lockapp.presentation.applist.screens.AppListScreen
import com.pg.lockapp.presentation.components.BottomNavigationBar
import com.pg.lockapp.presentation.settings.screens.SettingsScreen


@Composable
fun BaseScreen() {
    val navController = rememberNavController()
    Scaffold(
        modifier = Modifier
            .statusBarsPadding(),
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        NavHost(navController = navController, startDestination = Screen.AppList) {
            composable<Screen.AppList> {
                AppListScreen(padding)
            }
            composable<Screen.Settings> {
                SettingsScreen(padding)

            }
        }

    }
}