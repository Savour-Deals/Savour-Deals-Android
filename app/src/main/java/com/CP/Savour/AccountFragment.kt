package com.CP.Savour

import android.app.Activity
import android.content.DialogInterface
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
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AlertDialog.*
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.widget.TextView
import com.facebook.login.LoginManager
import com.google.android.gms.appinvite.AppInviteInvitation
import com.onesignal.OSSubscriptionObserver
import com.onesignal.OSSubscriptionStateChanges
import com.onesignal.OneSignal


class AccountFragment : Fragment(), OSSubscriptionObserver {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var authStateListner: FirebaseAuth.AuthStateListener
    private lateinit var notificationSwitch: SwitchCompat

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mAuth = FirebaseAuth.getInstance()
//        authStateListner = FirebaseAuth.AuthStateListener { auth ->
//            val user = auth.currentUser
//            if(user != null){
//            }
//        }
//        mAuth.addAuthStateListener(authStateListner)
        OneSignal.addSubscriptionObserver(this)


        val view = inflater.inflate(R.layout.fragment_account, container, false)
        var logoutButton: Button = view.findViewById(R.id.logout_button)
        var profileImage: ImageView = view.findViewById(R.id.profile_image)
        var userName: TextView = view.findViewById(R.id.name)
        var contactButton: View = view.findViewById(R.id.contact_view)
        var shareButton: View = view.findViewById(R.id.friend_share)
        var vendorButton: View = view.findViewById(R.id.vendor_view)
        notificationSwitch = view.findViewById(R.id.noti_switch)

        val onesignalState = OneSignal.getPermissionSubscriptionState().subscriptionStatus.subscribed
        val androidPermissionState = OneSignal.getPermissionSubscriptionState().permissionStatus.enabled
        notificationSwitch.isChecked = (onesignalState && androidPermissionState)

        if (mAuth.currentUser != null) {
            if (mAuth.currentUser!!.displayName != null) {
                userName.text = mAuth.currentUser!!.displayName
            }
        }
        logoutButton.setOnClickListener{
            logoutBtnOnclick()
        }
        contactButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.savourdeals.com/contact/")))
        }

        shareButton.setOnClickListener {
            shareBtnOnclick()
        }

        vendorButton.setOnClickListener{
            vendorBtnOnclick()
        }
        notificationSwitch.setOnClickListener {
            if (OneSignal.getPermissionSubscriptionState().permissionStatus.enabled){
                //if user has android notification permissions on, we are good
                if(notificationSwitch.isChecked){
                    //switch is on, set onesignal to on
                    OneSignal.setSubscription(true)
                    notificationSwitch.isChecked = true
                }else{
                    //switch is off, set onesignal to off
                    OneSignal.setSubscription(false)
                    notificationSwitch.isChecked = false
                }
            }else{
                notificationSwitch.isChecked = false
                //android notification permissions off, send them to turn it on!
                val alertDialog: AlertDialog? = activity?.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setPositiveButton("Take me there!",
                                DialogInterface.OnClickListener { dialog, id ->
                                    var intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity!!.getPackageName())).apply {
                                        addCategory(Intent.CATEGORY_DEFAULT)
                                        setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    activity!!.startActivity(intent)                                })
                        setNegativeButton("Cancel",
                                DialogInterface.OnClickListener { dialog, id ->
                                    // User cancelled the dialog
                                })
                    }
                    builder.setTitle("Turn on Notification Permissions")
                    builder.setMessage("Please turn on notification permissions in Settings -> Notifications")
                    builder.create()
                }
                alertDialog!!.show()

            }


        }


        //cant remember what this is for but must be important
        val SDK_INT = android.os.Build.VERSION.SDK_INT
        if (SDK_INT > 8) {
            val policy = StrictMode.ThreadPolicy.Builder()
                    .permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        //set profile image
        Glide.with(context!!.applicationContext)
                .load(R.drawable.savour_fullcolor)
                .into(profileImage)

        //get this user's fb profile image if they have one
        var ref = FirebaseDatabase.getInstance().getReference("Users").child(mAuth!!.uid!!).child("facebook_id")
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
//        mAuth.removeAuthStateListener(authStateListner)
    }

    companion object {
        fun newInstance(): AccountFragment = AccountFragment()
    }

    override fun onOSSubscriptionChanged(stateChanges: OSSubscriptionStateChanges?) {
        //,make sure button reflects state. For when user changes android permissions
        if (!stateChanges!!.getFrom().getSubscribed() && stateChanges!!.getTo().getSubscribed()) {
            //use subscribed to notifications, set switch to on
            notificationSwitch.isChecked = true
            // get player ID
            stateChanges.getTo().getUserId()
        }else{
            notificationSwitch.isChecked = false
        }
        Log.i("Debug", "onOSPermissionChanged: " + stateChanges);
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
            }
        }
    }

    fun vendorBtnOnclick(){
        val btnArr = arrayOf("Learn More", "Sign Up")
        val alertDialog: AlertDialog? = activity?.let {
            val builder = Builder(it)
            builder.setTitle("Want to become a vendor?")
                    .setItems(btnArr,
                            DialogInterface.OnClickListener { dialog, which ->
                                if (which == 0){
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.savourdeals.com/vendorsinfo/")))
                                }else{
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://savour-deals.firebaseapp.com/")))
                                }
                            })
            builder.apply {
                setNegativeButton("Cancel",
                        DialogInterface.OnClickListener { dialog, id ->
                            // User cancelled the dialog
                        })
            }

            // Set other dialog properties


            // Create the AlertDialog
            builder.create()
        }
        alertDialog!!.show()
    }

    fun logoutBtnOnclick(){
        LoginManager.getInstance().logOut()
        AuthUI.getInstance().signOut(this.context!!)
                .addOnCompleteListener {
                    var setupIntent =  Intent(this.context!!, LoginActivity::class.java)
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(setupIntent)
                    activity!!.finish()
                }
    }

    fun shareBtnOnclick(){
        var sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.setType("text/plain")
        val shareBody =  getString(R.string.invitation_message) + " " + getString(R.string.invitation_deep_link_kotlin)
        val shareSub = getString(R.string.invitation_title)
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSub)
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
        startActivityForResult(Intent.createChooser(sharingIntent, "Share using"),1)
    }


}
