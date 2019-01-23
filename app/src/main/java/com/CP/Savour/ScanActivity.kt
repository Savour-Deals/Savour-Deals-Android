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

private const val CODE = "code"
private const val RESULT_OK = 1
private const val RESULT_FAIL = -1
class ScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var scannerView: ZXingScannerView? = null
    private var loyaltyProgress: ProgressBar? = null
    private var loyaltyText: TextView? = null
    private var user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

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

        val code = rawResult.toString()

        //send code back to viewvendorfragmentz
        intent.putExtra(CODE,code)
        setResult(Activity.RESULT_OK,intent)
        onBackPressed()
    }
}
