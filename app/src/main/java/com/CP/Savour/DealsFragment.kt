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
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.CP.Savour.R.id.deal_list
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
import com.google.zxing.client.result.TextParsedResult
import kotlinx.android.synthetic.main.fragment_deals.*
import kotlinx.android.synthetic.main.fragment_vendor.*
import org.joda.time.DateTime
import java.time.DateTimeException


class DealsFragment : Fragment() {
    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerView.Adapter<DealsRecyclerAdapter.ViewHolder>? = null
    private lateinit var recyclerView : RecyclerView
    private var toolbar : ActionBar? = null
    private lateinit var savourImg: ImageView
    private lateinit var locationMessage: TextView
    private lateinit var locationButton: Button
    var geoRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors_Location")
    var geoFire = GeoFire(geoRef)
    val user = FirebaseAuth.getInstance().currentUser


    private lateinit var mAuth: FirebaseAuth
    private lateinit var authStateListner: FirebaseAuth.AuthStateListener

    var dealsArray : List<Deal?> = arrayListOf()
    var activedeals = mutableMapOf<String, Deal?>()
    var inactivedeals = mutableMapOf<String, Deal?>()

    val vendors = mutableMapOf<String, Vendor?>()

    var geoQuery: GeoQuery? = null

    var dealsReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Deals")
    val favoriteRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid).child("favorites")
    var vendorReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors")

    private var dealsListener: ValueEventListener? = null
    private var favoritesListener: ValueEventListener? = null
    private var vendorListener: ValueEventListener? = null

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

        // retrieving the vendors from the database
        layoutManager = LinearLayoutManager(context)

        // Inflate the layout for this fragment
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
        if (favoritesListener != null){
            favoriteRef.removeEventListener(favoritesListener!!)
        }
        if (vendorListener != null){
            vendorReference.removeEventListener(vendorListener!!)
        }
        if (dealsListener != null){
            dealsReference.removeEventListener(dealsListener!!)
        }
        if (geoQuery != null) {
            geoQuery!!.removeAllListeners()
        }
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

        val userID = user!!.uid

        var  dealsReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Deals")
        val user = FirebaseAuth.getInstance().currentUser
        val favoriteRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid).child("favorites")

        var favUpdated = false
        var favorites = mutableMapOf<String,String>()


        favoritesListener = object : ValueEventListener {//Get favorites
            /**
             * Listening for when the data has been changed
             * and also when we want to access f
             */
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    favorites = dataSnapshot.value as MutableMap<String, String>
                    for (deal in activedeals){
                        deal.value!!.favorited = favorites.containsKey(deal.key)
                    }
                    for (deal in inactivedeals){
                        deal.value!!.favorited = favorites.containsKey(deal.key)
                    }
                    if (favUpdated){
                        if(deal_list != null){
                            dealsArray = ArrayList(activedeals.values).sortedBy { deal -> deal!!.distanceMiles } + ArrayList(inactivedeals.values).sortedBy { deal -> deal!!.distanceMiles }
                            deal_list.adapter!!.notifyDataSetChanged()
                        }
                    }
                }else{
                    favorites.clear()
                    for (deal in activedeals){
                        deal.value!!.favorited = false
                    }
                    for (deal in inactivedeals){
                        deal.value!!.favorited = false
                    }
                    if (favUpdated){
                        if(deal_list != null){
                            dealsArray = ArrayList(activedeals.values).sortedBy { deal -> deal!!.distanceMiles } + ArrayList(inactivedeals.values).sortedBy { deal -> deal!!.distanceMiles }
                            deal_list.adapter!!.notifyDataSetChanged()
                        }
                    }
                }
                if (!favUpdated){ //DONT redo geofire and deals if
                    favUpdated = true
                    geoQuery = geoFire.queryAtLocation(GeoLocation(lat, lng), 80.5) // About 50 mile query

                    geoQuery!!.addGeoQueryEventListener(object : GeoQueryEventListener {
                        override fun onKeyEntered(key: String, location: GeoLocation) { //Location Entered! Get its info!
                            println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude))
                            vendorListener = object : ValueEventListener {
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

                                        dealsListener = object : ValueEventListener {//Now  get its deals!
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
                                                    dealsArray = ArrayList(activedeals.values).sortedBy { deal -> deal!!.distanceMiles } + ArrayList(inactivedeals.values).sortedBy { deal -> deal!!.distanceMiles }


                                                    adapter = DealsRecyclerAdapter(dealsArray,vendors, context!!)

                                                    deal_list.layoutManager = layoutManager

                                                    deal_list.adapter = adapter


                                                }
                                            }

                                            override fun onCancelled(databaseError: DatabaseError) {
                                            }
                                        }
                                        dealsReference.orderByChild("vendor_id").equalTo(dataSnapshot.key!!).addValueEventListener(dealsListener!!)
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                }
                            }
                            vendorReference.child(key).addValueEventListener(vendorListener!!)
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

                            dealsArray =  ArrayList(activedeals.values).sortedBy { deal -> deal!!.distanceMiles } + ArrayList(inactivedeals.values).sortedBy { deal -> deal!!.distanceMiles }

                            adapter = DealsRecyclerAdapter(dealsArray,vendors, context!!)

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
        favoriteRef.addValueEventListener(favoritesListener!!)
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
            registerLocationListner()

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
        myLocation = location
        if (myLocation != null){
            if(firstLocationUpdate){
                firstLocationUpdate = false
                getFirebaseData(location.latitude,location.longitude)
            }else{
                //recalculate distances and update recycler
                if (geoQuery != null) {
                    geoQuery!!.center = GeoLocation(location.latitude, location.longitude)
                    for (deal in activedeals){
                        if (vendors[deal.value!!.vendorID] != null){
                            deal.value!!.updateDistance(vendors[deal.value!!.vendorID]!!, location)
                        }
                    }
                    for (deal in inactivedeals){
                        if (vendors[deal.value!!.vendorID] != null){
                            deal.value!!.updateDistance(vendors[deal.value!!.vendorID]!!, location)
                        }
                    }
                    if (deal_list != null){
                        dealsArray =  ArrayList(activedeals.values).sortedBy { deal -> deal!!.distanceMiles } + ArrayList(inactivedeals.values).sortedBy { deal -> deal!!.distanceMiles }
                        adapter = DealsRecyclerAdapter(dealsArray,vendors, context!!)
                        deal_list.layoutManager = layoutManager

                        deal_list.adapter = adapter
                    }
                }
            }
        }

    }

    private fun registerLocationListner() {
        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest()
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        mLocationRequest!!.setInterval(UPDATE_INTERVAL)
        mLocationRequest!!.setFastestInterval(FASTEST_INTERVAL)

        locationMessage!!.visibility = View.INVISIBLE
        locationButton!!.visibility = View.INVISIBLE

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
