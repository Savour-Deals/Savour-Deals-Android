package com.CP.Savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<RecyclerAdapter.ViewHolder>? = null

    lateinit var vendorReference : DatabaseReference
    lateinit var recyclerView: RecyclerView
    lateinit var firebaseRecyclerAdapter : FirebaseRecyclerAdapter<Vendor,VendorsViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))

        recyclerView = findViewById(R.id.restaurant_list)

        vendorReference = FirebaseDatabase.getInstance().getReference("Vendors")
    }

    /**
     * This method will be registered to a firebase object
     * and will load the values into the cardview and display it to the recyclerview
     */
    private fun loadFirebaseData() {
       firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<Vendor, VendorsViewHolder> (Vendor::class.java,R.id.card_view, VendorsViewHolder::class.java,) {
            override fun onCreateViewHolder(p0: ViewGroup, p1: Int): VendorsViewHolder {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onBindViewHolder(holder: VendorsViewHolder, position: Int, model: Vendor) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }
    }

    inner class VendorsViewHolder(var mView: View) : RecyclerView.ViewHolder(mView) {

    }
}
