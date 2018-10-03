package com.CP.Savour

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

/**
 * The recycler adapter class creates the individual cards that are on display in the main activity
 */
class RecyclerAdapter(val vendors: List<Vendor?>, val context: Context) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {


        override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.card_layout,viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        var temp = vendors[i]

        if (temp!!.photo != null){
            Glide.with(context).load(temp.photo).into(viewHolder.itemImage)
        }
        viewHolder.vendorName.text = temp.name
        viewHolder.distanceLabel.text = "%.1f".format(temp.distanceMiles) + " Miles Away"
    }
    override fun getItemCount(): Int {
        println("VendorMap Size: " + vendors.size)
        return vendors.size
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView
        var vendorName: TextView
        var distanceLabel: TextView


        init {
            itemImage = itemView.findViewById(R.id.item_image)
            vendorName = itemView.findViewById(R.id.vendorName)
            distanceLabel = itemView.findViewById(R.id.distanceTo)
        }
    }
}