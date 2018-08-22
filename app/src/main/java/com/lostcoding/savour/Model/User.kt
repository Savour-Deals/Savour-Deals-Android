package com.lostcoding.savour.Model

class User {
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var email: String

    constructor(user: String, pass: String, mail: String ) {
        this.username = user
        this.password = pass
        this.email = mail
    }
}