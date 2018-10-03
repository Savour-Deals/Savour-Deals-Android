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
import android.R.string.ok
import android.R.string.ok
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.widget.Toast
import android.widget.Toolbar


class MainActivity : AppCompatActivity() {
    private var content: FrameLayout? = null
    val MY_PERMISSIONS_REQUEST_LOCATION = 99
    val dealFragment = DealsFragment()
    val favoriteFragment = FavoritesFragment()
    var vendorFragment = VendorFragment()
    var accountfragment = AccountFragment()
    var active: Fragment = dealFragment


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
        setContentView(R.layout.activity_main)
        content = findViewById(R.id.content) as FrameLayout
        val navigation = findViewById(R.id.navigation_view) as BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction().add(R.id.content,accountfragment).commit()
        supportFragmentManager.beginTransaction().hide(accountfragment).add(R.id.content, vendorFragment).commit()
        supportFragmentManager.beginTransaction().hide(vendorFragment).add(R.id.content, favoriteFragment).commit()
        supportFragmentManager.beginTransaction().hide(favoriteFragment).add(R.id.content, dealFragment).commit()

        checkLocationPermission()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.deals, menu)

        return true
    }


    fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)) {

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
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        MY_PERMISSIONS_REQUEST_LOCATION)
            }
            return false
        } else {
            return true
        }
    }


}
