package com.CP.Savour

import android.location.Location
import com.google.firebase.database.DataSnapshot
import java.util.*
import com.google.firebase.database.*


class Deal {
    var name: String? = null
    var id: String? = null
    var vendorID: String? = null
    var photo: String? = null
    var dealDescription: String? = null
    var startTime: Double? = null
    var endTime: Double? = null
    var favorited: Boolean? = null
    var redeemed: Boolean? = null
    var redeemedTime: Double? = null
    var type: String? = null
    var code: String? = null
    var activeHours: String? = null
    var inactiveString: String? = null
    var activeDays = arrayOfNulls<Boolean>(7)
    var active: Boolean? = null
    var countdown: String? = null
    var daysLeft: Int? = null
    var distanceMiles: Double? = null

    constructor() {}


    constructor(dealSnap: DataSnapshot, myLocation: Location, dealLocation: Location, userID: String) {
        val dealmap = dealSnap.value as  HashMap<String?, Any?>
        val deal = dealmap.withDefault { null }
        this.id = dealSnap.key
        this.name = deal.getValue("name").toString()
        this.vendorID = deal.getValue("vednor_id").toString()
        this.photo = deal.getValue("photo").toString()
        this.dealDescription = deal.getValue("deal_description").toString()
        this.startTime = deal.getValue("start_time") as? Double
        this.endTime = deal.getValue("start_time") as? Double
        this.favorited = false //TODO: Change this
        if (deal.containsKey("redeemed")){
            val redemptions = deal.getValue("redeemed") as HashMap<String?, Double?>
            if (redemptions.containsKey(userID)){
                val time = redemptions.get(userID) as Double
                if ((Date().time/1000 - time) > 60*60*24*7*2) {
                    //If redeemed 2 weeks ago, allow user to use deal again - Should be changed in the future
                    val randStr = java.util.UUID.randomUUID().toString().substring(0,9)
                    var ref = FirebaseDatabase.getInstance().getReference("Deals").child(this.id!!).child("redeemed")
                    ref.child(userID).removeValue()
                    ref.child(userID+"-"+randStr).setValue(time)
                    this.redeemed = false
                    this.redeemedTime = 0.0
                }else{
                    this.redeemed = true
                    this.redeemedTime = time
                }
            }else{
            this.redeemed = false
            this.redeemedTime = 0.0
        }
        }else{
            this.redeemed = false
            this.redeemedTime = 0.0
        }



    }
}