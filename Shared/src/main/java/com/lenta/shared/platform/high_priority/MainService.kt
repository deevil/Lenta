package com.lenta.shared.platform.high_priority


import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.lenta.shared.R
import com.lenta.shared.di.CoreInjectHelper
import com.lenta.shared.only_one_app.LockManager
import javax.inject.Inject

class MainService : Service() {
    @Inject
    lateinit var lockManager: LockManager

    override fun onCreate() {
        super.onCreate()
        CoreInjectHelper.provideCoreComponent(this.applicationContext).inject(this)
    }


    override fun onBind(intent: Intent): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (ACTION_HIGH_PRIORITY == action) {
            showNotification()
            lockManager.lock()

        } else if (ACTION_LOW_PRIORITY == action) {
            stopForeground(true)
            lockManager.unlock()
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

        val channelId =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    createNotificationChannel("lenta_service", "General Lenta Service")
                } else {
                    ""
                }


        val mBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getString(R.string.high_priority_title))
                .setContentText(getString(R.string.high_priority_content))
                .setContentIntent(pendingIntent)

        startForeground(NOTIFICATION_ID, mBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


    companion object {
        val ACTION_HIGH_PRIORITY = "ACTION_HIGH_PRIORITY"
        val ACTION_LOW_PRIORITY = "ACTION_LOW_PRIORITY"
        private val NOTIFICATION_ID = 1222221
    }


}
