package nodomain.watcher.checkzeppnotificationJob

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val componentName = ComponentName(context, NotificationAccessJobService::class.java)
            val jobInfo = JobInfo.Builder(123, componentName)
                .setPeriodic(15 * 60 * 1000L) // Minimum allowed interval
                .setPersisted(true)
                .build()

            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.schedule(jobInfo)
        }
    }
}
