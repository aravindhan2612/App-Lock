package com.pg.lockapp.presentation.applist.screens

import android.accessibilityservice.AccessibilityService
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import kotlinx.coroutines.delay
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
fun RenderAppListScreen() {
    val appListViewModel: AppListViewModel = hiltViewModel()
    val context = LocalContext.current
    val isDrawOverAccessGiven = remember {
        mutableStateOf(
            canDrawOverlays(
                context
            )
        )
    }
//    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//    context.startActivity(intent)
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
    intent.setData(Uri.parse("package:" + context.packageName))
    context.startActivity(intent)
    context.startForegroundService(Intent(context, AutoStartService::class.java))
    val scope = rememberCoroutineScope()
    scope.launch {
        delay(1000)
        println(
            "******access enable ${
                isAccessibilityServiceEnabled1(
                    context,
                    AccessibilityService::class.java
                )
            }"
        )
    }

    when (appListViewModel.appListUiState) {
        is AppListUiState.Finished -> {
            if (isDrawOverAccessGiven.value) {
                RenderAppList()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedVisibility(
                        visible = !isDrawOverAccessGiven.value
                    ) {
                        Button(onClick = {
                            scope.launch {
                                openDrawOverlaysSettings(context)
                                isDrawOverAccessGiven.value = true
                            }
                        }) {
                            Text(text = "Give draw over the app access")
                        }
                    }

                }
            }

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
                        if (!appInfo.packageName.isNullOrEmpty() && !appInfo.appName.isNullOrEmpty()) appListViewModel.updateLockForApp(
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

fun isUsageStatsPermissionGranted(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val packageManager = context.packageManager;
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)

    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED

}

fun openUsageAccessSettings(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    context.startActivity(intent)
}

fun canDrawOverlays(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        true
    }
}

fun openDrawOverlaysSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.packageName)
        )
    } else {
        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    }
    context.startActivity(intent)
}

fun isAccessibilityServiceEnabled(
    context: Context, service: Class<out AccessibilityService?>?
): Boolean {
    val expectedComponentName = ComponentName(context, service!!)

    val enabledServicesSetting = Settings.Secure.getString(
        context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )

    if (enabledServicesSetting == null) {
        return false
    }

    val colonSplitter = SimpleStringSplitter(':')
    colonSplitter.setString(enabledServicesSetting)

    while (colonSplitter.hasNext()) {
        val componentName = colonSplitter.next()
        if (componentName.equals(expectedComponentName.flattenToString(), ignoreCase = true)) {
            return true
        }
    }

    return false
}

fun isAccessibilityServiceEnabled1(
    context: Context,
    serviceName: Class<out AccessibilityService?>?
): Boolean {
    val accessibilityManager =
        context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices =
        accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
    println("*******Service ${serviceName?.name} ${serviceName?.`package`} ${enabledServices} ")
    for (service in enabledServices) {
        println("*******id ${service.id}")
        if (service.id == serviceName?.name) {
            return true
        }
    }
    return false
}

