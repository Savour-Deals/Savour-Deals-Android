package com.CP.Savour

import android.annotation.SuppressLint
import android.location.Location
import android.os.Parcel
import android.os.Parcelable
import android.support.design.internal.ParcelableSparseArray
import com.firebase.geofire.GeoLocation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference

@SuppressLint("ParcelCreator")
/**
 * This data class will be used to parse the Vendor datasnapshot from the firebase calls
 * In the Recycler adapter.
 */
class Vendor : Parcelable {
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
    var loyaltyCount: Long? = null
    var loyaltyDeal: String? = null
    var loyaltyPoints = IntArray(7)

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()
        id = parcel.readString()
        photo = parcel.readString()
        description = parcel.readString()
        address = parcel.readString()
        location = parcel.readParcelable(Location::class.java.classLoader)
        distanceMiles = parcel.readValue(Float::class.java.classLoader) as? Float
        menu = parcel.readString()
//        subscriptionId = parcel.readString()
        dailyHours = parcel.createStringArray()
        loyaltyCode = parcel.readString()
        loyaltyCount = parcel.readLong()
        loyaltyDeal = parcel.readString()
//        this.loyaltyPoints = IntArray(7)
//        parcel.readIntArray(this.loyaltyPoints)
        loyaltyPoints = parcel.createIntArray()
    }

    constructor() { }

    constructor(vendorSnap: DataSnapshot, myLocation: Location, vendorLocation: Location) {
        val vendormap = vendorSnap.value as  HashMap<String?, Any?>
        val vendor = vendormap.withDefault { null }
        this.id = vendorSnap.key
        this.address = vendor.getValue("address").toString()
        this.description = vendor.getValue("description").toString()
        this.menu = vendor.getValue("menu")?.toString()
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
        this.loyaltyCount = loyalty.getValue("loyalty_count") as Long?  ?: 0
        this.loyaltyDeal = loyalty.getValue("loyalty_deal")?.toString()  ?: ""
        if(loyalty.getValue("loyalty_points") != null){
            val pointsmap = loyalty.getValue("loyalty_points") as HashMap<String?,Number?>
            val points = pointsmap.withDefault { null }
            this.loyaltyPoints = intArrayOf(
                    points.getValue("mon")?.toInt() ?: 0,
                    points.getValue("tues")?.toInt() ?: 0,
                    points.getValue("wed")?.toInt() ?: 0,
                    points.getValue("thurs")?.toInt() ?: 0,
                    points.getValue("fri")?.toInt() ?: 0,
                    points.getValue("sat")?.toInt() ?: 0,
                    points.getValue("sun")?.toInt() ?: 0
            )
        }
        this.location = vendorLocation
        this.distanceMiles = myLocation.distanceTo(vendorLocation)*0.00062137f
    }

    fun updateDistance(myLocation: Location){
        this.distanceMiles  = myLocation.distanceTo(this.location)*0.00062137f
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(id)
        parcel.writeString(photo)
        parcel.writeString(description)
        parcel.writeString(address)
        parcel.writeParcelable(location, flags)
        parcel.writeValue(distanceMiles)
        parcel.writeString(menu)
        parcel.writeStringArray(dailyHours)
        parcel.writeString(loyaltyCode)
        parcel.writeLong(loyaltyCount!!)
        parcel.writeString(loyaltyDeal)
        parcel.writeIntArray(loyaltyPoints)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Vendor> {
        override fun createFromParcel(parcel: Parcel): Vendor {
            return Vendor(parcel)
        }

        override fun newArray(size: Int): Array<Vendor?> {
            return arrayOfNulls(size)
        }
    }
}