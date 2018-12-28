package com.CP.Savour

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.net.URL
import android.os.StrictMode
import android.util.Log
import android.widget.TextView
import com.facebook.login.LoginManager
import com.google.android.gms.appinvite.AppInviteInvitation


class AccountFragment : Fragment() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var authStateListner: FirebaseAuth.AuthStateListener
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mAuth = FirebaseAuth.getInstance()
        authStateListner = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if(user != null){
            }
        }
        mAuth.addAuthStateListener(authStateListner)

        val view = inflater.inflate(R.layout.fragment_account, container, false)
        var logoutButton: Button = view.findViewById(R.id.logout_button)
        var profileImage: ImageView = view.findViewById(R.id.profile_image)
        var userName: TextView = view.findViewById(R.id.name)
        var contactButton: View = view.findViewById(R.id.contact_view)
        var shareButton: View = view.findViewById(R.id.friend_share)

        if (mAuth.currentUser != null) {
            if (mAuth.currentUser!!.displayName != null) {
                userName.text = mAuth.currentUser!!.displayName
            }
        }
        logoutButton.setOnClickListener{
            LoginManager.getInstance().logOut()
            AuthUI.getInstance().signOut(this.context!!)
            .addOnCompleteListener {
                var setupIntent =  Intent(this.context!!, LoginActivity::class.java)
                setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(setupIntent)
                activity!!.finish()
            }
        }
        var ref = FirebaseDatabase.getInstance().getReference("Users").child(mAuth!!.uid!!).child("facebook_id")
//        self.friendsText = "Click here to invite your friends to Savour Deals!"

        contactButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.savourdeals.com/contact/")))
        }

        shareButton.setOnClickListener {
            var sharingIntent = Intent(android.content.Intent.ACTION_SEND)
            sharingIntent.setType("text/plain")
            val shareBody =  getString(R.string.invitation_message) + " " + getString(R.string.invitation_deep_link_kotlin)
            val shareSub = getString(R.string.invitation_title)
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub)
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
            startActivityForResult(Intent.createChooser(sharingIntent, "Share using"),1)

        }



        val SDK_INT = android.os.Build.VERSION.SDK_INT
        if (SDK_INT > 8) {
            val policy = StrictMode.ThreadPolicy.Builder()
                    .permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        Glide.with(context!!.applicationContext)
                .load(R.drawable.icon_user)
                .apply(RequestOptions.circleCropTransform())
                .into(profileImage)
        val fbListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val value = dataSnapshot.value as? String
                    val imageURL = URL("https://graph.facebook.com/$value/picture?type=large")

                    val img = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream())
                    Glide.with(context!!.applicationContext)
                            .load(img)
                            .apply(RequestOptions.circleCropTransform())
                            .into(profileImage)
                }
//                }else{
//                    Glide.with(context!!.applicationContext)
//                            .load(R.drawable.savour_fullcolor)
//                            .apply(RequestOptions.circleCropTransform())
//                            .into(profileImage)
//                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("cancelled fbListener")
            }
        }
        ref.addListenerForSingleValueEvent(fbListener)


        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onStart() {
        super.onStart()

    }

    override fun onPause() {
        super.onPause()
        mAuth.removeAuthStateListener(authStateListner)
    }

    companion object {
        fun newInstance(): AccountFragment = AccountFragment()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        print( "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                // Get the invitation IDs of all sent messages
                val ids = AppInviteInvitation.getInvitationIds(resultCode, data!!)
                for (id in ids) {
                    print( "onActivityResult: sent invitation $id")
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // ...
            }
        }
    }

}
