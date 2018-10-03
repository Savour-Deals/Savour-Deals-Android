package com.CP.Savour

import android.location.Location
import com.firebase.geofire.GeoLocation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference

/**
 * This data class will be used to parse the Vendor datasnapshot from the firebase calls
 * In the Recycler adapter.
 */
class Vendor {
    var name: String? = null
    var id: String? = null
    var photo: String? = null
    var description: String? = null
    var address: String? = null
    var location: Location? = null
    var distanceMiles: Float? = null
    var menu: String? = null
    var subscriptionId: String? = null
    var dailyHours = arrayOfNulls<String>(7)
    var loyaltyCode: String? = null
    var loyaltyCount: Int? = null
    var loyaltyDeal: String? = null
    var loyaltyPoints = arrayOfNulls<Int>(7)

    constructor() { }

    constructor(vendorSnap: DataSnapshot, myLocation: Location, vendorLocation: Location) {
        val vendormap = vendorSnap.value as  HashMap<String?, Any?>
        val vendor = vendormap.withDefault { null }
        this.id = vendorSnap.key
        this.address = vendor.getValue("address").toString()
        this.description = vendor.getValue("description").toString()
        this.menu = vendor.getValue("menu")?.toString() ?: ""
        this.name = vendor.getValue("name").toString()
        this.photo = vendor.getValue("photo").toString()
//        this.subscriptionId = vendor.getValue("subscription_id").toString()
        if (vendorSnap.child("daily_hours").exists()){
            val hours = vendor.getValue("daily_hours") as HashMap<String?,Any?>
            this.dailyHours[0] = hours.getValue("sun").toString()
            this.dailyHours[1] = hours.getValue("mon").toString()
            this.dailyHours[2] = hours.getValue("tues").toString()
            this.dailyHours[3] = hours.getValue("wed").toString()
            this.dailyHours[4] = hours.getValue("thurs").toString()
            this.dailyHours[5] = hours.getValue("fri").toString()
            this.dailyHours[6] = hours.getValue("sat").toString()
        }else {
            for (i in 0..6) {
                this.dailyHours[i] = "Not Available"
            }
        }
        val loyaltymap = vendor.getValue("loyalty") as HashMap<String?,Any?>
        val loyalty = loyaltymap.withDefault { null }
        this.loyaltyCode = loyalty.getValue("loyalty_code")?.toString()  ?: ""
        this.loyaltyCount = loyalty.getValue("loyalty_count") as? Int ?: 0
        this.loyaltyDeal = loyalty.getValue("loyalty_deal")?.toString()  ?: ""
        if(loyalty.getValue("loyalty_points") != null){
            val pointsmap = loyalty.getValue("loyalty_points") as HashMap<String?,Number?>
            val points = pointsmap.withDefault { null }
            this.loyaltyPoints = arrayOf(
                    points.getValue("sun")?.toInt(),
                    points.getValue("mon")?.toInt(),
                    points.getValue("tues")?.toInt(),
                    points.getValue("wed")?.toInt(),
                    points.getValue("thurs")?.toInt(),
                    points.getValue("fri")?.toInt(),
                    points.getValue("sat")?.toInt()
            )
        }
        this.location = vendorLocation
        this.distanceMiles = myLocation.distanceTo(vendorLocation)*0.00062137f
    }

    fun updateDistance(myLocation: Location){
        this.distanceMiles  = myLocation.distanceTo(this.location)*0.00062137f
    }
}