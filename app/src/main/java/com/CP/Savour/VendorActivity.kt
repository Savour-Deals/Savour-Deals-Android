package com.CP.Savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

private const val ARG_VENDOR = "vendor"
/**
 * This activity displays the individual vendor page
 */
class VendorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor)

        // To get to this page, an intent is used with extras to pass data about the vendor
        val vendor = intent.getParcelableExtra<Vendor>(ARG_VENDOR)

        // adding the image to the page

    }
}
