package com.CP.Savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.database.DataSnapshot



class MainActivity : AppCompatActivity() {
    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null
    lateinit var recyclerView : RecyclerView
    lateinit var toolbar : ActionBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // defining the bottom level navigation bar
        //toolbar = supportActionBar!!
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.navigation_view)

        // defining the top level action bar
        setSupportActionBar(findViewById(R.id.my_toolbar))

        // retrieving the vendors from the database
        val vendors = getFirebaseData()
        layoutManager = LinearLayoutManager(this)
    }

    private fun getFirebaseData() : ArrayList<Any> {
        var  vendorReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors")
        var vendors: ArrayList<Any> = ArrayList()
        println("Reference toString " + vendorReference.toString())

        //TODO("retrieve data from firebase data base of the restaurants to display onto card views for main activity")
        /**
         * This event listener keeps track of the vendors object in the realtime database
         * If a change occurs to any value in the database, the ValueEventListener will be triggered
         * this allows us to update information on the fly and display updated vendor information
         */
        val vendorListener = object : ValueEventListener {
            /**
             * Listening for when the data has been changed
             * and also when we want to access f
             */
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (vendorSnapshot in dataSnapshot.children) {
                        vendors.add(vendorSnapshot.value!!)
                    }

//                    dataSnapshot.children.filter {
//                        children -> children.hasChildren()
//                    }.map {
//                        children -> children.key
//
//                        vendors = children.

//                        println(children.child("photo").getValue().toString())
//                        val url = children.child("photo").getValue().toString()
//                        urls.add(url)
                        /**
                         * Since the datasnapshot contains all of the vendors, and the
                         */
                        //vendors[j] = children.key!!
                        //images[children.key!!] = url
                        //  loads the image from the url into the desired tag
                    adapter = RecyclerAdapter(vendors)

                    restaurant_list.layoutManager = layoutManager


                    restaurant_list.adapter = adapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        vendorReference.addValueEventListener(vendorListener)
        return vendors
    }


}
