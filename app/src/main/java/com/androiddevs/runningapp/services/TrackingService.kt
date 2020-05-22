package com.androiddevs.runningapp.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.*
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.ui.HomeActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

private const val CHANNEL_ID = "tracking_channel"

private const val SERVICE_ID = 0

const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

class TrackingService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                ACTION_START_SERVICE -> {
                    startForegroundService()
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused Service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service.")
                    stopSelf()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        Timber.d("TrackingService started.")
        val intent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val pauseIntent = Intent(ACTION_PAUSE_SERVICE)
        val pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, 0)

        val stopIntent = Intent(ACTION_STOP_SERVICE)
        val stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, 0)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
            .addAction(R.drawable.ic_pause_black_24dp, "Pause", pausePendingIntent)
            .addAction(R.drawable.ic_stop_black_24dp, "Stop", stopPendingIntent)
            .setContentTitle("Running App")

        var notification = notificationBuilder
            .setContentText("00:00")

        startForeground(SERVICE_ID, notification.build())
        notificationManager.notify(0, notification.build())


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }
}