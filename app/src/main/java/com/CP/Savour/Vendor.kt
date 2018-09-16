package com.CP.Savour

/**
 * This data class will be used to parse the Vendor datasnapshot from the firebase calls
 * In the Recycler adapter.
 */
class Vendor {
    var address: String? = null
    var description: String? = null
    var dailyHours: Map<String, Object>? = null
    var followers: Map<String,Object>? = null
    var loyalty: Map<String, Object>? = null
    var menu: String? = null
    var name: String? = null
    var phone: String? = null
    var photo: String? = null
    var placeId: String? = null


    constructor() {

    }
}