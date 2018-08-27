package com.CP.Savour

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateAccountActivity : AppCompatActivity() {
    /**
     *  UI Elements
     */
    private var editTextFirstName: EditText? = null
    private var editTextLastName: EditText? = null
    private var editTextEmail: EditText? = null
    private var editTextPassword: EditText? = null
    private var buttonCreateAccount: Button? = null
    private var progressBar: ProgressBar? = null

    //Firebase references
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null

    private val TAG = "CreateAccountActivity"

    //global variables
    private var firstName: String? = null
    private var lastName: String? = null
    private var email: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        // initializing references...
        initialize()
    }

    /**
     * This method retrieves the views from the layout, and creates the progress bar
     * the method also retrieves an instance of the firebase database and retrieves a reference
     * to the Users database location
     */
    private fun initialize() {

        // retrieving the views from the layout
        editTextFirstName = findViewById(R.id.et_first_name) as EditText
        editTextLastName = findViewById(R.id.et_last_name) as EditText
        editTextEmail = findViewById(R.id.et_email) as EditText
        editTextPassword = findViewById(R.id.et_password) as EditText
        buttonCreateAccount = findViewById(R.id.btn_register) as Button
        progressBar = findViewById(R.id.signup_progress) as ProgressBar

        // retrieving the data base reference
        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference!!.child("Users")
        mAuth = FirebaseAuth.getInstance()

        // registering the click listen to the button
        // when the button is pressed, it will call the custom method createNewAccount()
        buttonCreateAccount!!.setOnClickListener { createNewAccount() }
    }

    /**
     * This method is used as a callback for the button to register a new user
     */
    private fun createNewAccount() {

        // retrieving the text from the edit text boxes
        firstName = editTextFirstName?.text.toString()
        lastName = editTextLastName?.text.toString()
        email = editTextEmail?.text.toString()
        password = editTextPassword?.text.toString()

        // validating the register form...
        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            // TODO: Create a working progress bar.

            mAuth!!
                    .createUserWithEmailAndPassword(email!!,password!!)
                    .addOnCompleteListener(this) { task ->
                        progressBar!!.visibility = View.VISIBLE
                        if (task.isSuccessful) {
                            // sign in success, update UI with the signed-in user's information
                            Log.d(TAG,"createUserWithEmail:success")

                            val userId = mAuth!!.currentUser!!.uid

                            //  verify email
                            verifyEmail()

                            // update user profile information
                            val currentUserDb = mDatabaseReference!!.child(userId)
                            currentUserDb.child("full_name").setValue(firstName + " " + lastName)

                            updateUserInfoAndUI()
                        } else {
                            // if log in fails, display a message to the user
                            Log.w(TAG, "createUserWithEmail:failure",task.exception)
                            Toast.makeText(this@CreateAccountActivity, "Authentication Failed.", Toast.LENGTH_SHORT).show()
                        }
                    }
        } else {
            Toast.makeText(this, "Enter all details please", Toast.LENGTH_SHORT).show()
        }
        progressBar!!.visibility = View.GONE
    }

    private fun verifyEmail() {
        val mUser = mAuth!!.currentUser;
        mUser!!.sendEmailVerification()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this@CreateAccountActivity,
                                "Verification email sent to " + mUser.getEmail(),
                                Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.exception)
                        Toast.makeText(this@CreateAccountActivity,
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show()
                    }
                }
    }

    private fun updateUserInfoAndUI() {
        //start next activity
        val intent = Intent(this@CreateAccountActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}
