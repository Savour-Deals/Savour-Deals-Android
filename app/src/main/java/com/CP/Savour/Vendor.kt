package com.CP.Savour

/**
 * This data class will be used to parse the Vendor datasnapshot from the firebase calls
 * In the Recycler adapter.
 */
class Vendor {
    public var address: String? = ""
    public var description: String? = ""
    public var dailyHours: Map<String, Object>? = mapOf()
    public var followers: Map<String,Object>? = mapOf()
    public var loyalty: Map<String, Object>? = mapOf()
    public var menu: String? = ""
    public var name: String? = ""
    public var phone: String? = ""
    public var photo: String? = ""
    public var placeId: String? = ""
    public var uuid: String? = ""

    constructor() {

    }
}