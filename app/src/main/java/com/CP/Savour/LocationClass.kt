package com.CP.Savour

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.os.Build
import android.os.Looper
import com.google.android.gms.location.*

class LocationService {
    var ready = false
    var startedUpdates = false
    var currentLocation: Location? = null

    private var mLocationRequest: LocationRequest? = null
    private var mLocationCallback: LocationCallback? = null

    private var UPDATE_INTERVAL: Long = 0
    private var FASTEST_INTERVAL: Long = 0
    private var parentActivity: Activity
    private lateinit var locationCallback: (Location) -> Unit


    //30s and 2s
    constructor(update_interval: Long = 2000, fastest_interval: Long = 30000, pActivity: Activity,callback: ((Location) -> Unit)? = null){
        parentActivity = pActivity
        UPDATE_INTERVAL = update_interval
        FASTEST_INTERVAL = fastest_interval
        if (callback != null){
            locationCallback = callback!!
        }
//        startLocationUpdates()
    }

    // Trigger new location updates at interval
    @SuppressLint("MissingPermission") //we should not of made it here without permissions
    public fun startLocationUpdates() {

        // Create the location request to start receiving updates
        startedUpdates = true
        mLocationRequest = LocationRequest()
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest!!.setInterval(UPDATE_INTERVAL)
        mLocationRequest!!.setFastestInterval(FASTEST_INTERVAL)

        // Create LocationSettingsRequest object using location request
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        val settingsClient = LocationServices.getSettingsClient(this.parentActivity)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mLocationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult != null){
                    onLocationUpdated(locationResult!!.getLastLocation())
                }
            }

        }
        if(Build.VERSION.SDK_INT >= 23) {
            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
            LocationServices.getFusedLocationProviderClient(parentActivity).requestLocationUpdates(mLocationRequest!!, mLocationCallback, Looper.myLooper())
        }

    }

    private fun onLocationUpdated(newLocation: Location){
        currentLocation = newLocation
        ready  = true
        if (locationCallback != null){
            locationCallback(newLocation)
        }
    }

    fun cancel(){
        LocationServices.getFusedLocationProviderClient(parentActivity).removeLocationUpdates(mLocationCallback)
    }
}

