package com.CP.Savour

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


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
    private var vendor: Vendor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            vendor = it.getParcelable(ARG_VENDOR) as? Vendor
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_vendor, container, false)
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
