package com.CP.Savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null

    lateinit var vendorQuery : Query
    lateinit var recyclerView: RecyclerView
    lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<Vendor,VendorViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        restaurant_list.layoutManager = LinearLayoutManager(this)

        vendorQuery = FirebaseDatabase.getInstance().getReference().child("Vendors")
    }

    /**
     * This method will be registered to a firebase object
     * and will load the values into the cardview and display it to the recyclerview
     */
    private fun loadFirebaseData() {
        val options = FirebaseRecyclerOptions.Builder<Vendor>()
                .setQuery(vendorQuery,Vendor::class.java)
                .build()

       firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Vendor, VendorViewHolder> (options) {
            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): VendorViewHolder {
                val view = LayoutInflater.from(parent.baseContext).inflate(R.layout.card_layout,viewGroup,false)
                return VendorViewHolder(view)
            }

            override fun onBindViewHolder(holder: VendorViewHolder, position: Int, model: Vendor) {

            }

        }
    }

    inner class VendorViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {
        var itemImage: ImageView

        init {
            itemImage = itemView.findViewById(R.id.item_image)
        }
    }
}
