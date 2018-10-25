package com.CP.Savour

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextClock
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


private const val ARG_DEAL = "deal"
private const val ARG_VENDOR = "vendor"


class ViewDealFragment : Fragment() {
    private var deal: Deal? = null
    private var vendor: Vendor? = null

    private var dealImg: ImageView? = null
    private var vendorName: TextView? = null
    private var description: TextView? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            deal = it.getParcelable(ARG_DEAL) as Deal
//            vendor = it.getParcelable(ARG_VENDOR) as Vendor
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setRetainInstance(true)

        val view = inflater.inflate(R.layout.fragment_view_deal, container, false)

        dealImg = view.findViewById(R.id.dealImg) as? ImageView
        vendorName = view.findViewById(R.id.name) as? TextView
        description = view.findViewById(R.id.description) as? TextView


        Glide.with(this)
                .load(deal!!.photo)
                .apply(RequestOptions.circleCropTransform())
                .into(dealImg!!)

        vendorName!!.text = deal!!.vendorName
        description!!.text = deal!!.dealDescription

        return view
    }




    companion object {
        fun newInstance(): ViewDealFragment = ViewDealFragment()


    }
}
