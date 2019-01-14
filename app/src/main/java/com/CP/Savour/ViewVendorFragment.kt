package com.CP.Savour

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.graphics.drawable.ScaleDrawable
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import java.util.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.*

import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.vision.CameraSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.fragment_deals.*
import org.joda.time.DateTime

private const val ARG_VENDOR = "vendor"
private const val POINTS = "points"
private const val CODE = "code"
private const val SCAN_QR_REQUEST = 1

class ViewVendorFragment : Fragment() {
    //UI components
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
    private lateinit var loyaltyLabel: TextView



    private var redemptionTime: Long = 0
    var activedeals = mutableMapOf<String, Deal?>()
    var inactivedeals = mutableMapOf<String, Deal?>()
    var dealsArray : List<Deal?> = arrayListOf()

    private var redeem: Boolean = false

    private var layoutManager : RecyclerView.LayoutManager? = null
    private var dealsAdapter : DealsViewVendorRecyclerAdapter? = null

    var firstLocationUpdate = true
    private lateinit var locationService: LocationService
    private lateinit var loyaltyConstraint: ConstraintLayout

    private var user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private var descriptionExpanded = false

    private lateinit var dealsRef: Query
    val favoriteRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid).child("favorites")
    val userInfoRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid)

    private var points: Long? = null

    private lateinit var favoritesListener: ValueEventListener
    private lateinit var userListener: ValueEventListener
    private lateinit var dealsListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vendor = it.getParcelable(ARG_VENDOR) as Vendor
            dealsRef = FirebaseDatabase.getInstance().getReference("Deals").orderByChild("vendor_id").equalTo(vendor.id)
        }
        if (this.activity != null) {
            locationService = LocationService(pActivity = this.activity!!, callback = {
                onLocationChanged(it)
            })
            startLocation()
        } else {
            println("VIEWVENDORFRAGMENT:onCreate:Error getting activity for locationService")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_vendor, container, false)

        //grab out UI components
        loyaltyConstraint = view.findViewById(R.id.loyalty_checkin)
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
        loyaltyLabel = view.findViewById(R.id.loyalty_label)

        layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)

        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //is this user following this vendor or not?
                if (snapshot.child("following").child(vendor.id!!).exists()) {
                    followButton.background = ContextCompat.getDrawable(context!!, R.drawable.vendor_button)
                    followButton.text = "Following"
                } else {
                    followButton.background = ContextCompat.getDrawable(context!!, R.drawable.vendor_button_selected)
                    followButton.text = "Follow"
                }
                //setup loyalty deal visible or not depending on if it exists
                if(vendor.loyaltyDeal == "") {
                    //no loyalty program, set invisible
                    val params = loyaltyConstraint.layoutParams
                    params.height = 0
                    loyaltyConstraint.layoutParams = params
                }else{
                    //vendor has loyalty program, go ahead and get user data
                    //set visible
                    val params = loyaltyConstraint.layoutParams
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT
                    loyaltyConstraint.layoutParams = params

                    //get user data
                    if (snapshot.child("loyalty").child(vendor.id!!).exists()) {
                        points = snapshot.child("loyalty").child(vendor.id!!).child("redemptions").child("count").value as Long? ?:0
                        redemptionTime = snapshot.child("loyalty").child(vendor.id!!).child("redemptions").child("time").value as Long? ?:0
                        loyaltyText.text = "$points/${vendor.loyaltyCount}"
                        loyaltyProgress.progress = points!!.toInt()
                        if (points!! >= vendor.loyaltyCount!!){
                            loyaltyLabel.text = "You're ready to redeem your ${vendor.loyaltyDeal}!"
                        }else{
                            loyaltyLabel.text = "Today: +${vendor.loyaltyPoints[DateTime.now().dayOfWeek-1]}" +
                            "\n Reach points goal and recieve: a ${vendor.loyaltyDeal}!"
                        }
                    }else{
                        loyaltyText.text = "0/" + vendor.loyaltyCount
                        loyaltyProgress.progress = 0
                        userInfoRef.child("loyalty").child(vendor.id!!).child("redemptions").child("count").setValue(0)
                    }
                    if (points!!.toInt() >= vendor.loyaltyCount!!.toInt()) {
                        loyaltyButton.text = "Redeem"
                    } else {
                        loyaltyButton.text = "Loyalty Check-in"
                    }
                }
            }
            override fun onCancelled(dbError: DatabaseError) {
                println("cancelled userListener")
            }
        }
        userInfoRef.addValueEventListener(userListener)


        //Button listeners
        loyaltyButton.setOnClickListener {
            loyaltyPressed()
        }

        followButton.setOnClickListener {
            followPressed()
        }

        descriptionContainer.setOnClickListener {
            descriptionToggled()
        }

        menuButton.setOnClickListener {
            menuPressed()
        }

        directionsButton.setOnClickListener {
            val url = "http://maps.google.com/maps?daddr="+ vendor.address +"&mode=driving"
            val intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
            startActivity(intent)
        }

        //Setup UI display for vendor
        vendorName.text = vendor.name
        address.text = vendor.address
        hours.text = vendor.dailyHours[Calendar.DAY_OF_WEEK - 1]
        description.text = vendor.description

        Glide.with(this)
                .load(vendor.photo)
                .into(dealImage)

        // getting the buttons, and scaling their logo
        val scaledMap = ScaleDrawable(ContextCompat.getDrawable(context!!, R.drawable.icon_business),0, 5f,5f)
        val directionsButton = view.findViewById<Button>(R.id.vendor_directions)
        directionsButton.setCompoundDrawables(null, null,null,scaledMap)
        return view
    }

    fun getFirebaseData(){

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
                                    val temp = Deal(dealSnapshot,locationService.currentLocation!!,vendor.location!!,user!!.uid, favorites)

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
                                onDataChanged()
                            }else{
                                onDataChanged()
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

    fun menuPressed(){
        if (vendor.menu != null){
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(vendor.menu))
            startActivity(browserIntent)
        }else{
            displayMessage("Sorry!", "Looks like this vendor has not yet made their menu avaliable to us! Sorry for the inconvenience.", "😢 Okay")
        }
    }

    fun followPressed() {
        if (followButton.text == "Follow") {
            userInfoRef.child("following").child(vendor.id!!).setValue(true)
        } else {
            userInfoRef.child("following").child(vendor.id!!).removeValue()
        }
    }

    fun descriptionToggled() {
        if (!descriptionExpanded) {
            descriptionExpanded = true
            seeMore.text = "tap to see less..."
            val params = description.layoutParams
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT
            description.layoutParams = params
        } else {
            descriptionExpanded = false
            val scale = resources.displayMetrics.scaledDensity
            seeMore.text = "tap to see more..."
            val params = description.layoutParams
            params.height = (36 * scale).toInt()
            description.layoutParams = params
        }
    }

    fun loyaltyPressed() {
        var timeNow = DateTime.now().millis / 1000
        vendor.updateDistance(locationService.currentLocation!!)
        //Check if the deal is within range?
        if (vendor.distanceMiles!! < 0.2) {//close enough to continue
            //Now check their points count
            if (points!!.toInt() >= vendor.loyaltyCount!!) {//user has enough points to redeem!
                if (10800 < (timeNow - redemptionTime)) {//We are ready to redeem! Prompt user with next steps
                    redeemLoyalty()
                } else {
                    displayMessage("Too Soon!","Come back tomorrow to redeem your points!", "Okay")
                }
            }else{//user needs more points, let them check-in
                if (10800 < (timeNow - redemptionTime)) {//Ready to checkin!
                    val intent = Intent(context, ScanActivity::class.java)
                    if (points == null) {
                        points = 0
                    }
                    startActivityForResult(intent, SCAN_QR_REQUEST)
                } else {
                    displayMessage("Too Soon!","Come back tomorrow to get another loyalty visit!", "Okay")
                }
            }
        }else{//vendor too far away
            displayMessage("Too far away!", "Go to location to use their loyalty program!", "Okay")
        }
    }

    fun checkCode(code: String){
        if (code == vendor.loyaltyCode){
            redemptionTime = DateTime.now().millis/1000
            val status  = OneSignal.getPermissionSubscriptionState()
            //Redundant following for user and rest
            FirebaseDatabase.getInstance().getReference("Vendors").child(vendor.id!!).child("followers").child(user!!.uid).setValue(status.subscriptionStatus.userId)
            userInfoRef.child("following").child(vendor.id!!).setValue(true)

            OneSignal.sendTag(vendor.id!!,"true")

            points = points?.plus(vendor.loyaltyPoints[DateTime.now().dayOfWeek-1])
            userInfoRef.child("loyalty").child(vendor.id!!).child("redemptions").child("count").setValue(points)
            userInfoRef.child("loyalty").child(vendor.id!!).child("redemptions").child("time").setValue(redemptionTime)
            loyaltyProgress.progress = points!!.toInt()
            loyaltyText.text = "$points/${vendor.loyaltyCount}"

            //update loyalty text
            if (points!! >= vendor.loyaltyCount!!){
                loyaltyLabel.text = "You're ready to redeem your ${vendor.loyaltyDeal}!"
            }else{
                loyaltyLabel.text = "Today: +${vendor.loyaltyPoints[DateTime.now().dayOfWeek-1]}" +
                        "\n Reach points goal and recieve: a ${vendor.loyaltyDeal}!"
            }
            displayMessage("Success!", "Successfully checked in.", "Okay")
        }else{
            //wrong code. let user know whats up
            displayMessage("Incorrect code!", "The Check-In QRcode you used was incorrect. Please try again.", "Okay")
        }
    }

    fun redeemLoyalty(){
        val builder = AlertDialog.Builder(this.context!!)
        builder.setTitle("Confirm Redemption!")
        builder.setMessage("If you wish to redeem this loyalty deal now, show this message to the server. " +
                "If you wish to save this deal for later, hit CANCEL.")
        /* Set up the buttons */
        builder.setPositiveButton("Redeem") { dialogInterface, which->
            points = points!! - vendor.loyaltyCount!!
            redemptionTime = DateTime.now().millis / 1000
            userInfoRef.child("loyalty").child(vendor.id!!).child("redemptions").child("count").setValue(points)
            userInfoRef.child("loyalty").child(vendor.id!!).child("redemptions").child("time").setValue(redemptionTime)

            //update loyalty text
            loyaltyButton.setText("Loyalty Check-In")
            if (points!! >= vendor.loyaltyCount!!){
                loyaltyLabel.text = "You're ready to redeem your ${vendor.loyaltyDeal}!"
            }else{
                loyaltyLabel.text = "Today: +${vendor.loyaltyPoints[DateTime.now().dayOfWeek-1]}" +
                        "\n Reach points goal and recieve: a ${vendor.loyaltyDeal}!"
            }
        }
        builder.setNegativeButton("Cancel") { dialogInterface, which->
            //Cancelled redemption
        }
        builder.show()
    }

    fun checkNoDeals(){
        if (dealsArray.isEmpty()){
            dealsHeader.text = "No Current Offers"
        }else{
            dealsHeader.text = "Current Offers"
        }
    }

    fun onDataChanged(){
        dealsArray = ArrayList(activedeals.values) + ArrayList(inactivedeals.values)//.sortedBy { deal -> deal!!.distanceMiles } .sortedBy { deal -> deal!!.distanceMiles }
        checkNoDeals()
        if (dealsAdapter == null) {
            dealsAdapter = DealsViewVendorRecyclerAdapter(dealsArray,vendor, context!!)
            deal_list.layoutManager = layoutManager
            deal_list.adapter = dealsAdapter

        } else {
            dealsAdapter!!.updateElements(dealsArray)
            dealsAdapter!!.notifyDataSetChanged()
        }
    }

    fun startLocation(){
        if(checkPermission() && !locationService.startedUpdates) {
            locationService.startLocationUpdates()
        }
    }

    fun onLocationChanged(location: Location) {
        // New location has now been determined
        if(firstLocationUpdate){
            firstLocationUpdate = false
            getFirebaseData()
        }else{
            if (deal_list != null){
                for (deal in activedeals){
                    deal.value!!.updateDistance(vendor, location)
                }
                for (deal in inactivedeals){
                    deal.value!!.updateDistance(vendor, location)
                }
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
        locationService.cancel()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        println("ONACTIVITYRESULT FROM SCAN FRAGMENT!")

        if (Activity.RESULT_OK == resultCode ) {
            val codeResult = data!!.getStringExtra(CODE)
            checkCode(codeResult)
        }
    }

    fun displayMessage(title: String, message: String, buttonText: String){
        val builder = AlertDialog.Builder(this.context!!)
        builder.setTitle(title)
        builder.setMessage(message)
        /* Set up the button */
        builder.setPositiveButton(buttonText) { dialogInterface: DialogInterface, i: Int -> }
        builder.show()
    }

    companion object {

        @JvmStatic
        fun newInstance(): ViewVendorFragment = ViewVendorFragment()

    }
}
