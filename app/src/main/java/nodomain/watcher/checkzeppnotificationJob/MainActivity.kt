package nodomain.watcher.checkzeppnotificationJob

import android.Manifest
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
        scheduleJob()
        setContentView(R.layout.activity_main)
        createViewTextLinks()
    }

    private fun scheduleJob() {
        val componentName = ComponentName(this, NotificationAccessJobService::class.java)
        var period = 60 * 1_000L   // 60s
        if (JobInfo.getMinPeriodMillis() > period) period = JobInfo.getMinPeriodMillis()
        val jobInfo = JobInfo.Builder(123, componentName)
            .setPeriodic(period)
            .setPersisted(true)
            .build()

        val jobScheduler = getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.schedule(jobInfo)
    }

    fun createViewTextLinks() {
        val textView = findViewById<TextView>(R.id.textView)
        val text = textView.text.toString()
        val permissionString = getString(R.string.permission)
        val spannableString = SpannableString(text)
        var start = text.indexOf(permissionString)
        while(start>=0) {
            val end = start + permissionString.length
            spannableString.setSpan(TextLinkClicked(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            textView.text = spannableString
            textView.movementMethod = LinkMovementMethod.getInstance()
            start = text.indexOf(permissionString, ++start)
        }
    }

    private class TextLinkClicked(): ClickableSpan() {
        override fun onClick(view: View) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            view.context.startActivity(intent)
        }
    }

}
