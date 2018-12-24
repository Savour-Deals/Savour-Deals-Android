package com.CP.Savour

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.CP.Savour.R.id.deal_list
import com.bumptech.glide.Glide
import com.firebase.geofire.*
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_deals.*
import org.joda.time.DateTime

class FavoritesFragment : Fragment() {

    private var savourImg: ImageView? = null
    private var layoutManager : RecyclerView.LayoutManager? = null
    val vendors = mutableMapOf<String, Vendor?>()

    private var adapter : RecyclerView.Adapter<DealsRecyclerAdapter.ViewHolder>? = null
    var nodealsText: TextView? = null
    private lateinit var locationMessage: TextView
    private lateinit var locationButton: Button

    var firstLocationUpdate = true

    private lateinit var mAuth: FirebaseAuth
    private lateinit var authStateListner: FirebaseAuth.AuthStateListener

    var geoRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors_Location")
    var geoFire = GeoFire(geoRef)
    var  vendorReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors")

    private lateinit var dealsListener: ValueEventListener

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

        val view = inflater.inflate(R.layout.fragment_favorites, container, false)

        layoutManager = LinearLayoutManager(context)

        savourImg = view.findViewById(R.id.imageView5) as ImageView
        nodealsText = view.findViewById(R.id.textView2)
        locationMessage = view.findViewById(R.id.locationMessage) as TextView
        locationButton = view.findViewById(R.id.location_button) as Button

