package com.CP.Savour

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import android.view.WindowManager
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton

import java.util.*
import android.widget.Toast
import com.google.firebase.auth.FacebookAuthProvider
import com.facebook.AccessToken
import com.bumptech.glide.Glide
import com.facebook.login.LoginManager
import com.google.firebase.auth.UserInfo




class LoginActivity : AppCompatActivity() {



    private val TAG = "LoginActivity"
    private final  val EMAIL = "email"

    private var email: String? = null
    private var password: String? = null

    private var loading: ProgressBar? = null



    // UI Elements
    private var textViewForgotPassword: TextView? = null
    private var textViewCreateAccount: TextView? = null
    private var editTextEmail: EditText? = null
    private var editTextPassword: EditText? = null
    private var buttonLogin: Button? = null
    private lateinit var facebookButton: LoginButton
    private lateinit var callbackManager: CallbackManager
    var progressBarHolder: FrameLayout? = null
    private var backgroundImg: ImageView? = null
    private var logoImg: ImageView? = null


    // Firebase references
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        progressBarHolder = findViewById(R.id.progress_overlay) as FrameLayout
        backgroundImg = findViewById(R.id.savour_logo)
        logoImg = findViewById(R.id.imageView2)
        Glide.with(this)
                .load(R.drawable.jaywennington2065)
                .into(backgroundImg!!)
        Glide.with(this)
                .load(R.drawable.savour_white)
                .into(logoImg!!)

        initialize()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun initialize() {
        textViewForgotPassword = findViewById(R.id.tv_forgot_password) as TextView
        textViewCreateAccount = findViewById(R.id.tv_register_account) as TextView
        editTextEmail = findViewById(R.id.et_email) as EditText
        editTextPassword = findViewById(R.id.et_password) as EditText
        buttonLogin = findViewById(R.id.btn_login) as Button
        facebookButton = findViewById(R.id.facebook_login) as LoginButton

        callbackManager = CallbackManager.Factory.create()

        facebookButton.setReadPermissions(Arrays.asList(EMAIL))


        facebookButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                println("OMG FACEBOOK LOGIN!!!!")
                progressBarHolder!!.visibility = View.VISIBLE


                handleFacebookAccessToken(result!!.getAccessToken())
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException?) {
                println(error)
            }

        })


        makeTransparentStatusBar(true)

        mAuth = FirebaseAuth.getInstance()

        // sending the user to the login activity whenever the textview for the forgot password is pressed
        textViewForgotPassword!!.setOnClickListener { startActivity(Intent (this@LoginActivity,ForgotPasswordActivity::class.java)) }

        textViewCreateAccount!!.setOnClickListener { startActivity(Intent (this@LoginActivity, CreateAccountActivity::class.java))}

        buttonLogin!!.setOnClickListener { loginUser() }


        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            updateUI()
        }else{
            FirebaseAuth.getInstance().signOut()
            LoginManager.getInstance().logOut()
            Log.d(TAG, "onAuthStateChanged:signed_out")
        }
    }

    private fun makeTransparentStatusBar(isTransperant: Boolean) {
        if (isTransperant) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }



    private fun loginUser() {
        val email = editTextEmail?.text.toString()
        val password = editTextPassword?.text.toString()
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            Log.d(TAG, "Logging in user.")

            mAuth!!.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(this) { task ->

                        if(task.isSuccessful) {
                            // Sign in success, update UI with signed-in user's information
                            Log.d(TAG, "signInWithEmail:success")
                            updateUI()
                        } else {
                            Log.e(TAG,"signInWithEmail:failure",task.exception)
                            Toast.makeText(this@LoginActivity,"Authentication failed.",Toast.LENGTH_SHORT).show()
                        }
                    }
        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUI() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if(task.isSuccessful) {
                        // Sign in success, update UI with signed-in user's information
                        Log.d(TAG, "signInWithFacebook:success")
                        updateUI()
                        progressBarHolder!!.visibility = View.INVISIBLE

                    } else {
                        Log.e(TAG,"signInWithFacebook:failure",task.exception)
                        Toast.makeText(this@LoginActivity,"Authentication failed.",Toast.LENGTH_SHORT).show()
                        progressBarHolder!!.visibility = View.INVISIBLE
                    }
                }

    }

}
