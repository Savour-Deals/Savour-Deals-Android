package com.CP.Savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        val vendors = getFirebaseData()
        layoutManager = LinearLayoutManager(this)
    }

    private fun getFirebaseData() : MutableMap<String, Any> {
        var  vendorReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors")
        var vendors = mutableMapOf<String,Any>()
        var urls = mutableListOf<String>()
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
                    var j: Int = 0

                    dataSnapshot.children.filter {
                        children -> children.hasChildren()
                    }.map {
                        children -> children.key

                        vendors[children.key!!] = children

                        println(children.child("photo").getValue().toString())
                        val url = children.child("photo").getValue().toString()
                        urls.add(url)
                        /**
                         * Since the datasnapshot contains all of the vendors, and the
                         */
                        //vendors[j] = children.key!!
                        //images[children.key!!] = url
                        //  loads the image from the url into the desired tag


                    }.first()
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
