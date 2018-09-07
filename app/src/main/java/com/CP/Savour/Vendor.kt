package com.CP.Savour

/**
 * This data class will be used to parse the Vendor datasnapshot from the firebase calls
 * In the Recycler adapter.
 */
data class Vendor(
        var address: String,
        var description: String,
        var dailyHours: Map<String, Object>,
        var followers: Map<String,Object>,
        var loyalty: Map<String, Object>,
        var menu: String,
        var name: String,
        var phone: String,
        var photo: String,
        var placeId: String

) {

    // creating the empty constructor so that the class can parse the datasnapshot
    constructor() : this("","", mapOf(), mapOf(),mapOf(),"","","","","")
}