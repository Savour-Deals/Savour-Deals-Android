package com.CP.Savour

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val TAG = "LoginActivity"
    
    private var email: String? = null
    private var password: String? = null

    // UI Elements
    private var textViewForgotPassword: TextView? = null
    private var textViewCreateAccount: TextView? = null
    private var editTextEmail: EditText? = null
    private var editTextPassword: EditText? = null
    private var buttonLogin: Button? = null
    private var progressBar: ProgressBar? = null
    private var constraintView: View? = null

    // Firebase references
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        initialize()
    }

    private fun initialize() {
        textViewForgotPassword = findViewById(R.id.tv_forgot_password) as TextView
        textViewCreateAccount = findViewById(R.id.tv_register_account) as TextView
        editTextEmail = findViewById(R.id.et_email) as EditText
        editTextPassword = findViewById(R.id.et_password) as EditText
        buttonLogin = findViewById(R.id.btn_login) as Button
        progressBar!!.visibility = View.GONE
        mAuth = FirebaseAuth.getInstance()

        // sending the user to the login activity whenever the textview for the forgot password is pressed
        textViewForgotPassword!!.setOnClickListener { startActivity(Intent (this@LoginActivity,ForgotPasswordActivity::class.java)) }

        textViewCreateAccount!!.setOnClickListener { startActivity(Intent (this@LoginActivity, CreateAccountActivity::class.java))}

        buttonLogin!!.setOnClickListener { loginUser() }
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
}
