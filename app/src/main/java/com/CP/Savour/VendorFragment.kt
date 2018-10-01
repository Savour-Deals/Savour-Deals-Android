package com.CP.Savour

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.LinearLayoutManager

import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.database.DataSnapshot
import kotlinx.android.synthetic.main.fragment_vendor.*


class VendorFragment : Fragment() {

    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null
    private lateinit var recyclerView : RecyclerView
    private var toolbar : ActionBar? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)

        // retrieving the vendors from the database
        val vendors = getFirebaseData()
        layoutManager = LinearLayoutManager(context)

        return inflater.inflate(R.layout.fragment_vendor, container, false)
    }

    companion object {
        fun newInstance(): VendorFragment = VendorFragment()
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

                    adapter = RecyclerAdapter(vendors)

                    vendor_list.layoutManager = layoutManager


                    vendor_list.adapter = adapter
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
