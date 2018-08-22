package com.lostcoding.savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.facebook.internal.Utility.arrayList
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.*

class LoginActivity : AppCompatActivity() {
    private final var RC_SIGN_IN = 123

    private lateinit var  mAuth: FirebaseAuth
    // choose authentication providers
    var providers: List<AuthUI.IdpConfig> = arrayList(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build()
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        var facebooksign = AuthUI.IdpConfig.FacebookBuilder().build()
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()
        var currentUser: FirebaseUser = mAuth.getCurrentUser()!!

    }

    fun createAccount(email: String, password: String) {

    }
}
