package com.example.runningtracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.runningtracker.R
import com.example.runningtracker.ui.MainActivity
import com.example.runningtracker.utils.Constants.ACTION_PAUSE_SERVICE
import com.example.runningtracker.utils.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.runningtracker.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runningtracker.utils.Constants.ACTION_STOP_SERVICE
import com.example.runningtracker.utils.Constants.FASTEST_LOCATION_INTERVAL
import com.example.runningtracker.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.example.runningtracker.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.runningtracker.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.runningtracker.utils.Constants.NOTIFICATION_ID
import com.example.runningtracker.utils.Constants.TIMER_UPDATE_INTERVAL
import com.example.runningtracker.utils.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


typealias Polyline = MutableList<LatLng> /// LatLng -> the coordinate format of the class
typealias PolyLines = MutableList<Polyline>

/// THE DIFF BETWEEN A BACKGROUND AND FOREGROUND IS THAT THE FOREGROUND SERVICE MUST COME WITH A NOTIFICATION
@AndroidEntryPoint
class TrackingService : LifecycleService(){

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    lateinit var curNotificationBuilder:NotificationCompat.Builder

    // this is not a companion object because we need this variable only under this service and not outside of this service
    private val timeRunInSeconds = MutableLiveData<Long>()


    companion object{ // work as static object in java
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<PolyLines>() /// see type alias  or MutableLiveData<MutableList<MutableList<LatLng>>>

        val timeRunInMillis = MutableLiveData<Long>()
    }

    override fun onCreate() {
        super.onCreate()

        curNotificationBuilder = baseNotificationBuilder

        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        isTracking.observe(this , Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun postInitialValues(){
        /// INITIALIZATION
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }


    ///  ****   HANDLING THE TIME STAMPS  ****  ///

    private var isTimerEnabled = false
    private var lapTime = 0L // time between clicking on the start to clicking on the pause button
    private var timeRun = 0L // this is the total time run (all of our lap times added together)
    private var timeStarted = 0L // time stamp when we started the timer
    private var lastSecondTimeStamp = 0L // last whole second value that has passed in milli seconds

    private fun startTimer(){
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!){
                lapTime = System.currentTimeMillis() - timeStarted // time diff between now and time started
                timeRunInMillis.postValue(timeRun + lapTime)

                if (timeRunInMillis.value!! >= lastSecondTimeStamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L // that is adding total 1 seconds in ms type
                }
                delay(TIMER_UPDATE_INTERVAL) // we will delay the coroutine so that we do not update our observes everytime
            }

            timeRun += lapTime
        }

    }

    //////////////////////////////////////////////

    /// when we pause our run and again resume it then we need an newly initialized list to to add new coordinates
    private fun addEmptyPolyline() = pathPoints.value?.apply { /// here we are talking about innermost list
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf())) /// if value is null

    private fun addPathPoint(location: Location?){
        location?.let { // means location is not null
            val position = LatLng(location.latitude , location.longitude)

            pathPoints.value?.apply {
                last().add(position)   /// last() will give last list and in that list we will add(position)
                pathPoints.postValue(this)
            }
        }
    }

    private val locationCallback = object : LocationCallback(){
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            if (isTracking.value!!){
                result?.locations?.let { locations -> /// this result will only have one value not all tje location points travelled
                    for (location in locations){
                        addPathPoint(location)

                        Timber.d("NEW LOCATION: ${location.latitude} , ${location.latitude}")
                    }
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking : Boolean){
        if (isTracking){
            /// if we have permissions to track
            if (TrackingUtility.hasLocationPermission(this)){
                val request = LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }

                fusedLocationProviderClient.requestLocationUpdates(
                    request ,
                    locationCallback ,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("resuming service")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stop service")
                    killService()
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled = false
    }


    // to create a notification we need a notification channel
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
              NOTIFICATION_CHANNEL_ID
            , NOTIFICATION_CHANNEL_NAME
            , NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }


    private fun startForegroundService(){
        Timber.d("foreground service running")

        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID , baseNotificationBuilder.build())

        timeRunInSeconds.observe(this , Observer {
            if (!serviceKilled){
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it*1000 , false))
                notificationManager.notify(NOTIFICATION_ID , notification.build())
            }
        })
    }

    private fun updateNotificationTrackingState(isTracking: Boolean){

        val notificationActionText = if (isTracking) "Pause" else "Resume"

        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this , TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this , 1 , pauseIntent , FLAG_IMMUTABLE)
        } else {
            val resumeIntent = Intent(this , TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this , 2 , resumeIntent , FLAG_IMMUTABLE)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // to remove actions that are currently in the notification
        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true /* allowed to modify that */
            set(curNotificationBuilder , ArrayList<NotificationCompat.Action>()) /// remove all actions before we update it with new actions
        }

        if (!serviceKilled){
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp , notificationActionText  , pendingIntent)

            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }


}
