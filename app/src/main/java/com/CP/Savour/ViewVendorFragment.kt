package com.CP.Savour

import android.content.Context
import android.graphics.drawable.ScaleDrawable
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.github.debop.kodatimes.today
import java.util.*
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_VENDOR = "vendor"


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ViewVendorFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ViewVendorFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class ViewVendorFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var vendor: Vendor
    private lateinit var dealImage: ImageView
    private lateinit var vendorName: TextView
    private lateinit var directionsButton: Button
    private lateinit var followButton: Button
    private lateinit var menuButton: Button
    private lateinit var address: TextView
    private lateinit var hours: TextView
    private lateinit var description: TextView
    private lateinit var seeMore: TextView
    private lateinit var descriptionContainer: ConstraintLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var userListener: ValueEventListener

    private  var user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private var descriptionExpanded = false


    val userInfoRef = FirebaseDatabase.getInstance().getReference("Users").child(user!!.uid)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vendor = it.getParcelable(ARG_VENDOR) as Vendor
        }



        println("User Info")
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_vendor, container, false)

        dealImage = view.findViewById(R.id.view_vendor_image)
        vendorName = view.findViewById(R.id.view_vendor_name)
        address = view.findViewById(R.id.vendor_address)
        hours = view.findViewById(R.id.vendor_hours)
        description = view.findViewById(R.id.description)
        seeMore = view.findViewById(R.id.see_more)
        descriptionContainer = view.findViewById(R.id.info_container)

        menuButton = view.findViewById(R.id.vendor_menu)
        directionsButton = view.findViewById(R.id.vendor_directions)
        followButton = view.findViewById(R.id.vendor_follow)



        userListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                println("triggered")
                if (snapshot.child("following").child(vendor.id!!).exists()) {
                    followButton.background = ContextCompat.getDrawable(context!!,  R.drawable.vendor_button)
                    followButton.text = "Following"
                    println("Following")

                } else {
                    followButton.background = ContextCompat.getDrawable(context!!,  R.drawable.vendor_button_selected)
                    followButton.text = "Follow"
                    println("Follow")

                }
            }

            override fun onCancelled(dbError: DatabaseError) {
                println("Ballsack")
            }
        }

        userInfoRef.addValueEventListener(userListener)

        followButton.setOnClickListener {
            if (followButton.text == "Follow") {

                userInfoRef.child("following").child(vendor.id!!).setValue(true)
                println("set value")

            } else {
                userInfoRef.child("following").child(vendor.id!!).removeValue()
                println("remove")
            }
        }
        vendorName.text = vendor.name
        address.text = vendor.address
        hours.text = vendor.dailyHours[Calendar.DAY_OF_WEEK-1]
        description.text = vendor.description

        Glide.with(this)
                .load(vendor.photo)
                .into(dealImage)

        descriptionContainer.setOnClickListener {
            if (!descriptionExpanded){
                descriptionExpanded = true
                seeMore.text = "tap to see less..."
                val params = description.layoutParams
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                description.layoutParams = params
            }else{
                descriptionExpanded = false
                val scale = resources.displayMetrics.scaledDensity
                seeMore.text = "tap to see more..."
                val params = description.layoutParams
                params.height = (36 * scale).toInt()
                description.layoutParams = params
            }
        }

        menuButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(vendor.menu))
            startActivity(browserIntent)
        }

        directionsButton.setOnClickListener {
            val url = "http://maps.google.com/maps?daddr="+ vendor.address +"&mode=driving"
            val intent = Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url))
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity")
            startActivity(intent)
        }



        // getting the buttons, and scaling their logo
        val scaledMap = ScaleDrawable(ContextCompat.getDrawable(context!!, R.drawable.icon_business),0, 5f,5f)
        val directionsButton = view.findViewById<Button>(R.id.vendor_directions)
        directionsButton.setCompoundDrawables(null, null,null,scaledMap)
        return view

    }

    override fun onDestroy() {
        super.onDestroy()

        followButton.setOnClickListener(null)
        userInfoRef.removeEventListener(userListener)
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewVendorFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(): ViewVendorFragment = ViewVendorFragment()

    }
}
