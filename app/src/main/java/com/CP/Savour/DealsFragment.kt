package com.CP.Savour


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.ActionBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_deals.*
import kotlinx.android.synthetic.main.fragment_vendor.*


class DealsFragment : Fragment() {
    private var layoutManager : RecyclerView.LayoutManager? = null
    private var adapter : RecyclerView.Adapter<DealsRecyclerAdapter.ViewHolder>? = null
    private lateinit var recyclerView : RecyclerView
    private var toolbar : ActionBar? = null

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        setRetainInstance(true)
        // retrieving the vendors from the database
        val deals = getFirebaseData()
        layoutManager = LinearLayoutManager(context)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_deals, container, false)
    }

    companion object {
        fun newInstance(): DealsFragment = DealsFragment()
    }

    private fun getFirebaseData() : ArrayList<Any> {
        var  dealsReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Deals")
        var deals: ArrayList<Any> = ArrayList()
        println("Reference toString " + dealsReference.toString())

        //TODO("retrieve data from firebase data base of the restaurants to display onto card views for main activity")
        /**
         * This event listener keeps track of the vendors object in the realtime database
         * If a change occurs to any value in the database, the ValueEventListener will be triggered
         * this allows us to update information on the fly and display updated vendor information
         */
        val dealsListener = object : ValueEventListener {
            /**
             * Listening for when the data has been changed
             * and also when we want to access f
             */
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {

                    for (dealSnapshot in dataSnapshot.children) {
                        deals.add(dealSnapshot.value!!)
                    }

                    adapter = DealsRecyclerAdapter(deals, context!!)

                    deal_list.layoutManager = layoutManager


                    deal_list.adapter = adapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        dealsReference.addValueEventListener(dealsListener)
        return deals
    }

}
