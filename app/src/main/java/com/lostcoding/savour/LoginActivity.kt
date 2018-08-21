package com.lostcoding.savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.facebook.internal.Utility.arrayList
import com.firebase.ui.auth.AuthUI
import java.util.*

class LoginActivity : AppCompatActivity() {
    private final var RC_SIGN_IN = 123
    // choose authentication providers
    var providers: List<AuthUI.IdpConfig> = arrayList(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build()
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        var facebooksign = AuthUI.IdpConfig.FacebookBuilder().build()
    }
}
