package nodomain.watcher.checkzeppnotificationJob

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import android.os.Build
import androidx.work.Configuration


class NotificationAccessJobService : JobService() {

    companion object {
        private val PACKAGE = "com.huami.watch.hmwatchmanager"
        private val channelId = "notification_access_alert"
    }

    init {
        Configuration.Builder()
            .setJobSchedulerJobIdRange(0, 1000)
            .build()
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d("NotificationAccessJS", "Job started")

        if (!isNotificationListenerEnabled(PACKAGE)) {
            Log.d("NotificationAccessJS", "Permission missing")

            // Try auto-grant if root is available
            if (RootUtils.isRootAvailable()) {
                showToast()
                Log.d("NotificationAccessJS", "Root available, attempting auto-grant")
                val granted = RootUtils.grantNotificationListener(this, PACKAGE)
                if (granted) {
                    Log.d("NotificationAccessJS", "Auto-grant successful")
                    showFixedNotification()
                    return false
                } else {
                    Log.e("NotificationAccessJS", "Auto-grant failed")
                }
            } else {
                 Log.d("NotificationAccessJS", "Root not available")
            }

            // Fallback to manual warning
            try {
                showToast()
                showNotification()
            } catch (ignored: Exception) {
                Log.d("NotificationAccessJS", "toast failed", ignored)
            }

        } else {
             Log.d("NotificationAccessJS", "Permission already granted")
        }
        
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return true
    }

    private fun isNotificationListenerEnabled(packageName: String): Boolean {
        val enabledListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (enabledListeners.isNullOrEmpty()) {
            return false
        }
        val names = enabledListeners.split(":")
        for (name in names) {
            val component = ComponentName.unflattenFromString(name)
            if (component != null && component.packageName == packageName) {
                return true
            }
        }
        return false
    }

    private fun showToast() {
        Toast.makeText(applicationContext, getString(R.string.toast, "Zepp App"), Toast.LENGTH_LONG).show()
    }

    private fun showNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notification Access Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val settingsIntent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, settingsIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(1001, notification)
    }

    private fun showFixedNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "permission_fixed_channel"
        val channelName = "Permission Fixed"
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Permission Granted")
            .setContentText("Zepp Notification Access was automatically enabled.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .build()

        manager.notify(2, notification)
    }
}
