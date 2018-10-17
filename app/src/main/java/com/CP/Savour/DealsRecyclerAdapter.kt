package com.CP.Savour

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_account.view.*
import org.w3c.dom.Text

/**
 * The recycler adapter class creates the individual cards that are on display in the main activity
 */
class DealsRecyclerAdapter(val deals: List<Deal?>, val context: Context) : RecyclerView.Adapter<DealsRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.deal_card_layout,viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        var temp = deals[i]!!
        Picasso.get().setIndicatorsEnabled(false)


            Glide.with(context).load(temp.photo)
                    .into(viewHolder.itemImage)
        viewHolder.vendorName.text = temp.vendorName + " - %.1f miles".format(temp.distanceMiles)
        viewHolder.dealDescription.text = temp.dealDescription

        var dots = ""
        for (day in temp.activeDays){
            if (day!!){
                dots +=  "• "
            }else{
                dots += "◦ "
            }
        }
        viewHolder.dots.text = dots
        viewHolder.days.text = "Su. Mo. Tu. We. Th. Fr. Sa."
        if (temp.favorited!!){
            viewHolder.favorite.setBackgroundResource(R.drawable.filled_heart)
        }else{
            viewHolder.favorite.setBackgroundResource(R.drawable.icon_heart)
        }

        viewHolder.favorite.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            val favoriteRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid).child("favorites").child(temp.id!!)

            temp.favorited = !temp.favorited!!
            if (temp.favorited!!){
                favoriteRef.setValue(temp.id!!)
                viewHolder.favorite.setBackgroundResource(R.drawable.filled_heart)
            }else{
                favoriteRef.removeValue()
                viewHolder.favorite.setBackgroundResource(R.drawable.icon_heart)
            }
        }
    }
    override fun getItemCount(): Int {
        println("dealsMap Size: " + deals.size)
        return deals.size
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemImage: ImageView
        var vendorName: TextView
        var dealDescription: TextView
        var days : TextView
        var dots : TextView
        var favorite : ImageButton


        init {
            itemImage = itemView.findViewById(R.id.item_image)
            vendorName = itemView.findViewById(R.id.vendorName)
            dealDescription = itemView.findViewById(R.id.description)
            days = itemView.findViewById(R.id.days)
            dots = itemView.findViewById(R.id.dots)
            favorite = itemView.findViewById(R.id.favButton)
        }
    }
}