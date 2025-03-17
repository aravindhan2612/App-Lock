package com.pg.lockapp.presentation.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.pg.lockapp.LockAppApplication.Companion.CHANNEL_ID
import com.pg.lockapp.LockAppApplication.Companion.CHANNEL_NAME
import com.pg.lockapp.R
import com.pg.lockapp.domain.repository.AppInformationRepository
import com.pg.lockapp.presentation.MyLifecycleOwner
import com.pg.lockapp.presentation.activity.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class LockScreenService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    private val windowManager get() = getSystemService(WINDOW_SERVICE) as WindowManager
    private var isRunning = false;
    private var isVisible = false;

    @Inject
    lateinit var repo: AppInformationRepository

    override fun onCreate() {
        super.onCreate()
        isRunning = true;
        createNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAppMonitoring()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.launch {
            delay(5000)
            val broadcastIntent = Intent("com.pg.lockapp.presentation.services.AutoStartService")
            sendBroadcast(broadcastIntent)
        }
        isRunning = false
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotification() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle("Auto start service")
            .setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(false).build()
        notification.flags = Notification.FLAG_NO_CLEAR
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(serviceChannel)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, notification)
        }
    }

    private fun showOverlay(onClick: (composView: ComposeView) -> Unit) {
        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val composeView = ComposeView(this)
        composeView.setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.White),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Button(modifier = Modifier, onClick = { onClick(composeView) }) {
                    Text(text = "Dismiss activity")
                }
            }


        }

        // Trick The ComposeView into thinking we are tracking lifecycle
        val viewModelStore = ViewModelStore()
        val viewModelStoreOwner = object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore
                get() = viewModelStore
        }
        val lifecycleOwner = MyLifecycleOwner()
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        composeView.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        composeView.setViewTreeViewModelStoreOwner(viewModelStoreOwner)
        windowManager.addView(composeView, params)
    }

    private fun startAppMonitoring() {
        scope.launch {
            val usageStatsManager =
                getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
            val isLockedApps = repo.getAppInfosFromRoom().filter { app -> app.isLocked }
            var lastForegroundApp: String? = null

            while (isRunning) {
                val currentTime = System.currentTimeMillis();
                val interval = 1000; // 1 second
                val usageEvents =
                    usageStatsManager.queryEvents(currentTime - interval, currentTime);

                val event = UsageEvents.Event();
                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event);
                    if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                        val appInformation =
                            isLockedApps.find { app -> app.packageName == event.packageName }
                        if (appInformation != null && !isVisible) {
                            isVisible = true
                            // The locked app is now in the foreground
                            withContext(Dispatchers.Main) {
                                showOverlay(onClick = { view ->
                                    windowManager.removeView(view)
                                    isVisible = false
                                }) // Implement locking logic
                            }
                        }
                    }
                }
                delay(1000) // Check every second
            }
        }
    }

}