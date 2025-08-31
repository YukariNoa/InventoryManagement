package com.example.inventory_management

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.EditText
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent

import android.widget.TextView


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val login = findViewById<Button>(R.id.LoginButton)
        login.setOnClickListener {
            Log.d(TAG, "Login button clicked")
            val usernameEditText = findViewById<EditText>(R.id.Username)
            val passwordEditText = findViewById<EditText>(R.id.Password)

            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            val db = FirebaseFirestore.getInstance()
            db.collection("users")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val databaseUsername = document.data["username"]
                        val databasePassword = document.data["password"]
                        Log.d(TAG, "Testing: ${databaseUsername} => ${databasePassword}")
                        if (databaseUsername == username && databasePassword == password) {
                            Log.d(TAG, "Admin user logged in: ${document.id}")
                            if(document.data["role"] == "admin"){
                                Log.d(TAG, "Admin user logged in: $username")
                                val intent = Intent(this, Homepage::class.java)
                                intent.putExtra("role", "admin")
                                startActivity(intent)
                                finish()
                                // Handle admin login logic here
                            } else {
                                Log.d(TAG, "Regular user logged in: $username")
                                // Handle regular user login logic here
                            }
                        } else {
                            Log.d(TAG, "Authentication failed for user: $username")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
                }

            val greeting = greetUser(username)

            val loginMessageTextView = findViewById<TextView>(R.id.LoginMessage)
            loginMessageTextView.text = greeting
            // Handle login logic here
        }

    }
}