package com.pg.lockapp.presentation.applist.screens

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.pg.lockapp.domain.models.enitites.AppInformation
import com.pg.lockapp.domain.models.interfaces.AppListUiState
import com.pg.lockapp.presentation.applist.items.AppInfoItem
import com.pg.lockapp.presentation.applist.viewmodels.AppListViewModel
import com.pg.lockapp.presentation.services.AutoStartService
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun AppListScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState =
            rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
        if (notificationPermissionState.status.isGranted) {
            RenderAppListScreen()
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val textToShow = if (notificationPermissionState.status.shouldShowRationale) {
                    "The notification is important for this app. Please grant the permission."
                } else {
                    "Please grant the permission for notification"
                }

                Text(textToShow)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { notificationPermissionState.launchPermissionRequest() }) {
                    Text("Request  permission")
                }
            }
        }
    } else {
        RenderAppListScreen()
    }

}

@Composable
fun RenderAppListScreen(appListViewModel: AppListViewModel = hiltViewModel()) {
    when (appListViewModel.appListUiState) {
        is AppListUiState.Finished -> {
            RenderAppList()
        }

        is AppListUiState.Loading -> {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.width(64.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
fun RenderAppList(appListViewModel: AppListViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val appInfos by appListViewModel.appInfos.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    if (appInfos.any { it.isLocked }) {
        scope.launch {
            context.startForegroundService(Intent(context, AutoStartService::class.java))
        }
    } else {
        scope.launch {
            context.stopService(Intent(context, AutoStartService::class.java))
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        LazyColumn() {
            items(appInfos) { appInfo: AppInformation ->
                AppInfoItem(appInfo = appInfo, onCheckedChange = { checked ->
                    scope.launch {
                        if (!appInfo.packageName.isNullOrEmpty() && !appInfo.appName.isNullOrEmpty())
                            appListViewModel.updateLockForApp(
                                packageName = appInfo.packageName!!,
                                checked,
                                appName = appInfo.appName!!
                            )
                    }
                })
            }
        }
    }
}