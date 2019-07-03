package com.lenta.shared.platform.high_priority


import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.lenta.shared.R

class MainService : Service() {


    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (ACTION_HIGH_PRIORITY == action) {

            showNotification()

        } else if (ACTION_LOW_PRIORITY == action) {
            stopForeground(true)
        }


        return START_STICKY
    }

    private fun showNotification() {

        val notificationIntent = packageManager.getLaunchIntentForPackage(applicationContext.packageName)?.apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0)

        val mBuilder = NotificationCompat.Builder(this, "GENERAL")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.high_priority_title))
                .setContentText(getString(R.string.high_priority_content))
                .setContentIntent(pendingIntent)

        startForeground(NOTIFICATION_ID, mBuilder.build())
    }

    companion object {
        val ACTION_HIGH_PRIORITY = "ACTION_HIGH_PRIORITY"
        val ACTION_LOW_PRIORITY = "ACTION_LOW_PRIORITY"
        private val NOTIFICATION_ID = 1222221
    }


}
