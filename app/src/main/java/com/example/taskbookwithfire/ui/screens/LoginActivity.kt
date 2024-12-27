package com.example.taskbookwithfire.ui.screens

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taskbookwithfire.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity: AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var redirectToRegistration: TextView
    private lateinit var menuIcon: ImageButton

    private lateinit var auth: FirebaseAuth

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        redirectToRegistration = findViewById(R.id.redirect_login)
        redirectToRegistration.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }

        menuIcon = findViewById(R.id.menu_icon)
        menuIcon.visibility = View.GONE

            emailEditText = findViewById(R.id.email_edittext)
        passwordEditText = findViewById(R.id.signup_password)
        passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()

        loginButton = findViewById(R.id.login_button)
        loginButton.setOnClickListener {
            loginUser();
        }
    }

    private fun loginUser() {
        val email = emailEditText.getText().toString().trim()
        Log.d("TAG", "email $email")
        val password = passwordEditText.getText().toString()

        if (TextUtils.isEmpty(email)  || TextUtils.isEmpty(password)) {
            Toast.makeText(
                applicationContext,
                "Fill in all three fields",
                Toast.LENGTH_LONG
            ).show()
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Login was successful",
                        Toast.LENGTH_LONG
                    ).show()

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)

                } else {
                    Toast.makeText(
                        applicationContext,
                        "Registration failed, please try again later",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

}