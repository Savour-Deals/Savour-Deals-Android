package com.CP.Savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

private const val ARG_VENDOR = "vendor"
private const val POINTS = "points"

class ScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var scannerView: ZXingScannerView? = null
    private var loyaltyProgress: ProgressBar? = null
    private var loyaltyText: TextView? = null
    private var points: String? = null

    private lateinit var vendor: Vendor
    private var user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private val userInfoRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerView = ZXingScannerView(this)
        setContentView(scannerView)


    }

    override fun onResume() {
        super.onResume()

        scannerView!!.setResultHandler(this)
        scannerView!!.startCamera()

    }

    override fun onPause() {
        super.onPause()
        scannerView!!.stopCamera()

    }
    override fun handleResult(rawResult: Result?) {
        println("The raw result: ")
        println(rawResult.toString())

        userInfoRef.child("loyalty").child(vendor.id!!).child("redemptions").child("count").setValue(100)
        loyaltyProgress = findViewById(R.id.loyalty_progress)
        loyaltyText = findViewById(R.id.loyalty_text)


        vendor = intent.getParcelableExtra(ARG_VENDOR)
        points = intent.getStringExtra(POINTS)

        val code = rawResult.toString()
        println("THE PARCELABLE POINTS VALUE")
        println(points)

        if (code == vendor!!.loyaltyCode) {
            //loyaltyProgress!!.progress += 10
            println("Codes are the same")
            println(loyaltyProgress)
            println(loyaltyText)
        }
        onBackPressed()
    }
}
