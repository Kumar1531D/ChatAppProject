package com.example.chatappproject.login

data class LoginRequest (
    val userName: String,
    val password: String)

data class LoginResponse(
    val access: String,
    val message: String,
    val jwt: String? // JWT token if login is successful
)

