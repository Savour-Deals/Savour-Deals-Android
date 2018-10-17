package com.CP.Savour

import android.content.Intent
import android.nfc.Tag
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import android.widget.*
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth


class ForgotPasswordActivity : AppCompatActivity() {
    private val TAG = "ForgotPasswordActivity"

    // UI Elements
    private var editTextEmail: EditText? = null
    private var buttonSubmit: Button? = null

    private var backgroundImg: ImageView? = null

    // Firebase references
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        backgroundImg = findViewById(R.id.imageView4)
        Glide.with(this)
                .load(R.drawable.stil336188)
                .into(backgroundImg!!)
        initialize()
    }

    private fun initialize() {
        editTextEmail = findViewById(R.id.et_email) as EditText
        buttonSubmit = findViewById(R.id.btn_submit) as Button

        mAuth = FirebaseAuth.getInstance()

        buttonSubmit!!.setOnClickListener { sendPasswordResetEmail() }
        makeTransparentStatusBar(true)

    }

    private fun makeTransparentStatusBar(isTransperant: Boolean) {
        if (isTransperant) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }
    }

    private fun sendPasswordResetEmail() {
        val email = editTextEmail?.text.toString()

        if (!TextUtils.isEmpty(email)) {
            mAuth!!
                    .sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val message = "Email sent."
                            Log.d(TAG,message)

                            Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
                            updateUI()
                        } else {
                            Toast.makeText(this, "Enter Email",Toast.LENGTH_SHORT).show()
                        }
                    }
        }

    }

    private fun updateUI() {
        val intent = Intent(this@ForgotPasswordActivity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}
