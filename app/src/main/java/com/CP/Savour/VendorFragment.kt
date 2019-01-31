package com.CP.Savour

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
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
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.CP.Savour.R.id.vendor_list
import com.bumptech.glide.Glide
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_vendor.*
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener

private const val ARG_VENDOR = "vendor"


class VendorFragment : Fragment(), OnMapReadyCallback, OnMarkerClickListener, OnInfoWindowClickListener {
    private var layoutManager : RecyclerView.LayoutManager? = null
    private var vendorAdapter : VendorRecyclerAdapter? = null

    private var savourImg: ImageView? = null
    private lateinit var locationMessage: TextView
    private lateinit var locationButton: Button
    private lateinit var noVendorsText: TextView
    private lateinit var listButton: TextView
    private lateinit var mapButton: TextView
    private lateinit var mapFrame: FrameLayout
    private lateinit var listFrame: FrameLayout

    private var userLocation: LatLng? = null
    private var mapView: SupportMapFragment? = null
    private var map: GoogleMap? = null
    //private var fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity::clva)
    private var isList = true
    var vendorArray : List<Vendor?> = arrayListOf()


    var geoRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors_Location")
    var geoFire = GeoFire(geoRef)

    val vendors = mutableMapOf<String, Vendor?>()

    var firstLocationUpdate = true
    private lateinit var locationService: LocationService

    var geoQuery:GeoQuery? = null
    var  vendorReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors")


    override fun onMapReady(googleMap: GoogleMap?) {
        val sydney = LatLng(-33.852, 151.211)
        map = googleMap
        if (map != null) {
            map.let {
                if (it != null) {
                    it.setOnMarkerClickListener(this)
                    it.setOnInfoWindowClickListener(this)
                }

            }
//            googleMap.addMarker(MarkerOptions().position(sydney))
//            if (userLocation != null) {
//                googleMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation))
//            }
            updateLocationUI()
        }
    }

    override fun onMarkerClick(marker: Marker?): Boolean {

        return false
    }

    override fun onInfoWindowClick(marker: Marker?) {
        println("INFOWINDOW CLICKEDDD")
        if (marker != null) {
            println("NOT NULL INFOWINDOW CLICKEDDD")

            val vend = vendors[marker.tag]
            val intent = Intent(context, VendorActivity::class.java)
            intent.putExtra(ARG_VENDOR,vend)
           context.let {
               if (it != null) {
                   it.startActivity(intent)
               }
           }

        }

    }

    private fun getDeviceLocation() {

    }

    /**
     * This method is used to set the location of the map to the current location of the user
     */
    private fun updateLocationUI() {
        if (this.map == null) {
            return
        }

        if (checkPermission()) {
            map.also {
                if (it != null) {
                    it.isMyLocationEnabled = true
                    it.uiSettings.isMyLocationButtonEnabled = true
                }
            }
        } else {
            map.also {
                if (it != null) {
                    it.isMyLocationEnabled = false
                    it.uiSettings.isMyLocationButtonEnabled = false
                }
            }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        setRetainInstance(true)


        val view = inflater.inflate(R.layout.fragment_vendor, container, false)


        //savourImg = view.findViewById(R.id.imageView5) as ImageView
        listButton = view.findViewById(R.id.vendor_list_button) as TextView
        mapButton = view.findViewById(R.id.vendor_map_button) as TextView
        locationMessage = view.findViewById(R.id.locationMessage) as TextView
        locationButton = view.findViewById(R.id.location_button) as Button
        noVendorsText = view.findViewById(R.id.novendors)
        mapFrame = view.findViewById(R.id.vendor_map_layout) as FrameLayout
        listFrame = view.findViewById(R.id.vendor_list_layout) as FrameLayout



        mapFrame.visibility = View.INVISIBLE

        listButton.setOnClickListener {
            if(!isList) {
                listButton.setBackgroundColor(resources.getColor(R.color.white))
                listButton.setTextColor(resources.getColor(R.color.colorPrimary))

                mapButton.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                mapButton.setTextColor(resources.getColor(R.color.white))

                isList = true
                mapFrame.visibility = View.INVISIBLE
                listFrame.visibility = View.VISIBLE
            }
        }

        mapButton.setOnClickListener {
            if(isList) {
                mapButton.setBackgroundColor(resources.getColor(R.color.white))
                mapButton.setTextColor(resources.getColor(R.color.colorPrimary))

                listButton.setBackgroundColor(resources.getColor(R.color.colorPrimary))
                listButton.setTextColor(resources.getColor(R.color.white))

                isList = false
                mapFrame.visibility = View.VISIBLE
                listFrame.visibility = View.INVISIBLE
            }
        }

        locationButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity!!.packageName)).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity!!.startActivity(intent)
        }

