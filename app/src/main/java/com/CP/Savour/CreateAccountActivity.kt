package com.CP.Savour

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
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
import com.google.firebase.auth.FacebookAuthProvider
import com.facebook.AccessToken
import com.bumptech.glide.Glide
import com.facebook.login.LoginBehavior
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateAccountActivity : AppCompatActivity() {
    /**
     *  UI Elements
     */
    private var editTextFullName: EditText? = null
    private var editTextEmail: EditText? = null
    private var editTextPassword: EditText? = null
    private var buttonCreateAccount: Button? = null

    //Firebase references
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    private lateinit var facebookButton: LoginButton
    private lateinit var callbackManager: CallbackManager
    private lateinit var progressBarHolder: FrameLayout

    private val TAG = "CreateAccountActivity"

    //global variables
    private var fullName: String? = null
    private var email: String? = null
    private var password: String? = null

    private var logoImg: ImageView? = null
    private var backgroundImg: ImageView? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        progressBarHolder = findViewById(R.id.progress_overlay) as FrameLayout
        logoImg = findViewById(R.id.imageView)
        backgroundImg = findViewById(R.id.backgroundImg)
        Glide.with(this)
                .load(R.drawable.stil336188)
                .into(backgroundImg!!)
        Glide.with(this)
                .load(R.drawable.savour_white)
                .into(logoImg!!)
        // initializing references...
        initialize()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null){
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun initialize() {

        // retrieving the views from the layout
        editTextFullName = findViewById(R.id.et_full_name) as EditText
        editTextEmail = findViewById(R.id.et_email) as EditText
        editTextPassword = findViewById(R.id.et_password) as EditText
        buttonCreateAccount = findViewById(R.id.btn_register) as Button
        val privacyButton = findViewById(R.id.privacy_button) as Button
        val termsButton = findViewById(R.id.terms_button) as Button
        facebookButton = findViewById(R.id.facebook_register) as LoginButton

        makeTransparentStatusBar(true)

        // retrieving the data base reference
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Users")
        mAuth = FirebaseAuth.getInstance()

        privacyButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.savourdeals.com/privacy-policy/")))
        }

        termsButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.savourdeals.com/terms-of-use/")))
        }

        callbackManager = CallbackManager.Factory.create()
        facebookButton.setReadPermissions("public_profile", "email", "user_friends")
        facebookButton.setLoginBehavior(LoginBehavior.WEB_ONLY)
        facebookButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult?) {
                println("OMG FACEBOOK LOGIN!!!!")
                progressBarHolder.visibility = View.VISIBLE
                handleFacebookAccessToken(result!!.getAccessToken())
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
            }

            override fun onError(error: FacebookException?) {
                println(error)
            }

        })

        // registering the click listen to the button
        // when the button is pressed, it will call the custom method createNewAccount()
        buttonCreateAccount!!.setOnClickListener { createNewAccount() }
    }

    private fun makeTransparentStatusBar(isTransperant: Boolean) {
        if (isTransperant) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    private fun createNewAccount() {
        // retrieving the text from the edit text boxes
        fullName = editTextFullName?.text.toString()
        email = editTextEmail?.text.toString()
        password = editTextPassword?.text.toString()

        // validating the register form...
        if (!TextUtils.isEmpty(fullName)  && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            mAuth!!
                    .createUserWithEmailAndPassword(email!!,password!!)
                    .addOnCompleteListener(this) { task ->
                        progressBarHolder.visibility = View.VISIBLE
                        if (task.isSuccessful) {
                            // sign in success, update UI with the signed-in user's information
                            Log.d(TAG,"createUserWithEmail:success")

                            val userId = mAuth!!.currentUser!!.uid

                            //  verify email
                            verifyEmail()

                            // update user profile information
                            val currentUserDb = mDatabaseReference!!.child(userId)
                            currentUserDb.child("full_name").setValue(fullName)

                            //we dont want to login yet!
//                            updateUserInfoAndUI()
                        } else {
                            // if log in fails, display a message to the user
                            Log.w(TAG, "createUserWithEmail:failure",task.exception)
                            messagePopup( task.exception!!.localizedMessage,"User creation failed!")
                        }
                        progressBarHolder.visibility = View.INVISIBLE
                    }
        } else {
            messagePopup("Please enter all details ","Information Missing!")
            progressBarHolder.visibility = View.INVISIBLE
        }
    }

    private fun verifyEmail() {
        val mUser = mAuth!!.currentUser
        mUser!!.sendEmailVerification()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        messagePopup("Verification email sent to " + mUser.getEmail() + ". Please check your email to verify your account. Then come back to login!","Verify Email")
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.exception)
                        messagePopup("Please try again. If this problem persists, contact us.","Failed to send verification email.")
                    }
                }
    }

    private fun updateUserInfoAndUI() {
        //start next activity
        val intent = Intent(this@CreateAccountActivity, MainActivity::class.java)
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
                        updateUserInfoAndUI()
//                        progressBarHolder!!.visibility = View.INVISIBLE
                    } else {
                        Log.e(TAG,"signInWithFacebook:failure",task.exception)
                        messagePopup("Could not sign in with Facebook. Please try again.","Authentication failed.")
                        progressBarHolder.visibility = View.INVISIBLE
                    }
                }

    }

    fun messagePopup(message: String, title: String){
        val alertDialog: AlertDialog? = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("Ok",null)
            }
            builder?.setMessage(message)
                    .setTitle(title)
            builder.create()
        }
        alertDialog!!.show()
    }
}
