package com.CP.Savour

import android.Manifest
import android.content.Context
import android.graphics.drawable.ScaleDrawable
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.github.debop.kodatimes.today
import java.util.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.ProgressBar

import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.fragment_deals.*
import org.joda.time.DateTime


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_VENDOR = "vendor"


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ViewVendorFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ViewVendorFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ViewVendorFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var vendor: Vendor
    private lateinit var dealImage: ImageView
    private lateinit var dealsHeader: TextView
    private lateinit var vendorName: TextView
    private lateinit var loyaltyText: TextView
    private lateinit var directionsButton: Button
    private lateinit var followButton: Button
    private lateinit var menuButton: Button
    private lateinit var loyaltyButton: Button
    private lateinit var address: TextView
    private lateinit var hours: TextView
    private lateinit var description: TextView
    private lateinit var seeMore: TextView
    private lateinit var descriptionContainer: ConstraintLayout
    private lateinit var loyaltyProgress: ProgressBar


    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerView.Adapter<DealsViewVendorRecyclerAdapter.ViewHolder>? = null

    var firstLocationUpdate = true

    private var mLocationRequest: LocationRequest? = null
    private var myLocation: Location? = null

    private val UPDATE_INTERVAL = (30 * 1000).toLong()  /* 30 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */

    private var user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private var descriptionExpanded = false

    val favoriteRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid).child("favorites")
    private lateinit var dealsRef: Query
    val userInfoRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid)

    private lateinit var favoritesListener: ValueEventListener
    private lateinit var userListener: ValueEventListener
    private lateinit var dealsListener: ValueEventListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vendor = it.getParcelable(ARG_VENDOR) as Vendor
            dealsRef = FirebaseDatabase.getInstance().getReference("Deals").orderByChild("vendor_id").equalTo(vendor.id)
        }
        startLocationUpdates()

        println("User Info")
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_vendor, container, false)

        dealImage = view.findViewById(R.id.view_vendor_image)
        vendorName = view.findViewById(R.id.view_vendor_name)
        address = view.findViewById(R.id.vendor_address)
        hours = view.findViewById(R.id.vendor_hours)
        description = view.findViewById(R.id.description)
        seeMore = view.findViewById(R.id.see_more)
        descriptionContainer = view.findViewById(R.id.info_container)
        dealsHeader = view.findViewById(R.id.deals_header)

        menuButton = view.findViewById(R.id.vendor_menu)
        directionsButton = view.findViewById(R.id.vendor_directions)
        followButton = view.findViewById(R.id.vendor_follow)

        loyaltyButton = view.findViewById(R.id.checkin_button)
        loyaltyProgress = view.findViewById(R.id.loyalty_progress)
        loyaltyText = view.findViewById(R.id.loyalty_text)

        if(vendor.loyaltyDeal != "") {
            println("Deal Present!")
            println(vendor.loyaltyDeal)
        } else {
            print("vendor deal is not present")
            loyaltyButton!!.visibility = View.INVISIBLE
            loyaltyProgress!!.visibility = View.INVISIBLE
            loyaltyText!!.visibility = View.INVISIBLE
        }
        layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)

        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                println("triggered")

                println(snapshot.toString())

                if (snapshot.child("following").child(vendor.id!!).exists()) {
                    followButton.background = ContextCompat.getDrawable(context!!, R.drawable.vendor_button)
                    followButton.text = "Following"
                    println("Following")



                } else {
                    followButton.background = ContextCompat.getDrawable(context!!, R.drawable.vendor_button_selected)
                    followButton.text = "Follow"
                    println("Follow")

                }

                if (snapshot.child("loyalty").child(vendor.id!!).exists()) {
                    println("userPoints with loyalty already: ")
                    println(snapshot.child("loyalty").child(vendor.id!!).child("count").toString())
                    val points = snapshot.child("loyalty").child(vendor.id!!).child("count").toString() + ""

                } else {
                    println("no userPoints with loyalty already: ")
                    println("vendor info: ")
                    println(vendor.loyaltyCount)
                    loyaltyText.text = "0/" + vendor.loyaltyCount

                }
            }

            override fun onCancelled(dbError: DatabaseError) {
                println("cancelled userListener")
            }
        }
        userInfoRef.addValueEventListener(userListener)




        followButton.setOnClickListener {
            if (followButton.text == "Follow") {

                userInfoRef.child("following").child(vendor.id!!).setValue(true)
                println("set value")



            } else {
                userInfoRef.child("following").child(vendor.id!!).removeValue()
                println("remove")
            }
        }
        vendorName.text = vendor.name
        address.text = vendor.address
        hours.text = vendor.dailyHours[Calendar.DAY_OF_WEEK - 1]
        description.text = vendor.description

        Glide.with(this)
                .load(vendor.photo)
                .into(dealImage)

        descriptionContainer.setOnClickListener {
            if (!descriptionExpanded){
                descriptionExpanded = true
                seeMore.text = "tap to see less..."
                val params = description.layoutParams
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                description.layoutParams = params
            }else{
                descriptionExpanded = false
                val scale = resources.displayMetrics.scaledDensity
                seeMore.text = "tap to see more..."
                val params = description.layoutParams
                params.height = (36 * scale).toInt()
                description.layoutParams = params
            }
        }

        menuButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(vendor.menu))
            startActivity(browserIntent)
        }

        directionsButton.setOnClickListener {
            val url = "http://maps.google.com/maps?daddr="+ vendor.address +"&mode=driving"
            val intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
            startActivity(intent)
        }



        // getting the buttons, and scaling their logo
        val scaledMap = ScaleDrawable(ContextCompat.getDrawable(context!!, R.drawable.icon_business),0, 5f,5f)
        val directionsButton = view.findViewById<Button>(R.id.vendor_directions)
        directionsButton.setCompoundDrawables(null, null,null,scaledMap)
        return view

    }

    fun getFirebaseData(){
        var activedeals = mutableMapOf<String, Deal?>()
        var inactivedeals = mutableMapOf<String, Deal?>()
        var dealsArray : List<Deal?>
        var vendors = mutableMapOf<String, Vendor?>()
        vendors[vendor.id!!] = vendor

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
                    for (deal in activedeals) {
                        deal.value!!.favorited = favorites.containsKey(deal.key)
                    }
                    for (deal in inactivedeals) {
                        deal.value!!.favorited = favorites.containsKey(deal.key)
                    }
                    if (favUpdated) {
                        if (deal_list != null) {
                            deal_list.adapter!!.notifyDataSetChanged()
                        }
                    }
                } else {
                    favorites.clear()
                    for (deal in activedeals) {
                        deal.value!!.favorited = false
                    }
                    for (deal in inactivedeals) {
                        deal.value!!.favorited = false
                    }
                    if (favUpdated) {
                        if (deal_list != null) {
                            deal_list.adapter!!.notifyDataSetChanged()
                        }
                    }
                }
                if (!favUpdated) { //DONT redo deals if already just updating favorites
                    dealsListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {

                                for (dealSnapshot in dataSnapshot.children) {
                                    val temp = Deal(dealSnapshot,myLocation!!,vendor.location!!,user!!.uid, favorites)

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
                                dealsArray = ArrayList(activedeals.values) + ArrayList(inactivedeals.values)//.sortedBy { deal -> deal!!.distanceMiles } .sortedBy { deal -> deal!!.distanceMiles }
                                if (dealsArray.isEmpty()){
                                    dealsHeader.text = "No Current Offers"
                                }else{
                                    dealsHeader.text = "Current Offers"
                                }

                                adapter = DealsViewVendorRecyclerAdapter(dealsArray,vendor, context!!)

                                deal_list.layoutManager = layoutManager

                                deal_list.adapter = adapter
                            }else{
                                dealsHeader.text = "No Current Offers"
                            }

                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            println("cancelled userListener")
                        }
                    }
                    dealsRef.addValueEventListener(dealsListener)

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
            registerLocationListner()

        }else{
            //location denied. Tell user to turn it on
            //we actually should have location here. Can't get here without it
//            locationMessage!!.visibility = View.VISIBLE

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
                getFirebaseData()
            }
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


    override fun onDestroy() {
        super.onDestroy()
        followButton.setOnClickListener(null)
        if (userListener != null){
            userInfoRef.removeEventListener(userListener)
        }
        if (favoritesListener != null){
            favoriteRef.removeEventListener(favoritesListener)
        }
        if (dealsListener != null){
            dealsRef.removeEventListener(dealsListener)
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewVendorFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(): ViewVendorFragment = ViewVendorFragment()

    }
}
