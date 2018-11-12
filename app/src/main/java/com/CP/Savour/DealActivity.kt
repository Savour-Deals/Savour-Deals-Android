package com.CP.Savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import android.content.Intent
import android.graphics.PorterDuff
import android.widget.ImageView


private const val ARG_DEAL = "deal"
private const val ARG_VENDOR = "vendor"

class DealActivity : AppCompatActivity() {
    var savourImg: ImageView? = null
    private var content: FrameLayout? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_deal)
        content = findViewById(R.id.content) as FrameLayout

        val deal = getIntent().getParcelableExtra(ARG_DEAL) as? Deal
        val vendor = getIntent().getParcelableExtra(ARG_VENDOR) as? Vendor


        val bundle = Bundle()
        bundle.putParcelable(ARG_DEAL, deal)
        bundle.putParcelable(ARG_VENDOR, vendor)


        savourImg = findViewById(R.id.logo) as ImageView
        val toolbar = findViewById(R.id.deal_toolbar) as Toolbar
        setSupportActionBar(toolbar)

        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
        toolbar.getNavigationIcon()!!.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP)


        Glide.with(this)
                .load(R.drawable.savour_white)
                .into(savourImg!!)

        val fragobj = ViewDealFragment()

        fragobj.arguments = bundle

        supportFragmentManager.beginTransaction().add(R.id.content,fragobj).commit()
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
