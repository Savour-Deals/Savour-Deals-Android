package com.CP.Savour

import android.content.Context
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindow(val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(p0: Marker?): View {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}