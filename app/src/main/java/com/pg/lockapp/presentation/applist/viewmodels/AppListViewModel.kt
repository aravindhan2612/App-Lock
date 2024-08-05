package com.pg.lockapp.presentation.applist.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.lockapp.common.CommonHelper
import com.pg.lockapp.data.db.AppLockDataBase
import com.pg.lockapp.domain.models.enitites.AppInformation
import com.pg.lockapp.domain.models.interfaces.AppListUiState
import com.pg.lockapp.domain.repository.AppInformationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppListViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: AppInformationRepository
) : ViewModel() {

    var appListUiState: AppListUiState by mutableStateOf(AppListUiState.Loading)
        private set

    init {
        getAllApps()
    }

    val appInfos = repo.getAllAppInformationListAsFlow()

    private fun getAllApps() {
        viewModelScope.launch {
            appListUiState = AppListUiState.Loading
            delay(2000)
            repo.getAppInfosFromRoom()
                .also { dbApplicationInfoList ->
                    val localApplicationList = getApplicationFromPm()
                    if (localApplicationList.isNotEmpty()) {
                        if (dbApplicationInfoList.isEmpty()) {
                            repo.addAllAppInfoToRoom(localApplicationList)
                        } else {
                            val set1 =
                                dbApplicationInfoList.map { dbApplicationInfo -> dbApplicationInfo.packageName }
                                    .toSet()
                            val filterList =
                                localApplicationList.filter { localAppInfo -> localAppInfo.packageName !in set1 }
                            if (filterList.isNotEmpty()) {
                                repo.addAllAppInfoToRoom(filterList)
                            }
                        }
                        // once retrieved application info adding to Ui state
                        appListUiState = AppListUiState.Success
                    } else {
                        appListUiState = AppListUiState.Error
                    }
                }
        }
    }

    fun updateLockForApp(packageName: String, checked: Boolean) = viewModelScope.launch {
        repo.updateAppInfoToRoom(packageName = packageName, isLocked = checked)
    }

    private fun getApplicationFromPm(): List<AppInformation> {
        val pm = context.packageManager
        val localApplicationList = ArrayList<AppInformation>()
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        // mainIntent is used to skip the system apps
        val resolvedInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            pm.queryIntentActivities(mainIntent, 0)
        }
        resolvedInfos.forEach { resolvedInfo ->
            val appInformation = AppInformation()
            val resources =
                pm.getResourcesForApplication(resolvedInfo.activityInfo.applicationInfo)
            appInformation.appName = if (resolvedInfo.activityInfo.labelRes != 0) {
                resources.getString(resolvedInfo.activityInfo.labelRes)
            } else {
                resolvedInfo.activityInfo.applicationInfo.loadLabel(pm).toString()
            }
            appInformation.packageName = resolvedInfo.activityInfo.packageName
            appInformation.icon = CommonHelper.getEncoded64ImageStringFromBitmap(
                resolvedInfo.activityInfo.loadIcon(pm).toBitmap()
            )
            localApplicationList.add(appInformation)
        }
        return localApplicationList
    }
}