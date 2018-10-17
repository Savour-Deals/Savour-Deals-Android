package com.CP.Savour


import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_deals.*
import kotlinx.android.synthetic.main.fragment_vendor.*
import org.joda.time.DateTime
import java.time.DateTimeException


class DealsFragment : Fragment() {
    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerView.Adapter<DealsRecyclerAdapter.ViewHolder>? = null
    private lateinit var recyclerView : RecyclerView
    private var toolbar : ActionBar? = null
    private var savourImg: ImageView? = null
    var geoRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors_Location")
    var geoFire = GeoFire(geoRef);

    val vendors = mutableMapOf<String, Vendor?>()

    var geoQuery: GeoQuery? = null
    var  vendorReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors")

    var firstLocationUpdate = true

    private var mLocationRequest: LocationRequest? = null
    private var myLocation: Location? = null

    private val UPDATE_INTERVAL = (30 * 1000).toLong()  /* 30 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        onCreate(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        setRetainInstance(true)

        val view = inflater.inflate(R.layout.fragment_deals, container, false)
        // grabbing the search bar
        //val searchBar = view!!.findViewById(R.id.deal_search) as SearchView

        //val query = searchBar.query
        // adding a listener to the search bar

        savourImg = view.findViewById(R.id.imageView5) as ImageView

        Glide.with(this)
                .load(R.drawable.savour_white)
                .into(savourImg!!)

        // retrieving the vendors from the database
        layoutManager = LinearLayoutManager(context)
        startLocationUpdates()

        // Inflate the layout for this fragment
        return view
    }


    companion object {
        fun newInstance(): DealsFragment = DealsFragment()
    }

    private fun search(search: SearchView) {
        search.setOnQueryTextListener( object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
    }
    private fun getFirebaseData(lat:Double, lng:Double) {
        var favUpdated = false
        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        var activedeals = mutableMapOf<String, Deal?>()
        var inactivedeals = mutableMapOf<String, Deal?>()
        var favorites = mutableMapOf<String,String>()

        var  dealsReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Deals")
        val user = FirebaseAuth.getInstance().currentUser
        val favoriteRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid).child("favorites")

        val favoritesListener = object : ValueEventListener {//Get favorites
            /**
             * Listening for when the data has been changed
             * and also when we want to access f
             */
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    favorites = dataSnapshot.value as MutableMap<String, String>
                }
                if (!favUpdated){ //DONT redo geofire and deals if
                    geoQuery = geoFire.queryAtLocation(GeoLocation(lat, lng), 80.5) // About 50 mile query

                    geoQuery!!.addGeoQueryEventListener(object : GeoQueryEventListener {
                        override fun onKeyEntered(key: String, location: GeoLocation) { //Location Entered! Get its info!
                            println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude))
                            val vendorListener = object : ValueEventListener {
                                /**
                                 * Listening for when the data has been changed
                                 * and also when we want to access f
                                 */
                                override fun onDataChange(dataSnapshot: DataSnapshot) { //Got Location's Info
                                    if (dataSnapshot.exists()) {

                                        //convert to android Location object
                                        val vendorLocation = Location("")
                                        vendorLocation.latitude = location.latitude
                                        vendorLocation.longitude = location.longitude

                                        vendors.put(dataSnapshot.key!!,Vendor(dataSnapshot,myLocation!!,vendorLocation))

                                        val dealsListener = object : ValueEventListener {//Now  get its deals!
                                            /**
                                             * Listening for when the data has been changed
                                             * and also when we want to access f
                                             */
                                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                if (dataSnapshot.exists()) {

                                                    for (dealSnapshot in dataSnapshot.children) {
                                                        val temp = Deal(dealSnapshot,myLocation!!,vendorLocation,userID, favorites)

                                                        //if the deal is not expired or redeemed less than half an hour ago, show it
                                                        if (temp.isAvailable()){
                                                            if (temp.active!!){
                                                                activedeals[temp.id!!] = temp
                                                                inactivedeals.remove(temp.id!!)
                                                            }else{
                                                                inactivedeals[temp.id!!] = temp
                                                                activedeals.remove(temp.id!!)
                                                            }
                                                        }else if (temp.redeemedTime != null){
                                                            if (((DateTime().millis/1000) - temp.redeemedTime!!) < 1800){
                                                                activedeals[temp.id!!] = temp
                                                                inactivedeals.remove(temp.id!!)
                                                            }
                                                        }else{
                                                            activedeals.remove(temp.id!!)
                                                            inactivedeals.remove(temp.id!!)
                                                        }
                                                    }
                                                    val dealsArray =  ArrayList(activedeals.values).sortedBy { deal -> deal!!.distanceMiles } + ArrayList(inactivedeals.values).sortedBy { deal -> deal!!.distanceMiles }

                                                    adapter = DealsRecyclerAdapter(dealsArray, context!!)

                                                    deal_list.layoutManager = layoutManager

                                                    deal_list.adapter = adapter
                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                            }
                                        }
                                        dealsReference.orderByChild("vendor_id").equalTo(dataSnapshot.key!!).addValueEventListener(dealsListener)
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

                            for (deal in activedeals){
                                if (deal.value!!.vendorID == key){
                                    activedeals.remove(deal.key)
                                }
                            }
                            for (deal in inactivedeals){
                                if (deal.value!!.vendorID == key){
                                    inactivedeals.remove(deal.key)
                                }
                            }

                            val dealsArray =  ArrayList(activedeals.values).sortedBy { deal -> deal!!.distanceMiles } + ArrayList(inactivedeals.values).sortedBy { deal -> deal!!.distanceMiles }

                            adapter = DealsRecyclerAdapter(dealsArray, context!!)

                            deal_list.layoutManager = layoutManager

                            deal_list.adapter = adapter

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
            LocationServices.getFusedLocationProviderClient(this.activity!!).requestLocationUpdates(mLocationRequest!!,mLocationCallback, Looper.myLooper())
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
        }else{
            geoQuery!!.center = GeoLocation(location.latitude, location.longitude)
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
