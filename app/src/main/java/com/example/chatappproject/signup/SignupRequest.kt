package com.example.chatappproject.signup

data class SignupRequest(
    val userName: String,
    val email: String,
    val password: String
)

data class SignupResponse(
    val access: String
)
