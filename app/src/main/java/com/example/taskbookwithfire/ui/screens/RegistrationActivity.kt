package com.example.taskbookwithfire.ui.screens

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.taskbookwithfire.R
import com.google.firebase.auth.FirebaseAuth

class RegistrationActivity : AppCompatActivity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var redirectToLogin: TextView

    private lateinit var auth: FirebaseAuth

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        redirectToLogin  = findViewById(R.id.redirect_login)
        redirectToLogin.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        emailEditText = findViewById(R.id.email_edittext)
        passwordEditText =  findViewById(R.id.signup_password)
        passwordEditText.inputType = EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        passwordEditText.transformationMethod = PasswordTransformationMethod.getInstance()
        registerButton = findViewById(R.id.signup_button)

        registerButton.setOnClickListener {
            register()
        }
    }

    private fun register() {

        val email = emailEditText.getText().toString()
        val password = passwordEditText.getText().toString()
        Log.d("TAG", "$password $email")

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this,
                "Please, fill in all the required fields",
                Toast.LENGTH_LONG
                ).show()

            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                    if (it.isSuccessful) {
                        Toast.makeText(this,
                            "Registration was successful",
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)

                    } else {
                        Toast.makeText(
                            this,
                            "Registration failed, please try again later",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
    }
}