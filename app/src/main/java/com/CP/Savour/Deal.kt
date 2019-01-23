package com.CP.Savour

import android.location.Location
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import java.util.*
import com.google.firebase.database.*
import org.joda.time.*
import kotlin.collections.HashMap
import org.joda.time.format.DateTimeFormat



class Deal : Parcelable {
    var vendorName: String? = null
    var id: String? = null
    var vendorID: String? = null
    var photo: String? = null
    var dealDescription: String? = null
    var startTime: Long? = null
    var endTime: Long? = null
    var favorited: Boolean? = null
    var redeemed: Boolean? = null
    var redeemedTime: Long? = null
    var type: String? = null
    var code: String? = null
    var activeHours: String? = null
    var inactiveString: String? = null
    var activeDays = arrayOfNulls<Boolean>(7)
    var active: Boolean? = null
    var countdown: String? = null
    var daysLeft: Int? = null
    var distanceMiles: Float? = null

    constructor(parcel: Parcel) : this() {
        vendorName = parcel.readString()
        id = parcel.readString()
        vendorID = parcel.readString()
        photo = parcel.readString()
        dealDescription = parcel.readString()
        startTime = parcel.readValue(Long::class.java.classLoader) as? Long
        endTime = parcel.readValue(Long::class.java.classLoader) as? Long
        favorited = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        redeemed = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        redeemedTime = parcel.readValue(Long::class.java.classLoader) as? Long
        type = parcel.readString()
        code = parcel.readString()
        activeHours = parcel.readString()
        inactiveString = parcel.readString()
        active = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        countdown = parcel.readString()
        daysLeft = parcel.readValue(Int::class.java.classLoader) as? Int
        distanceMiles = parcel.readValue(Float::class.java.classLoader) as? Float
    }

    constructor() {}


    constructor(dealSnap: DataSnapshot, myLocation: Location, dealLocation: Location, userID: String, favorites: MutableMap<String,String>) {
        val dealmap = dealSnap.value as  HashMap<String?, Any?>
        val deal = dealmap.withDefault { null }
        this.id = dealSnap.key
        this.vendorName = deal.getValue("vendor_name").toString()
        this.vendorID = deal.getValue("vendor_id").toString()
        this.photo = deal.getValue("photo").toString()
        this.dealDescription = deal.getValue("deal_description").toString()
        this.startTime = dealmap.getValue("start_time") as? Long
        this.endTime = dealmap.getValue("end_time") as? Long
        this.favorited = favorites.containsKey(this.id!!)

        if (deal.containsKey("redeemed")){
            val redemptions = deal.getValue("redeemed") as HashMap<String?, Double?>
            if (redemptions.containsKey(userID)){
                val time = redemptions.get(userID)!!.toDouble()
                if ((Date().time/1000 - time) > 60*60*24*7*2) {
                    //If redeemed 2 weeks ago, allow user to use deal again - Should be changed in the future
                    val randStr = java.util.UUID.randomUUID().toString().substring(0,9)
                    var ref = FirebaseDatabase.getInstance().getReference("Deals").child(this.id!!).child("redeemed")
                    ref.child(userID).removeValue()
                    ref.child(userID+"-"+randStr).setValue(time)
                    this.redeemed = false
                    this.redeemedTime = 0
                }else{
                    this.redeemed = true
                    this.redeemedTime = time.toLong()
                }
            }else{
            this.redeemed = false
            this.redeemedTime = 0
        }
        }else{
            this.redeemed = false
            this.redeemedTime = 0
        }
        this.distanceMiles = myLocation.distanceTo(dealLocation)*0.00062137f
        val activeSnap = deal.getValue("active_days") as HashMap<String,Boolean>
        this.activeDays[6] = activeSnap.getValue("sun")
        this.activeDays[0] = activeSnap.getValue("mon")
        this.activeDays[1] = activeSnap.getValue("tues")
        this.activeDays[2] = activeSnap.getValue("wed")
        this.activeDays[3] = activeSnap.getValue("thur")
        this.activeDays[4] = activeSnap.getValue("fri")
        this.activeDays[5] = activeSnap.getValue("sat")


        val startDateTime = LocalDateTime(this.startTime!!*1000L)
        val endDateTime = LocalDateTime(this.endTime!!*1000L)
        val dtf = DateTimeFormat.forPattern("h:mm a")

        var now = LocalDateTime()
        var start = now.withTime(startDateTime.hourOfDay,startDateTime.minuteOfHour,0,0)
        var end = now.withTime(endDateTime.hourOfDay,endDateTime.minuteOfHour,0,0)
        if (now.hourOfDay < 5){//To solve problem with checking for deals after midnight... might have better way
            start = start.minusDays(1)
            end = end.minusDays(1)
        }
        if (start > end){//Deal goes past midnight (might be typical of bar's drink deals)
            end = end.plusDays(1)
        }
        if (this.activeDays[now.dayOfWeek-1]!!){//Active today
            if (now > start && now < end){
                this.activeHours = "valid until " + endDateTime.toString(dtf)
                this.active = true
            }else if (start == end){
                this.activeHours = ""//"active all day!"
                this.active = true
            }else{
                this.activeHours = ""//"valid from " + formatter.string(from: startTime) + " to " + formatter.string(from: endTime)
                this.inactiveString = "from " + startDateTime.toString(dtf) + " to " + endDateTime.toString(dtf)
                this.active = false
            }
        }else{//Not Active today
            this.inactiveString = ""
            val days = arrayOf("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday")
            for (i in 0..6){
                if (this.activeDays[i]!!){
                    if (this.inactiveString != ""){
                        this.inactiveString = this.inactiveString + ", "
                    }
                    this.inactiveString = this.inactiveString + days[i]
                }
            }
            this.active = false
        }

        //Get countdown string and days left
        val st  = startDateTime.toDateTime()
        val en = endDateTime.toDateTime()
        this.daysLeft = Days.daysBetween(startDateTime,endDateTime).days
        if ((startDateTime.hourOfDay == endDateTime.hourOfDay) || (now > startDateTime && now < endDateTime)) {
            if (daysLeft!! > 1) {
                this.countdown = daysLeft.toString() + " days left"
            } else if (daysLeft!! == 1) {
                this.countdown = "Deal expires tomorrow!"
            } else {
                this.countdown = "Deal expires today!"
            }
        }else{
            this.countdown = "Deal expires today!"
            this.daysLeft = 0
        }
    }