//        Glide.with(this)
//                .load(R.drawable.savour_white)
//                .into(savourImg!!)

        // retrieving the vendors from the database
        layoutManager = LinearLayoutManager(context)

        if (this.activity != null){
            locationService = LocationService(pActivity = this.activity!!,callback = {
                onLocationChanged(it)
                userLocation = LatLng(it.latitude,it.longitude)

            })
            startLocation()
        }else{
            println("VENDORFRAGMENT:onCreate:Error getting activity for locationService")
        }
        mapView = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment

        if (mapView != null) mapView?.getMapAsync(this)
        return view
    }

    companion object {
        fun newInstance(): VendorFragment = VendorFragment()
    }

    override fun onStart() {
        super.onStart()
        if (locationService != null){ //check that we didnt get an error before and not init locationService
            startLocation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationService.cancel()
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
                            vendors.put(dataSnapshot.key!!,Vendor(dataSnapshot,locationService.currentLocation!!,vendorLocation))
                            onDataChanged()
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
                onDataChanged()
            }

            override fun onKeyMoved(key: String, location: GeoLocation) {
                println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude))
            }

            override fun onGeoQueryReady() {
                println("All initial data has been loaded and events have been fired!")
                checkNoVendors() // should perform a check if any keys enter radius. This will give a false check until firebase gives us data back from those keys
            }

            override fun onGeoQueryError(error: DatabaseError) {
                System.err.println("There was an error with this query: $error")
            }
        })

    }

    fun checkNoVendors(){
        if (vendorArray.count() < 1) {
            noVendorsText!!.setVisibility(View.VISIBLE)
            vendorArray = ArrayList()
        } else {
            noVendorsText!!.setVisibility(View.INVISIBLE)
        }
    }

    fun onDataChanged(){
        vendorArray = ArrayList(vendors.values).sortedBy { vendor -> vendor!!.distanceMiles }
        checkNoVendors()
        if (vendorAdapter == null) {
            vendorAdapter = VendorRecyclerAdapter(vendorArray, context!!)
            vendor_list.layoutManager = layoutManager
            vendor_list.adapter = vendorAdapter

        } else {
            vendorAdapter!!.updateElements(vendorArray)
            vendorAdapter!!.notifyDataSetChanged()
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


    private fun onLocationChanged(location: Location) {
        if (this.map != null) {
            userLocation = LatLng(location.latitude,location.longitude)

            this.map.let {
                if (it != null) {
                    for (vendor in vendorArray) {
                        if (vendor != null) {
                                var lat: Double
                                var lng: Double
                                vendor.location.let {its ->
                                    lat = its!!.latitude
                                    lng =  its!!.longitude
                                }
                                val marker = MarkerOptions().position(LatLng(lat,lng))
                                        .title(vendor.name)


                                it.addMarker(marker).tag = vendor.id


                        }
                        it.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,10f))
                    }
                }
            }
        }
        // New location has now been determined
        if(firstLocationUpdate){
            firstLocationUpdate = false
            getFirebaseData(location.latitude,location.longitude)

        }else{
            //recalculate distances and update recycler
            if (geoQuery!!.center != GeoLocation(location.latitude, location.longitude)) {
                geoQuery!!.center = GeoLocation(location.latitude, location.longitude)
            }
            if (vendor_list != null){
                for (vendor in vendors){
                    vendor.value!!.updateDistance(location)
                }
                onDataChanged()
            }
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
