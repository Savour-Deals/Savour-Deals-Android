package com.CP.Savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView


import android.support.v4.app.Fragment
import android.view.MenuItem
import android.widget.FrameLayout





class MainActivity : AppCompatActivity() {
    private var content: FrameLayout? = null

    private val mOnNavigationItemSelectedListener = object : BottomNavigationView.OnNavigationItemSelectedListener {

        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.navigation_deals -> {
                    val fragment = DealsFragment()
                    addFragment(fragment)
                    return true
                }
                R.id.navigation_favorites -> {
                    val fragment = FavoritesFragment()
                    addFragment(fragment)
                    return true
                }
                R.id.navigation_vendors -> {
                    var fragment = VendorFragment()
                    addFragment(fragment)
                    return true
                }
                R.id.navigation_account -> {
                    var fragment = AccountFragment()
                    addFragment(fragment)
                    return true
                }
            }
            return false
        }

    }

    /**
     * add/replace fragment in container [framelayout]
     */
    private fun addFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.content, fragment, fragment.javaClass.getSimpleName())
                .addToBackStack(fragment.javaClass.getSimpleName())
                .commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        content = findViewById(R.id.content) as FrameLayout
        val navigation = findViewById(R.id.navigation_view) as BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


        val fragment = DealsFragment()
        addFragment(fragment)
    }

}
