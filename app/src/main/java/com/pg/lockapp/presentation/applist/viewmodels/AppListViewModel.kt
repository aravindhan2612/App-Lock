package com.pg.lockapp.presentation.applist.viewmodels

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pg.lockapp.common.CommonHelper
import com.pg.lockapp.domain.models.enitites.AppInformation
import com.pg.lockapp.domain.models.interfaces.AppListUiState
import com.pg.lockapp.domain.repository.AppInformationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private fun getAllApps() = viewModelScope.launch {
        appListUiState = AppListUiState.Loading
        val dbApplicationInfoList = getAppInfosFromRoom()
        val localApplicationList = getApplicationInfoWithoutSystemApps()
        if (localApplicationList.isNotEmpty()) {
            if (dbApplicationInfoList.isEmpty()) {
              addAllAppInfoToRoom(localApplicationList)
            } else {
              addFilterAppInfosToRoom(dbApplicationInfoList, localApplicationList)
            }
            // once retrieved application info adding to Ui state
        }
        appListUiState = AppListUiState.Finished
    }

    private suspend fun getAppInfosFromRoom(): List<AppInformation> = withContext(Dispatchers.IO) {
        repo.getAppInfosFromRoom()
    }

    private suspend fun addAllAppInfoToRoom(appInfoList: List<AppInformation>) =
        withContext(Dispatchers.IO) {
            repo.addAllAppInfoToRoom(appInfoList)
        }

    private suspend fun addFilterAppInfosToRoom(
        dbApplicationInfoList: List<AppInformation>,
        localApplicationList: List<AppInformation>
    ) =
        withContext(Dispatchers.IO) {
            val set1 =
                dbApplicationInfoList.map { dbApplicationInfo -> dbApplicationInfo.packageName }
                    .toSet()
            val filterList =
                localApplicationList.filter { localAppInfo -> localAppInfo.packageName !in set1 }
            if (filterList.isNotEmpty()) {
                repo.addAllAppInfoToRoom(filterList)
            }
        }


    fun updateLockForApp(packageName: String, checked: Boolean, appName: String) = viewModelScope.launch {
        repo.updateAppInfoToRoom(packageName = packageName, isLocked = checked, appName = appName)
    }

    private fun getApplicationInfoWithoutSystemApps(): List<AppInformation> {
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

    // below functions used to get system apps
    private fun getApplicationInfoWithSystemApps(): List<AppInformation> {
        val pm = context.packageManager
        val localApplicationList = ArrayList<AppInformation>()
        val resolvedInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0L))
        } else {
            pm.getInstalledApplications(0)
        }
        resolvedInfos.forEachIndexed { indexed, resolvedInfo ->
            val appInformation = AppInformation()
            appInformation.appName = resolvedInfo.name ?: resolvedInfo.loadLabel(pm).toString()
            appInformation.packageName = resolvedInfo.packageName
            appInformation.icon = CommonHelper.getEncoded64ImageStringFromBitmap(
                resolvedInfo.loadIcon(pm).toBitmap()
            )
            localApplicationList.add(appInformation)
        }
        return localApplicationList
    }
}