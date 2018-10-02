package com.CP.Savour

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.fragment_account.view.*


class AccountFragment : Fragment() {

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

    companion object {
        fun newInstance(): AccountFragment = AccountFragment()
    }




}
