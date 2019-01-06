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

    private var dealsAdapter : DealsRecyclerAdapter? = null

    var nodealsText: TextView? = null
    private lateinit var locationMessage: TextView
    private lateinit var locationButton: Button

    var firstLocationUpdate = true
    private lateinit var locationService: LocationService

    var activedeals = mutableMapOf<String, Deal?>()
    var inactivedeals = mutableMapOf<String, Deal?>()
    var dealsArray: List<Deal?> = arrayListOf()

    private lateinit var mAuth: FirebaseAuth
    private lateinit var authStateListner: FirebaseAuth.AuthStateListener

    var geoRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors_Location")
    var geoFire = GeoFire(geoRef)
    var  vendorReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors")

    private lateinit var dealsListener: ValueEventListener

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
        nodealsText = view.findViewById(R.id.nodeals)
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

        if (this.activity != null){
            locationService = LocationService(pActivity = this.activity!!,callback = {
                onLocationChanged(it)
            })
        }else{
            println("FAVORITESFRAGMENT:onCreate:Error getting activity for locationService")
        }
        startLocation()

        return view
    }

    override fun onStart() {
        super.onStart()
//        mAuth = FirebaseAuth.getInstance()
//        authStateListner = FirebaseAuth.AuthStateListener { auth ->
//            val user = auth.currentUser
//            if(user != null && firstLocationUpdate){
//            }
//        }
//        mAuth.addAuthStateListener(authStateListner)
        startLocation()

    }

    override fun onPause() {
        super.onPause()
//        if (authStateListner != null){
//            mAuth.removeAuthStateListener(authStateListner)
//        }
    }
    companion object {
        fun newInstance(): FavoritesFragment = FavoritesFragment()
    }

    private fun getFirebaseData(lat:Double, lng:Double) {
        val userID = FirebaseAuth.getInstance().currentUser!!.uid


        var oldFavs = ArrayList<String>()

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
                                              val temp = Deal(dataSnapshot,locationService.currentLocation!!,vendorLocation,userID, favorites)

                                              val dealsListener = object : ValueEventListener {//Now  get its deals!
                                                  /**
                                                   * Listening for when the data has been changed
                                                   * and also when we want to access f
                                                   */
                                                  override fun onDataChange(dataSnapshot: DataSnapshot) {
                                                      vendors.put(dataSnapshot.key!!,Vendor(dataSnapshot,locationService.currentLocation!!,vendorLocation))
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
                                                          onDataChanged()
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
                            onDataChanged()
                        }
                    }
                    oldFavs = newFavs
                }else{//If no favorites
                    onDataChanged()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        favoriteRef.addValueEventListener(favoritesListener)
    }


    fun checkNoDeals(){
        if (dealsArray.count() < 1) {
            nodealsText!!.setVisibility(View.VISIBLE)
            dealsArray = ArrayList()
        } else {
            nodealsText!!.setVisibility(View.INVISIBLE)
        }
    }

    fun onDataChanged(){
        dealsArray = ArrayList(activedeals.values).sortedBy { deal -> deal!!.distanceMiles } + ArrayList(inactivedeals.values).sortedBy { deal -> deal!!.distanceMiles }
        checkNoDeals()
        if (dealsAdapter == null) {
            dealsAdapter = DealsRecyclerAdapter(dealsArray,vendors, context!!)
            deal_list.layoutManager = layoutManager
            deal_list.adapter = dealsAdapter

        } else {
            dealsAdapter!!.updateElements(dealsArray,vendors)
            dealsAdapter!!.notifyDataSetChanged()
        }
    }

    fun onLocationChanged(location: Location) {
        // New location has now been determined
        if(firstLocationUpdate){
            firstLocationUpdate = false
            getFirebaseData(location.latitude,location.longitude)
        }else{
            //recalculate distances and update recycler
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
                onDataChanged()
            }
        }
    }


    fun startLocation(){
        if(checkPermission()) {
            locationMessage!!.visibility = View.INVISIBLE
            locationButton!!.visibility = View.INVISIBLE
            if (!locationService.startedUpdates){
                locationService.startLocationUpdates()
            }
        }else {
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



    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this.activity!!, arrayOf("Manifest.permission.ACCESS_FINE_LOCATION"),1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1) {
            if (permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION ) {
                startLocation()
            }
        }
    }
}

