package com.CP.Savour

/**
 * This data class will be used to parse the Vendor datasnapshot from the firebase calls
 * In the Recycler adapter.
 */
class Vendor {
    var address: String? = null
    var dailyHours: Map<String, Any>? = null
    var description: String? = null
    var followers: Map<String,Any>? = null
    var loyalty: Map<String, Any>? = null
    var menu: String? = null
    var name: String? = null
    var phone: String? = null
    var photo: String? = null
    var placeId: String? = null
    var subscriptionId: String? = null

    constructor() { }

    constructor(address: String?, dailyHours: Map<String, Any>?, description: String?, followers: Map<String, Any>?, loyalty: Map<String, Any>?, menu: String?, name: String?, phone: String?, photo: String?, placeId: String?, subscriptionId: String?) {
        this.address = address
        this.dailyHours = dailyHours
        this.description = description
        this.followers = followers
        this.loyalty = loyalty
        this.menu = menu
        this.name = name
        this.phone = phone
        this.photo = photo
        this.placeId = placeId
        this.subscriptionId = subscriptionId
    }
}