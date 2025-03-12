package com.example.chatappproject.signup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatappproject.api.RetrofitClient
import com.example.chatappproject.databinding.ActivitySignupBinding
import com.example.chatappproject.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signupButton.setOnClickListener {
            val name = binding.nameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val request = SignupRequest(name, email, password)

            RetrofitClient.instance.signup(request).enqueue(object : Callback<SignupResponse> {
                override fun onResponse(call: Call<SignupResponse>, response: Response<SignupResponse>) {
                    if (response.isSuccessful && response.body()?.access == "ok") {
                        Toast.makeText(this@SignupActivity, "Signup Successful!", Toast.LENGTH_SHORT).show()

                        // Redirect to LoginActivity
                        val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@SignupActivity, "Signup Failed :(  userName already exist!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<SignupResponse>, t: Throwable) {
                    Toast.makeText(this@SignupActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        // Redirect to login page
        binding.loginText.setOnClickListener {
            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}