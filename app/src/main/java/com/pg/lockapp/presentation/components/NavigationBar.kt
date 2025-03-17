package com.pg.lockapp.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.pg.lockapp.R
import com.pg.lockapp.domain.models.enitites.Screen
import com.pg.lockapp.presentation.theme.Purple80
import com.pg.lockapp.presentation.theme.aqua20


@Composable
fun BottomNavigationBar(navController: NavController) {
    val selectedIndex = remember {
        mutableIntStateOf(0)
    }
    NavigationBar(
        containerColor = Purple80,
    ) {
        val iconSize = 32.dp
        NavigationBarItem(
            selected = selectedIndex.intValue == 0,
            onClick = {
                selectedIndex.intValue = 0
                navController.navigate(Screen.AppList)
            }, icon = {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.baseline_apps_24),
                    contentDescription = "Apps",
                    modifier = Modifier.size(iconSize),
                    tint = aqua20
                )
            },
            label = {
                Text(text = "Apps")
            }
        )
        NavigationBarItem(
            selected = selectedIndex.intValue == 1,
            onClick = {
                selectedIndex.intValue = 1
                navController.navigate(Screen.Settings)
            }, icon = {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(iconSize),
                    tint = aqua20
                )
            },
            label = {
                Text(text = "Settings")
            }
        )
    }
}