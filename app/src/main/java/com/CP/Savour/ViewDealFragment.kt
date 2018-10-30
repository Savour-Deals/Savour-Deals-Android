package com.CP.Savour

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
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
import android.support.v7.app.AlertDialog
import android.text.InputType
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import org.joda.time.LocalDateTime
import pl.bclogic.pulsator4droid.library.PulsatorLayout
import java.util.*


private const val ARG_DEAL = "deal"
private const val ARG_VENDOR = "vendor"


class ViewDealFragment : Fragment() {
    private var deal: Deal? = null
    private var vendor: Vendor? = null

    private var timer: Timer? = null

    private var dealImg: ImageView? = null
    private var vendorName: TextView? = null
    private var timerLabel: TextView? = null
    private var description: TextView? = null
    private var redemptionButton: Button? = null
    private var moreButton: Button? = null
    private var inRange = true
    private var termsText: TextView? = null
    private var pulsator: PulsatorLayout? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deal = it.getParcelable(ARG_DEAL) as Deal
//            vendor = it.getParcelable(ARG_VENDOR) as Vendor
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (timer != null){
            timer!!.cancel()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        retainInstance = true

        val view = inflater.inflate(R.layout.fragment_view_deal, container, false)

        dealImg = view.findViewById(R.id.dealImg) as? ImageView
        vendorName = view.findViewById(R.id.name) as? TextView
        description = view.findViewById(R.id.description) as? TextView
        redemptionButton = view.findViewById(R.id.redeem) as? Button
        moreButton = view.findViewById(R.id.see_more) as? Button
        termsText = view.findViewById(R.id.info) as? TextView
        timerLabel = view.findViewById(R.id.timer) as? TextView

        pulsator = view.findViewById(R.id.pulsator) as? PulsatorLayout
        pulsator!!.start()


        Glide.with(this)
                .load(deal!!.photo)
                .apply(RequestOptions.circleCropTransform())
                .into(dealImg!!)

        vendorName!!.text = deal!!.vendorName
        description!!.text = deal!!.dealDescription

        //TODO: Get location to update distance when loading!
//        deal.updateDistance()

        moreButton!!.text = "See More From " + deal!!.vendorName
        if (deal!!.redeemed!!) { //deal is active right now
            redemptionButton!!.setBackgroundDrawable(resources.getDrawable(R.drawable.green_rounded))
            redemptionButton!!.text = "Deal Already Redeemed"
            pulsator!!.color = resources.getColor(R.color.red_tint)
            if (timerLabel!!.text == ""){
                runTimer()
            }
            if (timerLabel!!.text == "Reedeemed over half an hour ago"){
                pulsator!!.color = resources.getColor(R.color.red_tint)
            } else{
                pulsator!!.color = resources.getColor(R.color.green)
                if (deal!!.code != null){
                    termsText!!.text = deal!!.code
                }
            }
        }else if (deal!!.distanceMiles!! > 0.1F && deal!!.active!!) {//deal not in distance range (1/2 mile. can shrink after testing)
            redemptionButton!!.setBackgroundDrawable(resources.getDrawable(R.drawable.red_rounded))
            redemptionButton!!.text = "Go to Location to Redeem"
            inRange = false
        }else if (!deal!!.active!!){
            redemptionButton!!.setBackgroundDrawable(resources.getDrawable(R.drawable.red_rounded))
            redemptionButton!!.text = "Deal Not Active"
            pulsator!!.color = resources.getColor(R.color.red_tint)
            termsText!!.text = "This deal is valid " + deal!!.inactiveString + "."
            termsText!!.text = "This deal is valid " + deal!!.inactiveString + "."

        }

        redemptionButton!!.setOnClickListener {redeemPressed()}

        return view
    }

    private fun redeemPressed(){
        if (deal!!.active!! && !deal!!.redeemed!!/*&& inRange*/){//perform redemption process
            val builder = AlertDialog.Builder(this.context!!)
            builder.setTitle("Vendor Approval")
            builder.setMessage("This deal is intended for one person only. " +
                    "Show this message to the vendor to redeem your coupon. " +
                    "The deal is not guaranteed if the vendor does not see this message.")
            /* Set up the buttons */
            builder.setPositiveButton("Approve") { dialogInterface, which->
                val time = Date().time/1000
                var ref = FirebaseDatabase.getInstance().getReference("Deals").child(deal!!.id!!).child("redeemed")
                ref.child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(time)
                redemptionButton!!.setBackgroundDrawable(resources.getDrawable(R.drawable.green_rounded))
                redemptionButton!!.text = "Deal Already Redeemed"
                pulsator!!.color = resources.getColor(R.color.green)
                deal!!.redeemedTime = time
                deal!!.redeemed = true
            }
            builder.setNegativeButton("Cancel") { dialogInterface, which->
                //Cancelled redemption
            }
            builder.show()
        }else if(!inRange){//Open Maps

        }//else do nothing
    }

    fun runTimer(){
        timer = Timer()
        //Set the schedule function
        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val timeSince = Date().time/1000 - deal!!.redeemedTime!!
                termsText!!.setTextColor(resources.getColor(R.color.gray))
                timerLabel!!.text = time2String(timeSince) //This will update the label
                if (timeSince > 1800) {
                    if (deal!!.code != null){
                        termsText!!.text = deal!!.code
                    }
                    timerLabel!!.text = "Reedeemed over half an hour ago"
                    pulsator!!.color = resources.getColor(R.color.red_tint)
                    timer!!.cancel()
                }
            }
        },0, 1000)

    }

     fun time2String(time: Long): String{
        val minutes = time / 60 % 60
        val seconds = time % 60
        return "Redeemed " + minutes + " minutes " + seconds + " seconds ago"
    }






    companion object {
        fun newInstance(): ViewDealFragment = ViewDealFragment()


    }
}
