package com.pg.lockapp.presentation.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pg.lockapp.domain.models.enitites.Screen
import com.pg.lockapp.presentation.applist.screens.AppListScreen
import com.pg.lockapp.presentation.chat.screens.ChatScreen
import com.pg.lockapp.presentation.components.BottomNavigationBar
import com.pg.lockapp.presentation.theme.LockAppTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LockAppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxHeight()
                ) {
                    BaseScreen()
                }

            }
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @Composable
    fun BaseScreen() {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier
                .statusBarsPadding(),
            bottomBar = { BottomNavigationBar(navController) }
        ) {
            NavHost(navController = navController, startDestination = Screen.AppList) {
                composable<Screen.AppList> {
                    AppListScreen()
                }
                composable<Screen.ChatList> {
                    ChatScreen()
                }
            }

        }
    }
}