        locationButton.setOnClickListener {
            var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity!!.getPackageName())).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity!!.startActivity(intent)
        }

        Glide.with(this)
                .load(R.drawable.savour_white)
                .into(savourImg!!)
        return view
    }

    override fun onStart() {
        super.onStart()
        mAuth = FirebaseAuth.getInstance()
        authStateListner = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if(user != null){
                startLocationUpdates()
            }
        }
        mAuth.addAuthStateListener(authStateListner)
    }

    override fun onPause() {
        super.onPause()
        if (authStateListner != null){
            mAuth.removeAuthStateListener(authStateListner)
        }
    }
    companion object {
        fun newInstance(): FavoritesFragment = FavoritesFragment()
    }

    private fun getFirebaseData(lat:Double, lng:Double) {
        val userID = FirebaseAuth.getInstance().currentUser!!.uid

        var dealsArray: List<Deal?>
        var oldFavs = ArrayList<String>()

        var  dealsReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Deals")
        val user = FirebaseAuth.getInstance().currentUser
        val favoriteRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid).child("favorites")
        var activedeals = mutableMapOf<String, Deal?>()
        var inactivedeals = mutableMapOf<String, Deal?>()

        val favoritesListener = object : ValueEventListener {//Get favorites
            /**
             * Listening for when the data has been changed
             * and also when we want to access f
             */
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    nodealsText!!.setVisibility(View.INVISIBLE)
                    var favorites = dataSnapshot.value as MutableMap<String, String>
                    var newFavs = ArrayList(favorites.values)


                    for (favid in newFavs) {
                        dealsListener = object : ValueEventListener {//Now  get its deals!
                            /**
                             * Listening for when the data has been changed
                             * and also when we want to access f
                             */
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    geoFire.getLocation(dataSnapshot.child("vendor_id").value.toString(),  object: LocationCallback {
                                        override fun onLocationResult(key: String?, location: GeoLocation?) {
                                          if (location != null){
                                              val vendorLocation = Location("")
                                              vendorLocation.latitude = location!!.latitude
                                              vendorLocation.longitude = location!!.longitude
                                              val temp = Deal(dataSnapshot,myLocation!!,vendorLocation,userID, favorites)

                                              val dealsListener = object : ValueEventListener {//Now  get its deals!
                                                  /**
                                                   * Listening for when the data has been changed
                                                   * and also when we want to access f
                                                   */
                                                  override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                      vendors.put(dataSnapshot.key!!,Vendor(dataSnapshot,myLocation!!,vendorLocation))
                                                      if (dataSnapshot.exists()) {

                                                          //if the deal is not expired or redeemed less than half an hour ago, show it
                                                          if (temp.isAvailable()) {
                                                              if (temp.active!!) {
                                                                  activedeals[temp.id!!] = temp
                                                                  inactivedeals.remove(temp.id!!)
                                                              } else {
                                                                  inactivedeals[temp.id!!] = temp
                                                                  activedeals.remove(temp.id!!)
                                                              }
                                                          } else if (temp.redeemedTime != null) {
                                                              if (((DateTime().millis / 1000) - temp.redeemedTime!!) < 1800) {
                                                                  activedeals[temp.id!!] = temp
                                                                  inactivedeals.remove(temp.id!!)
                                                              }
                                                          } else {
                                                              activedeals.remove(temp.id!!)
                                                              inactivedeals.remove(temp.id!!)
                                                          }

                                                          dealsArray = ArrayList(activedeals.values).sortedBy { deal -> deal!!.distanceMiles } + ArrayList(inactivedeals.values).sortedBy { deal -> deal!!.distanceMiles }

                                                          if (dealsArray.count() < 1) {
                                                              nodealsText!!.setVisibility(View.VISIBLE)
                                                              dealsArray = ArrayList()
                                                          } else {
                                                              nodealsText!!.setVisibility(View.INVISIBLE)
                                                          }
                                                          adapter = DealsRecyclerAdapter(dealsArray, vendors, context!!)

                                                          deal_list.layoutManager = layoutManager

                                                          deal_list.adapter = adapter
                                                      }
                                                  }

                                                  override fun onCancelled(databaseError: DatabaseError) {
                                                  }
                                              }
                                              vendorReference.child(key!!).addValueEventListener(dealsListener)

                                          }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError?) {
                                        }

                                    })


                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                            }
                        }
                        dealsReference.child(favid).addValueEventListener(dealsListener)
                    }
                    for (favid in oldFavs){
                        if (!newFavs.contains(favid)){
                            dealsReference.child(favid).removeEventListener(dealsListener)
                            activedeals.remove(favid)
                            inactivedeals.remove(favid)
                            dealsArray = ArrayList(activedeals.values).sortedBy { deal -> deal!!.distanceMiles } + ArrayList(inactivedeals.values).sortedBy { deal -> deal!!.distanceMiles }

                            if (dealsArray.count() < 1) {
                                nodealsText!!.setVisibility(View.VISIBLE)
                                dealsArray = ArrayList()
                            } else {
                                nodealsText!!.setVisibility(View.INVISIBLE)
                            }
                            adapter = DealsRecyclerAdapter(dealsArray, vendors, context!!)

                            deal_list.layoutManager = layoutManager
                            deal_list.adapter!!.notifyDataSetChanged()
                        }
                    }
                    oldFavs = newFavs
                }else{//If no favorites
                    nodealsText!!.setVisibility(View.VISIBLE)
                    dealsArray = ArrayList()

                    adapter = DealsRecyclerAdapter(dealsArray,vendors,context!!)

                    deal_list.layoutManager = layoutManager

                    deal_list.adapter = adapter
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        favoriteRef.addValueEventListener(favoritesListener)
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
            locationMessage!!.visibility = View.INVISIBLE
            locationButton!!.visibility = View.INVISIBLE
            LocationServices.getFusedLocationProviderClient(this.activity!!).requestLocationUpdates(mLocationRequest!!,mLocationCallback, Looper.myLooper())
        }else{
            //location not on. Tell user to turn it on
            locationMessage!!.visibility = View.VISIBLE
            locationButton!!.visibility = View.VISIBLE
        }

    }

    private fun checkPermission() : Boolean {
        if (ContextCompat.checkSelfPermission(this.context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
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
        }
    }

    private fun registerLocationListner() {
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

        // initialize location callback object
        val mLocationCallback = object : com.google.android.gms.location.LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                onLocationChanged(locationResult!!.getLastLocation())
            }

        }

        // add permission if android version is greater then 23
        if(Build.VERSION.SDK_INT >= 19 && checkPermission()) {
            LocationServices.getFusedLocationProviderClient(this.activity!!).requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
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

