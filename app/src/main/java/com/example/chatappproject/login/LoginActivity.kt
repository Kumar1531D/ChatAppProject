package com.example.chatappproject.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.chatappproject.MainActivity
import com.example.chatappproject.api.RetrofitClient
import com.example.chatappproject.databinding.ActivityLoginBinding
import com.example.chatappproject.signup.SignupActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = LoginRequest(email, password)
            SessionManager.init(this)

            RetrofitClient.instance.login(request).enqueue(object : retrofit2.Callback<LoginResponse> {
                override fun onResponse(call: retrofit2.Call<LoginResponse>, response: retrofit2.Response<LoginResponse>) {
                    if (response.isSuccessful && response.body()?.access == "ok") {
                        val token = response.body()?.jwt
                        Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()

                        SessionManager.saveUserSession(token!!, email)

                        // Navigate to home screen
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login Failed: ${response.body()?.access}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        binding.signupText.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}