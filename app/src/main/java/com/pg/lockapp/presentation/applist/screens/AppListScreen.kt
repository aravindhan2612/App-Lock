package com.pg.lockapp.presentation.applist.screens

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.pg.lockapp.domain.models.enitites.AppInformation
import com.pg.lockapp.domain.models.interfaces.AppListUiState
import com.pg.lockapp.presentation.applist.items.AppInfoItem
import com.pg.lockapp.presentation.applist.viewmodels.AppListViewModel
import com.pg.lockapp.presentation.services.LockScreenService
import kotlinx.coroutines.launch


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AppListScreen(
    paddingValues: PaddingValues,
    appListViewModel: AppListViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    val permissionRequest =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { result ->
            hasNotificationPermission = result
        }
    val isDrawOverAccessGiven = remember {
        mutableStateOf(
            false
        )
    }
    val isUsageStatsAccessGiven = remember {
        mutableStateOf(
            false
        )
    }
    OnLifecycleEvent { owner, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME -> {
                isDrawOverAccessGiven.value = Settings.canDrawOverlays(context)
                isUsageStatsAccessGiven.value = isUsageStatsPermissionGranted(
                    context
                )
            }

            else -> {
            }
        }
    }
    if (hasNotificationPermission && isDrawOverAccessGiven.value && isUsageStatsAccessGiven.value) {
        RenderAppListScreen(paddingValues, appListViewModel)
    } else {
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = !hasNotificationPermission
            ) {
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionRequest.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }) {
                    Text(text = "Allow notification")
                }
            }
            AnimatedVisibility(
                visible = !isDrawOverAccessGiven.value
            ) {
                Button(onClick = {
                    scope.launch {
                        openDrawOverlaysSettings(context)
                    }

                }) {
                    Text(text = "Allow Draw overlay access")
                }
            }
            AnimatedVisibility(
                visible = !isUsageStatsAccessGiven.value
            ) {
                Button(onClick = {
                    scope.launch {
                        openUsageAccessSettings(context)
                    }
                }) {
                    Text(text = "Allow usage stats access")
                }
            }
        }
    }


}

@Composable
fun RenderAppListScreen(
    paddingValues: PaddingValues,
    appListViewModel: AppListViewModel
) {

    when (appListViewModel.appListUiState) {
        is AppListUiState.Finished -> {
            RenderAppList(paddingValues, appListViewModel)
        }

        is AppListUiState.Loading -> {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
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
fun RenderAppList(
    paddingValues: PaddingValues,
    appListViewModel: AppListViewModel
) {
    val context = LocalContext.current
    val appInfos by appListViewModel.appInfos.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    LaunchedEffect(appInfos) {
        scope.launch {
            if (appInfos.any { it.isLocked }) {
                context.startService(Intent(context, LockScreenService::class.java))
            } else {
                context.stopService(Intent(context, LockScreenService::class.java))
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        LazyColumn {
            items(appInfos) { appInfo: AppInformation ->
                AppInfoItem(appInfo = appInfo, onCheckedChange = { checked ->
                    scope.launch {
                        if (checked) {
                            context.startService(Intent(context, LockScreenService::class.java))
                        }
                        if (!appInfo.packageName.isNullOrEmpty() && !appInfo.appName.isNullOrEmpty()) {
                            appListViewModel.updateLockForApp(
                                packageName = appInfo.packageName!!,
                                checked,
                                appName = appInfo.appName!!
                            )
                        }
                    }
                })
            }
        }
    }
}


fun openUsageAccessSettings(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    context.startActivity(intent)
}


fun openDrawOverlaysSettings(context: Context) {
    val intent = Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION, ("package:" + context.packageName).toUri()
    )
    context.startActivity(intent)
}

private fun isUsageStatsPermissionGranted(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val packageManager = context.packageManager
    val applicationInfo = packageManager.getApplicationInfo(context.packageName, 0)

    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, context.packageName
        )
    } else {
        AppOpsManager.MODE_ALLOWED
    }
    return mode == AppOpsManager.MODE_ALLOWED

}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

