package com.CP.Savour

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat


import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.MenuItem
import android.widget.FrameLayout
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.content.Intent
import com.onesignal.OneSignal


class MainActivity : AppCompatActivity() {
    private var content: FrameLayout? = null
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    val MY_PERMISSIONS_REQUEST_CAMERA = 100
    var dealFragment = DealsFragment()
    var favoriteFragment = FavoritesFragment()
    var vendorFragment = VendorFragment()
    var accountfragment = AccountFragment()
    var active: Fragment = dealFragment
    var firstEnter = true


    private val mOnNavigationItemSelectedListener = object : BottomNavigationView.OnNavigationItemSelectedListener {

        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.navigation_deals -> {
                    if(active == dealFragment) {
                        supportFragmentManager
                                .beginTransaction()
                                .show(dealFragment)
                                .commit()
                    }
                    else {
                        supportFragmentManager
                                .beginTransaction()
                                .hide(active)
                                .show(dealFragment)
                                .commit()
                    }
                    active = dealFragment
                    return true
                }
                R.id.navigation_favorites -> {
                    supportFragmentManager
                            .beginTransaction()
                            .hide(active)
                            .show(favoriteFragment)
                            .commit()
                    active = favoriteFragment
                    return true
                }
                R.id.navigation_vendors -> {
                    supportFragmentManager
                            .beginTransaction()
                            .hide(active)
                            .show(vendorFragment)
                            .commit()
                    active = vendorFragment
                    return true
                }
                R.id.navigation_account -> {
                    supportFragmentManager
                            .beginTransaction()
                            .hide(active)
                            .show(accountfragment)
                            .commit()
                    active = accountfragment
                    return true
                }
            }
            return false
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.onResume()

        if (!firstEnter){
            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
        }
        firstEnter = false

        setContentView(R.layout.activity_main)
        content = findViewById(R.id.content) as FrameLayout
        val navigation = findViewById(R.id.navigation_view) as BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction().add(R.id.content,accountfragment).commit()
        supportFragmentManager.beginTransaction().hide(accountfragment).add(R.id.content, vendorFragment).commit()
        supportFragmentManager.beginTransaction().hide(vendorFragment).add(R.id.content, favoriteFragment).commit()
        supportFragmentManager.beginTransaction().hide(favoriteFragment).add(R.id.content, dealFragment).commit()

        checkLocationPermission()
        //refreshFragments()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.deals, menu)

        return true
    }


    private fun refreshFragments() {
//        supportFragmentManager.beginTransaction().add(R.id.content,accountfragment).commit()
//        supportFragmentManager.beginTransaction().hide(accountfragment).add(R.id.content, vendorFragment).commit()
//        supportFragmentManager.beginTransaction().hide(vendorFragment).add(R.id.content, favoriteFragment).commit()
//        supportFragmentManager.beginTransaction().hide(favoriteFragment).add(R.id.content, dealFragment).commit()

        supportFragmentManager.beginTransaction().detach(vendorFragment).attach(vendorFragment).commit()
        supportFragmentManager.beginTransaction().detach(favoriteFragment).attach(favoriteFragment).commit()
        supportFragmentManager.beginTransaction().detach(dealFragment).attach(dealFragment).commit()

    }

    fun checkLocationPermission(): Boolean {

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||  ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                AlertDialog.Builder(this)
                        .setTitle("Location Permissions")
                        .setMessage("We need your location to show you sweet deals nearby!")
                        .setPositiveButton("OK", DialogInterface.OnClickListener { dialogInterface, i ->
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(this@MainActivity,
                                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                    MY_PERMISSIONS_REQUEST_LOCATION)
                        })
                        .create()
                        .show()


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA),
                        MY_PERMISSIONS_REQUEST_LOCATION)

            }
            return false
        } else {
            return true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        println("First")
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    println("Second")

                    refreshFragments()
                    //finish()
                    //startActivity(intent)
                } else if(grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    println("SECOND PERMISSION BABY WOO")
                    refreshFragments()

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //refreshFragments()
                    //finish()
                    //startActivity(intent)
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
                }
            }
        }

    override fun finish() {
        super.finish()
        supportFragmentManager.beginTransaction().detach(dealFragment).commit()
        supportFragmentManager.beginTransaction().detach(favoriteFragment).commit()
        supportFragmentManager.beginTransaction().detach(vendorFragment).commit()
        supportFragmentManager.beginTransaction().detach(accountfragment).commit()
        onLeaveThisActivity()
    }

    protected fun onLeaveThisActivity() {
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
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