    fun updateTimes(){
        val startDateTime = LocalDateTime(this.startTime)
        val endDateTime = LocalDateTime(this.endTime)
        val dtf = DateTimeFormat.forPattern("h:mm a")

        var now = LocalDateTime()
        var start = now.withTime(startDateTime.hourOfDay,startDateTime.minuteOfHour,0,0)
        var end = now.withTime(endDateTime.hourOfDay,endDateTime.minuteOfHour,0,0)
        if (now.hourOfDay < 5){//To solve problem with checking for deals after midnight... might have better way
            start = start.minusDays(1)
            end = end.minusDays(1)
        }
        if (start.hourOfDay > end.hourOfDay){//Deal goes past midnight (might be typical of bar's drink deals)
            end = end.plusDays(1)
        }
        if (this.activeDays[now.dayOfWeek-1]!!){//Active today
            if (now > start && now < end){
                this.activeHours = "valid until " + endDateTime.toString(dtf)
                this.active = true
            }else if (start == end){
                this.activeHours = ""//"active all day!"
                this.active = true
            }else{
                this.activeHours = ""//"valid from " + formatter.string(from: startTime) + " to " + formatter.string(from: endTime)
                this.inactiveString = "from " + startDateTime.toString(dtf) + " to " + endDateTime.toString(dtf)
                this.active = false
            }
        }else{//Not Active today
            this.inactiveString = ""
            val days = arrayOf("Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday")
            for (i in 0..6){
                if (this.activeDays[i]!!){
                    if (this.inactiveString != ""){
                        this.inactiveString = this.inactiveString + ", "
                    }
                    this.inactiveString = this.inactiveString + days[i]
                }
            }
            this.active = false
        }

        //Get countdown string and days left
        val st  = startDateTime.toDateTime()
        val en = endDateTime.toDateTime()
        this.daysLeft = Days.daysBetween(startDateTime,endDateTime).days
        if ((startDateTime.hourOfDay == endDateTime.hourOfDay) || (now > startDateTime && now < endDateTime)) {
            if (daysLeft!! > 1) {
                this.countdown = daysLeft.toString() + " days left"
            } else if (daysLeft!! == 1) {
                this.countdown = "Deal expires tomorrow!"
            } else {
                this.countdown = "Deal expires today!"
            }
        }else{
            this.countdown = "Deal expires today!"
            this.daysLeft = 0
        }
    }

     fun isAvailable(): Boolean {
        val currentTime = DateTime().millis/1000
        val startOfToday = DateTime().withTimeAtStartOfDay().millis/1000

        // ((deal not expired AND deal has started) OR deal runs all day) AND deal is not redeemed
        return ((this.endTime!! > currentTime &&  this.startTime!! < currentTime) || (this.endTime!! == startOfToday)) && !this.redeemed!!
    }

    fun updateDistance(vendor: Vendor, myLocation: Location){
        if (this.vendorID != "SVRDEALS"){
            if ((vendor.location != null) && (myLocation != null)){
                this.distanceMiles = myLocation.distanceTo(vendor.location)*0.00062137f
            }
        }else{
            print("Could not update distance. Vendor or myLocation not present")
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(vendorName)
        parcel.writeString(id)
        parcel.writeString(vendorID)
        parcel.writeString(photo)
        parcel.writeString(dealDescription)
        parcel.writeValue(startTime)
        parcel.writeValue(endTime)
        parcel.writeValue(favorited)
        parcel.writeValue(redeemed)
        parcel.writeValue(redeemedTime)
        parcel.writeString(type)
        parcel.writeString(code)
        parcel.writeString(activeHours)
        parcel.writeString(inactiveString)
        parcel.writeValue(active)
        parcel.writeString(countdown)
        parcel.writeValue(daysLeft)
        parcel.writeValue(distanceMiles)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Deal> {
        override fun createFromParcel(parcel: Parcel): Deal {
            return Deal(parcel)
        }

        override fun newArray(size: Int): Array<Deal?> {
            return arrayOfNulls(size)
        }
    }
}