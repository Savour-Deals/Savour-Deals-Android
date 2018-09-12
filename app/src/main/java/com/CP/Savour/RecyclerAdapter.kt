package com.CP.Savour

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.database.*

/**
 * The recycler adapter class creates the individual cards that are on display in the main activity
 */
class RecyclerAdapter : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private val restaurants = arrayOf("Purple Onion")
    private val resturantDescriptions = arrayOf("The purple onion is yummy!")
    private val images = intArrayOf(R.drawable.patio)
    public var vendors = mutableListOf<Vendor>()
    private var testArray = mutableMapOf<String?, Any>()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.card_layout,viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        var  vendorReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Vendors")

        viewHolder.itemTitle.text = restaurants[i]
        viewHolder.itemDetail.text = resturantDescriptions[i]
        viewHolder.itemImage.setImageResource(images[i])


        println("Reference toString " + vendorReference.toString())

        val vendorListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    vendors.clear()
                    dataSnapshot.children.mapNotNullTo(vendors) {
                        it!!.getValue<Vendor>(Vendor::class.java)
                    }
                    // looping through the data snapshot containing all of the vendors
                    for(child: DataSnapshot in dataSnapshot.children) {
                        // adding the vendors to a hashmap
                        //this doesn't like it for whatever reason
                        //vendors.add(child.getValue(Vendor::class.java)!!)
                        child.getValue() as MutableMap<String,Any>
                        testArray[child.key] = child.getValue() as MutableMap<String,Any>
                    }

                    for(item in testArray) {

                        var subitem = item.value.to(HashMap<String,Any>())
                        println(subitem.toString())

                    }
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
        var itemTitle: TextView
        var itemDetail: TextView

        init {
            itemImage = itemView.findViewById(R.id.item_image)
            itemTitle = itemView.findViewById(R.id.item_title)
            itemDetail = itemView.findViewById(R.id.item_detail)
        }
    }
}