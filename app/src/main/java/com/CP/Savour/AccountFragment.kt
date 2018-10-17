package com.CP.Savour

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_account.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class AccountFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {




        val view = inflater.inflate(R.layout.fragment_account, container, false)
        var logoutButton: Button = view.findViewById(R.id.logout_button)
        logoutButton.setOnClickListener{
            AuthUI.getInstance().signOut(this.context!!)
            .addOnCompleteListener {
                var setupIntent =  Intent(this.context!!, LoginActivity::class.java)
                setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(setupIntent)
            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mAuth = FirebaseAuth.getInstance()

        val currentUser = mAuth.currentUser

/**
        if (currentUser!!.photoUrl != null) {
            var profileImage = frameLayout!!.findViewById(R.id.profile_image) as ImageView
            Glide.with(this)
                    .load(currentUser!!.photoUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(profileImage!!)
        }
        */
        for(data in currentUser!!.providerData) {
            println("Photo stuff! " + data.photoUrl)
        }
        println("Current User information: " + currentUser!!.providerData.toString())
    }
    companion object {
        fun newInstance(): AccountFragment = AccountFragment()
    }




}
