package com.CP.Savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var scannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        scannerView = ZXingScannerView(this)
        setContentView(R.layout.activity_scan)

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
        onBackPressed()
    }
}
