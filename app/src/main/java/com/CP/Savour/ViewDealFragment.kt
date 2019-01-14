package com.CP.Savour


import android.Manifest
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextClock
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.functions.FirebaseFunctions
import com.onesignal.OneSignal
import kotlinx.android.synthetic.main.fragment_deals.*
import org.joda.time.LocalDateTime
import pl.bclogic.pulsator4droid.library.PulsatorLayout
import java.util.*
import kotlin.collections.HashMap


private const val ARG_DEAL = "deal"
private const val ARG_VENDOR = "vendor"
private const val ARG_FROM = "from"


class ViewDealFragment : Fragment() {
    private lateinit var deal: Deal
    private lateinit var vendor: Vendor
    private var from: String? = null

    private lateinit var timer: Timer

    private lateinit var dealImg: ImageView
    private lateinit var vendorName: TextView
    private lateinit var timerLabel: TextView
    private lateinit var description: TextView
    private lateinit var redemptionButton: Button
    private lateinit var moreButton: Button
    private var inRange = true
    private lateinit var termsText: TextView
    private lateinit var pulsator: PulsatorLayout

//    private var mFunctions: FirebaseFunctions? = null
    private lateinit var locationService: LocationService





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deal = it.getParcelable(ARG_DEAL) as Deal
            vendor = it.getParcelable(ARG_VENDOR) as Vendor
            from = it.getString(ARG_FROM)
        }
        timer = Timer()
        if (this.activity != null){
            locationService = LocationService(pActivity = this.activity!!,callback = {
                onLocationChanged(it)
            })
            startLocation()
        }else{
            println("VIEWDEALFRAGMENT:onCreate:Error getting activity for locationService")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (timer != null){
            timer.cancel()
        }
        locationService.cancel()
    }

    override fun onPause() {
        super.onPause()
//        if (timer != null){
//            timer.cancel()
//        }
//        locationService.cancel()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        retainInstance = true

        val view = inflater.inflate(R.layout.fragment_view_deal, container, false)

//        mFunctions = FirebaseFunctions.getInstance()


        dealImg = view.findViewById(R.id.dealImg) as ImageView
        vendorName = view.findViewById(R.id.name) as TextView
        description = view.findViewById(R.id.description) as TextView
        redemptionButton = view.findViewById(R.id.redeem) as Button
        moreButton = view.findViewById(R.id.see_more) as Button
        termsText = view.findViewById(R.id.info) as TextView
        timerLabel = view.findViewById(R.id.timer) as TextView

        pulsator = view.findViewById(R.id.pulsator) as PulsatorLayout
        pulsator.start()


        Glide.with(this)
                .load(deal.photo)
                .apply(RequestOptions.circleCropTransform())
                .into((dealImg))

        vendorName.text = deal.vendorName
        description.text = deal.dealDescription

        if (from != "main"){
            moreButton.visibility = View.INVISIBLE
        }else{
            moreButton.visibility = View.VISIBLE
            moreButton.text = "See More From " + deal.vendorName
        }

        setButtons()

        redemptionButton.setOnClickListener {redeemPressed()}
        moreButton.setOnClickListener {morePressed()}

        return view
    }

    fun setButtons(){
        if (deal.redeemed!!) { //deal is active right now
            redemptionButton.setBackgroundDrawable(resources.getDrawable(R.drawable.green_rounded))
            redemptionButton.text = "Deal Already Redeemed"
            pulsator.color = resources.getColor(R.color.red_tint)
            if (timerLabel.text == ""){
                runTimer()
            }
            if (timerLabel.text == "Reedeemed over half an hour ago"){
                pulsator.color = resources.getColor(R.color.red_tint)
            } else{
                pulsator.color = resources.getColor(R.color.green)
                if (deal.code != null){
                    termsText.text = deal.code
                }
            }
        }else if (deal.active!! && deal.distanceMiles!! <= 1.1F){//deal is active and in range
            redemptionButton.setBackgroundDrawable(resources.getDrawable(R.drawable.primary_rounded))
            redemptionButton.text = "Redeem"
            termsText.text = resources.getString(R.string.terms_text)
            inRange = true
        }else if (!deal.active!!){
            redemptionButton.setBackgroundDrawable(resources.getDrawable(R.drawable.red_rounded))
            redemptionButton.text = "Deal Not Active"
            pulsator.color = resources.getColor(R.color.red_tint)
            termsText.text = "This deal is valid " + deal.inactiveString + "."
        }else if (deal.distanceMiles!! > 1.1F) {//deal not in distance range (1/2 mile. can shrink after testing)
            redemptionButton.setBackgroundDrawable(resources.getDrawable(R.drawable.red_rounded))
            redemptionButton.text = "Go to Location to Redeem"
            inRange = false
        }
    }

    private fun redeemPressed(){
        if (deal.active!! && !deal.redeemed!! && inRange){//perform redemption process
//        if (true){
            val builder = AlertDialog.Builder(this.context!!)
            builder.setTitle("Vendor Approval")
            builder.setMessage("This deal is intended for one person only. " +
                    "Show this message to the vendor to redeem your coupon. " +
                    "The deal is not guaranteed if the vendor does not see this message.")
            /* Set up the buttons */
            builder.setPositiveButton("Approve") { dialogInterface, which->
                val currTime = Date().time/1000
                val uID = FirebaseAuth.getInstance().currentUser!!.uid
                val dataRef = FirebaseDatabase.getInstance().getReference()

                //Note redemption time
                dataRef.child("Deals").child(deal.id!!).child("redeemed").child(uID).setValue(currTime)

                //Call Firebase cloud functions to increment stripe counter
                val sub_id = if (vendor.subscriptionId != null) vendor.subscriptionId else ""
                val vendor_id = if (vendor.id != null) vendor.id else ""
                var data =  HashMap<String, Any>()
                data.put("subscription_id", sub_id!!)
                data.put("vendor_id", vendor_id!!)
                data.put("deal_type",0)

                pulsator.apply {
                    this.color =resources.getColor(R.color.green)
                }

                redemptionButton.setBackgroundDrawable(resources.getDrawable(R.drawable.red_rounded))
                redemptionButton.text = "Already Redeemed!"
                deal.redeemedTime = currTime
                deal.redeemed = true


                dataRef.child("Users").child(uID).child("favorites").child(deal.id!!).removeValue()
                if (deal.code != null){
                    termsText.text = deal.code
                    termsText.setTextColor(resources.getColor(R.color.black))
                }

                val status = OneSignal.getPermissionSubscriptionState()
                if (status.subscriptionStatus.userId != null){
                    //Redundant following for user and rest
                    dataRef.child("Vendors").child(vendor.id!!).child("followers").child(uID).setValue(status.subscriptionStatus.userId)
                    dataRef.child("Users").child(uID).child("following").child(vendor.id!!).setValue(true)
                }
                val ft = fragmentManager!!.beginTransaction()
                ft.detach(this).attach(this).commit()
            }
            builder.setNegativeButton("Cancel") { dialogInterface, which->
                //Cancelled redemption
            }
            builder.show()
        }else if(!inRange){//Open Maps
            val url = "http://maps.google.com/maps?daddr="+ vendor.address +"&mode=driving"
            val intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
            startActivity(intent)
        }//else do nothing
    }

    private fun morePressed(){
        val intent = Intent(context, VendorActivity::class.java)
        intent.putExtra(ARG_VENDOR,vendor)
        activity!!.startActivity(intent)
    }


    fun runTimer(){
        //Set the schedule function
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run(){
                val handler = Handler(Looper.getMainLooper())
                handler.post({
                    val timeSince = Date().time/1000 - deal.redeemedTime!!
                    termsText.setTextColor(resources.getColor(R.color.gray))
                    timerLabel.text = time2String(timeSince) //This will update the label
                    if (timeSince > 1800) {
                        if (deal.code != null){
                            termsText.text = deal.code
                        }
                        timerLabel.text = "Reedeemed over half an hour ago"
                        pulsator.color = resources.getColor(R.color.red_tint)
                        timer.cancel()
                    }
                })
            }
        },0, 1000)

    }

     fun time2String(time: Long): String{
        val minutes = time / 60 % 60
        val seconds = time % 60
        return "Redeemed " + minutes + " minutes " + seconds + " seconds ago"
    }


    fun startLocation(){
        if(checkPermission() && !locationService.startedUpdates) {
            locationService.startLocationUpdates()
        }
    }

    fun onLocationChanged(location: Location) {
        //update the deal location when it changes
        deal.updateDistance(vendor, location)
//        setButtons()
        val ft = fragmentManager!!.beginTransaction()
        ft.detach(this).attach(this).commit()
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





    companion object {
        fun newInstance(): ViewDealFragment = ViewDealFragment()


    }
}
