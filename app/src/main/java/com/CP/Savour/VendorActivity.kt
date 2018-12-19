package com.CP.Savour

import android.content.Intent
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.Glide

private const val ARG_VENDOR = "vendor"

/**
 * This activity displays the individual vendor page
 */
class VendorActivity : AppCompatActivity() {
    private var content: FrameLayout? = null
    var savourImg: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vendor)
        content = findViewById(R.id.vendor_content) as FrameLayout
        // To get to this page, an intent is used with extras to pass data about the vendor
        val vendor = intent.getParcelableExtra<Vendor>(ARG_VENDOR)

        // passing data to the fragment
        val bundle = Bundle()

        bundle.putParcelable(ARG_VENDOR, vendor)


        // load images into the toolbar
        savourImg = findViewById(R.id.logo) as ImageView
        val toolbar = findViewById(R.id.vendor_toolbar) as Toolbar
        setSupportActionBar(toolbar)

        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        toolbar.getNavigationIcon()!!.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP)

        Glide.with(this)
                .load(R.drawable.savour_white)
                .into(savourImg!!)

        val fragment = ViewVendorFragment()

        fragment.arguments = bundle

        supportFragmentManager.beginTransaction().add(R.id.vendor_content, fragment).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        onLeaveThisActivity()
    }

    protected fun onLeaveThisActivity() {
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
    }


    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        onStartNewActivity()
    }

    override fun startActivity(intent: Intent, options: Bundle?) {
        super.startActivity(intent, options)
        onStartNewActivity()
    }

    protected fun onStartNewActivity() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
    }
}
