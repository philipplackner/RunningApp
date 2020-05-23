package com.androiddevs.runningapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.*
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import com.androiddevs.runningapp.R
import com.androiddevs.runningapp.other.Constants
import com.androiddevs.runningapp.other.Constants.Companion.LOCATION_PROVIDER
import com.androiddevs.runningapp.other.Constants.Companion.MIN_LOCATION_UPDATE_DISTANCE
import com.androiddevs.runningapp.other.Constants.Companion.MIN_LOCATION_UPDATE_INTERVAL
import com.androiddevs.runningapp.other.TrackingUtility
import com.androiddevs.runningapp.ui.HomeActivity
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import timber.log.Timber

private const val CHANNEL_ID = "tracking_channel"
private const val NOTIFICATION_ID = 1

private const val SERVICE_ID = 1

const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

class TrackingService : Service(), LocationListener {

    val timeRunInMillis = MutableLiveData<Long>()
    val isTracking = MutableLiveData<Boolean>()
    val pathPoints = MutableLiveData<MutableList<LatLng>>()

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L

    private val timeRunInSeconds = MutableLiveData<Long>()
    private var lastSecondTimestamp = 0L

    private val binder = TrackingBinder()

    inner class TrackingBinder : Binder() {
        val service = this@TrackingService
    }

    init {
        timeRunInMillis.postValue(0L)
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        isTracking.observeForever {
            updateLocationChecking()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_SERVICE -> {
                    startForegroundService()
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused Service")
                    pauseTimer()
                    isTracking.postValue(false)
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service.")
                    stopSelf()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationChecking() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (isTracking.value!!) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                locationManager.requestLocationUpdates(
                    LOCATION_PROVIDER,
                    MIN_LOCATION_UPDATE_INTERVAL,
                    MIN_LOCATION_UPDATE_DISTANCE,
                    this
                )
                Timber.d("Tracking started")
            }
        } else {
            locationManager.removeUpdates(this)
            Timber.d("Tracking stopped")
        }
    }

    private fun startTimer() {
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        CoroutineScope(Dispatchers.Main).launch {
            while (isTimerEnabled) {
                lapTime = System.currentTimeMillis() - timeStarted
                timeRunInMillis.postValue(timeRun + lapTime)
                if (timeRunInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(Constants.TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseTimer() {
        isTimerEnabled = false
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    override fun onLocationChanged(newLocation: Location?) {
        if (isTracking.value!!) {
            addPathPoint(newLocation)
            Timber.d("Location changed: (${newLocation?.latitude}, ${newLocation?.longitude})")
        }
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Timber.d("Status changed: $status")
    }

    override fun onProviderEnabled(provider: String?) {
        Timber.d("Provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String?) {
        Timber.d("Provider disabled: $provider")
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

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
            .setContentText("00:00:00")

        startForeground(SERVICE_ID, notification.build())
        notificationManager.notify(NOTIFICATION_ID, notification.build())
        startTimer()
        isTracking.postValue(true)

        // updating notification
        timeRunInSeconds.observeForever {
            notification = notificationBuilder
                .setContentText(TrackingUtility.getFormattedPreviewTimeWithMillis(it * 1000L))
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }
}