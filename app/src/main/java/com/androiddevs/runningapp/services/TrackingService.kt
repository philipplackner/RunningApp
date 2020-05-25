package com.androiddevs.runningapp.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.*
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
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
import com.androiddevs.runningapp.other.Constants.Companion.ACTION_SHOW_TRACKING_FRAGMENT
import com.androiddevs.runningapp.other.Constants.Companion.LOCATION_PROVIDER
import com.androiddevs.runningapp.other.Constants.Companion.MIN_LOCATION_UPDATE_DISTANCE
import com.androiddevs.runningapp.other.Constants.Companion.MIN_LOCATION_UPDATE_INTERVAL
import com.androiddevs.runningapp.other.TrackingUtility
import com.androiddevs.runningapp.ui.HomeActivity
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.*
import timber.log.Timber

private const val CHANNEL_ID = "tracking_channel"
private const val NOTIFICATION_ID = 1

private const val SERVICE_ID = 1

const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_SERVICE"
const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"

class TrackingService : LifecycleService(), LocationListener {

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L

    private val timeRunInSeconds = MutableLiveData<Long>()
    private var lastSecondTimestamp = 0L

    private var isFirstRun = true

    companion object {
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<MutableList<MutableList<LatLng>>>()
    }

    private val baseNotificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
        .setContentTitle("Running App")
        .setContentText("00:00:00")

    private var curNotification = baseNotificationBuilder

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("SERVICE: onDestroy")
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("SERVICE: onCreate")
        postInitialValues()
        isTracking.observe(this) {
            updateCurNotification(it)
            updateLocationChecking(it)
        }

    }

    private fun postInitialValues() {
        timeRunInMillis.postValue(0L)
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
    }

    private var serviceKilled = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("GOT COMMAND TO START OR RESUME")
                    if(isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                        serviceKilled = false
                    } else {
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused Service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service.")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun killService() {
        serviceKilled = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationChecking(isTracking: Boolean) {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                locationManager.requestLocationUpdates(
                    LOCATION_PROVIDER,
                    MIN_LOCATION_UPDATE_INTERVAL,
                    MIN_LOCATION_UPDATE_DISTANCE,
                    this
                )
            }
        } else {
            locationManager.removeUpdates(this)
        }
    }

    private fun startTimer() {
        isTracking.postValue(true)
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

    private fun pauseService() {
        isTimerEnabled = false
        isTracking.postValue(false)
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().apply {
                    add(pos)
                }
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

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        Timber.d("TrackingService started.")
        addEmptyPolyline()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, curNotification.build())
        curNotification = curNotification.setContentIntent(getActivityPendingIntent())
        startTimer()
        isTracking.postValue(true)

        // updating notification
        timeRunInSeconds.observe(this) {
            if(!serviceKilled) {
                val notification = curNotification
                    .setContentText(TrackingUtility.getFormattedPreviewTimeWithMillis(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
                Timber.d("SERVICE: notify in observe")
            }
        }
    }

    private fun getActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, HomeActivity::class.java).apply {
            action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )

    private fun updateCurNotification(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotification.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotification, ArrayList<NotificationCompat.Action>())
        }

        if(!serviceKilled) {
            curNotification = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, curNotification.build())
        }

        Timber.d("SERVICE: notify in updateCurNotification")

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channelName = "channelName"
        val channel = NotificationChannel(CHANNEL_ID, channelName, IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }
}