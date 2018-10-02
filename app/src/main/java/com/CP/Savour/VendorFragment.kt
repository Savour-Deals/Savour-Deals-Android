package com.CP.Savour

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.LinearLayoutManager
import com.firebase.geofire.*

import com.google.firebase.database.*
import com.google.firebase.database.DataSnapshot

import com.firebase.geofire.GeoLocation
import com.google.firebase.database.DatabaseError
import com.firebase.geofire.GeoQueryEventListener
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_vendor.*


class VendorFragment : Fragment() {

    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null
    private lateinit var recyclerView : RecyclerView
    private var toolbar : ActionBar? = null
    var geoRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors_Location")
    var geoFire = GeoFire(geoRef);

    val vendors = mutableMapOf<String, Vendor?>()

    var firstLocationUpdate = true
    var geoQuery:GeoQuery? = null
    var  vendorReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors")

    private var mLocationRequest: LocationRequest? = null
    private var myLocation: Location? = null

    private val UPDATE_INTERVAL = (30 * 1000).toLong()  /* 30 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        onCreate(savedInstanceState)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        setRetainInstance(true)


        // retrieving the vendors from the database
        layoutManager = LinearLayoutManager(context)

        startLocationUpdates()

        return inflater.inflate(R.layout.fragment_vendor, container, false)
    }

    companion object {
        fun newInstance(): VendorFragment = VendorFragment()
    }





    private fun getFirebaseData(lat:Double, lng:Double) {
        geoQuery = geoFire.queryAtLocation(GeoLocation(lat, lng), 80.5) // About 50 mile query

        geoQuery!!.addGeoQueryEventListener(object : GeoQueryEventListener {
            override fun onKeyEntered(key: String, location: GeoLocation) {
                println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude))
                vendorReference.child(key)
                val vendorListener = object : ValueEventListener {
                    /**
                     * Listening for when the data has been changed
                     * and also when we want to access f
                     */
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {

                            //convert to android Location object
                            val vendorLocation = Location("")
                            vendorLocation.latitude = location.latitude
                            vendorLocation.longitude = location.longitude

                            vendors.put(dataSnapshot.key!!,Vendor(dataSnapshot,myLocation!!,vendorLocation))

                            adapter = RecyclerAdapter(ArrayList(vendors.values), context!!)

                            vendor_list.layoutManager = layoutManager

                            vendor_list.adapter = adapter
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                }
                vendorReference.child(key).addValueEventListener(vendorListener)
            }

            override fun onKeyExited(key: String) {
                println(String.format("Key %s is no longer in the search area", key))
                vendors.remove(key)
                adapter = RecyclerAdapter(ArrayList(vendors.values), context!!)

                vendor_list.layoutManager = layoutManager

                vendor_list.adapter = adapter
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude))
            }

            override fun onGeoQueryReady() {
//                println("All initial data has been loaded and events have been fired!")
            }

            override fun onGeoQueryError(error: DatabaseError) {
                System.err.println("There was an error with this query: $error")
            }
        })

    }

    // Trigger new location updates at interval
    protected fun startLocationUpdates() {

        // Create the location request to start receiving updates
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
        val settingsClient = LocationServices.getSettingsClient(this.activity!!)
        settingsClient.checkLocationSettings(locationSettingsRequest)

        val mLocationCallback = object : com.google.android.gms.location.LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                onLocationChanged(locationResult!!.getLastLocation())
            }

        }
        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        if(Build.VERSION.SDK_INT >= 19 && checkPermission()) {
            getFusedLocationProviderClient(this.activity!!).requestLocationUpdates(mLocationRequest!!,mLocationCallback, Looper.myLooper())
        }

    }

    private fun checkPermission() : Boolean {
        if (ContextCompat.checkSelfPermission(this.context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions()
            return false
        }
    }

    fun onLocationChanged(location: Location) {
        // New location has now been determined
        this.myLocation = location
        if(firstLocationUpdate){
            firstLocationUpdate = false
            getFirebaseData(location.latitude,location.longitude)
        }else{
            geoQuery!!.center = GeoLocation(location.latitude, location.longitude)
        }
    }

    private fun registerLocationListner() {
        // initialize location callback object
        val locationCallback = object : com.google.android.gms.location.LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                onLocationChanged(locationResult!!.getLastLocation())
            }
        }

        // add permission if android version is greater then 23
        if(Build.VERSION.SDK_INT >= 23 && checkPermission()) {
            LocationServices.getFusedLocationProviderClient(this.activity!!).requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper())
        }

    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this.activity!!, arrayOf("Manifest.permission.ACCESS_FINE_LOCATION"),1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1) {
            if (permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION ) {
                registerLocationListner()
            }
        }
    }



}
