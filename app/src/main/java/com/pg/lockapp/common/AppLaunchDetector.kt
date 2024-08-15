package com.pg.lockapp.common

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AppLaunchDetector(private val context: Context) {

    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var previousUsageStats: List<UsageStats>? = null

    fun startMonitoring(interval: Long = 60000, onAppLaunch: (String) -> Unit) {
        coroutineScope.launch {
            while (true) {
                delay(interval)
                val currentUsageStats = getUsageStats(interval)
                if (previousUsageStats != null) {
                    checkAppLaunches(currentUsageStats, onAppLaunch)
                }
                previousUsageStats = currentUsageStats
            }
        }
    }

    private suspend fun getUsageStats(interval: Long): List<UsageStats> {
        val endTime = System.currentTimeMillis()
        val beginTime = endTime - 1000L // Adjust interval as needed
        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            beginTime,
            endTime
        )
    }

    private fun checkAppLaunches(
        currentUsageStats: List<UsageStats>,
        onAppLaunch: (String) -> Unit
    ) {
        // Assuming app launch is defined as:
        // - App was not in foreground in previous interval
        // - App is in foreground in current interval

        val previousForegroundPackages =
            previousUsageStats?.filter { it.totalTimeInForeground > 0 }?.map { it.packageName }
                ?.toSet() ?: emptySet()
        val currentForegroundPackages =
            currentUsageStats.filter { it.totalTimeInForeground > 0 }?.map { it.packageName }
                ?.toSet() ?: emptySet()

        val launchedApps = currentForegroundPackages - previousForegroundPackages
        launchedApps.forEach { onAppLaunch(it) }
    }
}
