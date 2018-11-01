package com.CP.Savour

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_create_account.view.*
import org.w3c.dom.Text
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent
import android.widget.*
import com.CP.Savour.R.id.textView




private const val ARG_DEAL = "deal"
private const val ARG_VENDOR = "vendor"
/**
 * The recycler adapter class creates the individual cards that are on display in the main activity
 */
class DealsRecyclerAdapter(val deals: List<Deal?>,val vendors: Map<String, Vendor?>, val context: Context) : RecyclerView.Adapter<DealsRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): ViewHolder {
        
        val v = LayoutInflater.from(viewGroup.context).inflate(R.layout.deal_card_layout,viewGroup, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        var temp = deals[i]!!
        viewHolder.deal = temp
        viewHolder.vendor = vendors[temp.vendorID]

        Glide.with(context).load(temp.photo).thumbnail(0.5f)
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

        if (temp.active!!){
            viewHolder.activeText.visibility = View.INVISIBLE
        }else{
            viewHolder.activeText.text = "Deal is currently unavailable. This deal is valid " + temp.inactiveString
            viewHolder.activeText.visibility = View.VISIBLE
        }
        if (temp.activeHours != ""){
            viewHolder.timeText.text = "valid from " + temp.activeHours
            viewHolder.timeText.height = 0
        }else{
            viewHolder.timeText.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }

        if (temp.redeemed!!) {
            viewHolder.countdownText.text = "Deal Already Redeemed!"
            viewHolder.timeText.text = ""
            viewHolder.countdownView.visibility = View.VISIBLE
        }
        else if (temp.daysLeft!! < 8){
            viewHolder.countdownView.visibility = View.VISIBLE
            viewHolder.countdownText.text = temp.countdown
        }else{
            viewHolder.countdownView.visibility = View.INVISIBLE
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
        var deal : Deal? = null
        var vendor: Vendor? = null
        var activeText: TextView
        var timeText: TextView
        var countdownView: LinearLayout
        var countdownText: TextView

        init {
            itemImage = itemView.findViewById(R.id.item_image)
            vendorName = itemView.findViewById(R.id.vendorName)
            dealDescription = itemView.findViewById(R.id.description)
            days = itemView.findViewById(R.id.days)
            dots = itemView.findViewById(R.id.dots)
            favorite = itemView.findViewById(R.id.favButton)
            activeText = itemView.findViewById(R.id.activetext)
            timeText = itemView.findViewById(R.id.time)
            countdownView = itemView.findViewById(R.id.countdown_view)
            countdownText = itemView.findViewById(R.id.countdown_text)

            itemView.setOnClickListener {
                val intent = Intent(context, DealActivity::class.java)
                intent.putExtra(ARG_DEAL, deal)
                intent.putExtra(ARG_VENDOR, vendor)
                context.startActivity(intent)
            }
        }
    }
}