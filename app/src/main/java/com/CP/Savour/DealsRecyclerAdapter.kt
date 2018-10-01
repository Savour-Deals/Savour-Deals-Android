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
import com.google.zxing.client.result.TextParsedResult
import com.squareup.picasso.Picasso
import java.net.URI

/**
 * The recycler adapter class creates the individual cards that are on display in the main activity
 */
class DealsRecyclerAdapter(val deals: ArrayList<Any>) : RecyclerView.Adapter<DealsRecyclerAdapter.ViewHolder>() {



    public val Context.picasso: Picasso
        get() = Picasso.get()

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.deal_card_layout,viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        var temp = deals[i] as HashMap<String, Any>
        Picasso.get().setIndicatorsEnabled(false)

        val img = temp.getValue("photo")?.let {
            Picasso.get().load(temp.getValue("photo").toString()).into(viewHolder.itemImage)
        }
        viewHolder.vendorName.text = temp.getValue("vendor_name").toString()
        viewHolder.dealDescription.text = temp.getValue("deal_description").toString()
    }
    override fun getItemCount(): Int {
        println("VendorMap Size: " + deals.size)
        return deals.size
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView
        var vendorName: TextView
        var dealDescription: TextView


        init {
            itemImage = itemView.findViewById(R.id.item_image)
            vendorName = itemView.findViewById(R.id.vendorName)
            dealDescription = itemView.findViewById(R.id.description)
        }
    }
}