package com.lostcoding.savour

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
class RegisterActivity : AppCompatActivity() {
    // firebase database instantiation
    //private lateinit var database: FirebaseDatabase
    //private lateinit var users: DatabaseReference

    //private lateinit var editUsername: EditText
    //private lateinit var editPassword: EditText
    //private lateinit var editMail: EditText

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        mAuth = FirebaseAuth.getInstance()

        // grabbing the firebase instance
        //database = FirebaseDatabase.getInstance()
        //users = database.getReference("Users")

    }

    override fun onStart() {
        super.onStart()

        // check if the user is signed in (non-null) and update UI accordingly
        var currentUser: FirebaseUser = mAuth.currentUser!!
    }

    fun createAccount(email: String, password: String) {

    }
}
