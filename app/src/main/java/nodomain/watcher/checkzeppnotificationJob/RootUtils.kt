package nodomain.watcher.checkzeppnotificationJob

import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import java.io.DataOutputStream
import java.io.IOException

object RootUtils {

    fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val os = DataOutputStream(process.outputStream)
            os.writeBytes("exit\n")
            os.flush()
            val exitValue = process.waitFor()
            exitValue == 0
        } catch (e: Exception) {
            false
        }
    }

    fun grantNotificationListener(context: Context, targetPackage: String): Boolean {
        android.util.Log.d("RootUtils", "Attempting to grant notification listener for $targetPackage")
        val componentName = getNotificationListenerComponent(context, targetPackage)
        if (componentName == null) {
            android.util.Log.e("RootUtils", "NotificationListenerService component not found for $targetPackage")
            return false
        }
        android.util.Log.d("RootUtils", "Found component: $componentName")
        
        // Command to allow notification listener
        val cmd = "cmd notification allow_listener $componentName"
        android.util.Log.d("RootUtils", "Executing command: $cmd")
        
        return try {
            val success = executeRootCommand(cmd)
            android.util.Log.d("RootUtils", "Command execution result: $success")
            if (!success) {
                 android.util.Log.e("RootUtils", "Failed to execute allow_listener command via root")
                false
            } else {
                true
            }
        } catch (e: Exception) {
            android.util.Log.e("RootUtils", "Exception executing root command", e)
            false
        }
    }

    fun getNotificationListenerComponent(context: Context, targetPackage: String): String? {
        val packageManager = context.packageManager
        val intent = android.content.Intent("android.service.notification.NotificationListenerService")
        intent.setPackage(targetPackage)
        
        val services = packageManager.queryIntentServices(intent, PackageManager.GET_META_DATA)
        if (services.isEmpty()) {
            android.util.Log.w("RootUtils", "No services found for intent with package $targetPackage")
            return null
        }
        
        val serviceInfo = services[0].serviceInfo
        return "${serviceInfo.packageName}/${serviceInfo.name}"
    }

    private fun executeRootCommand(command: String): Boolean {
        var process: Process? = null
        var os: DataOutputStream? = null
        return try {
            process = Runtime.getRuntime().exec("su")
            os = DataOutputStream(process.outputStream)
            os.writeBytes("$command\n")
            os.writeBytes("exit\n")
            os.flush()
            val exitValue = process.waitFor()
            android.util.Log.d("RootUtils", "Root command exit value: $exitValue")
            exitValue == 0
        } catch (e: IOException) {
             android.util.Log.e("RootUtils", "IOException in executeRootCommand", e)
            false
        } catch (e: InterruptedException) {
             android.util.Log.e("RootUtils", "InterruptedException in executeRootCommand", e)
            false
        } finally {
            try {
                os?.close()
                process?.destroy()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
