package com.CP.Savour

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.joda.time.DateTime

private const val ARG_VENDOR = "vendor"
private const val POINTS = "points"
private const val RESULT_OK = 1
private const val RESULT_FAIL = -1
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

        println("Saved instance state of the ScanActivity")
        println(savedInstanceState.toString())
        loyaltyProgress = findViewById(R.id.loyalty_progress)
        loyaltyText = findViewById(R.id.loyalty_text)

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

        vendor = intent.getParcelableExtra(ARG_VENDOR)
        points = intent.getStringExtra(POINTS)


        println(userInfoRef.child("loyalty").child(vendor.id!!).child("redemptions").child("count"))




        val code = rawResult.toString()

        println("THE PARCELABLE POINTS VALUE")
        println(points)
        val day = DateTime.now().dayOfWeek
        println("Day of week: ")
        println(day)
        intent.putExtra("Test","Hello, World!")
        intent.putExtra(POINTS,points)
        if (code == vendor.loyaltyCode) {
            val intent = Intent()
            points?.let {
                var pts =  it.toInt()

                pts += vendor.loyaltyPoints[day - 1]

                intent.putExtra(POINTS,pts)
            }

            setResult(Activity.RESULT_OK,intent)
        }
        onBackPressed()
    }
}
