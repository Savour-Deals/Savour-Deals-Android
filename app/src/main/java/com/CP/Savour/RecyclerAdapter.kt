package com.CP.Savour

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.net.URI

/**
 * The recycler adapter class creates the individual cards that are on display in the main activity
 */
class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private val restaurants = arrayOf("Purple Onion")
    private val resturantDescriptions = arrayOf("The purple onion is yummy!")
    private val images = mutableMapOf<String,String>()
    private var vendors : MutableList<String> = mutableListOf()
    private var testArray = mutableMapOf<String?, Any>()
    public val Context.picasso: Picasso
    get() = Picasso.get()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.card_layout,viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        var  vendorReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors")

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


                        println(children.child("photo").getValue().toString())
                        val url = children.child("photo").getValue().toString()
                        println(children.key)

                        /**
                         * Since the datasnapshot contains all of the vendors, and the
                         */
                        vendors[j] = children.key!!
                        images[children.key!!] = url
                        //  loads the image from the url into the desired tag
                        Picasso.get().load(url).into(viewHolder.itemImage)

                    }.first()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        vendorReference.addValueEventListener(vendorListener)
    }
    override fun getItemCount(): Int {
        return restaurants.size
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView


        init {
            itemImage = itemView.findViewById(R.id.item_image)

        }
    }
}