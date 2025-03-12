package com.example.chatappproject.login

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private lateinit var sharedPreferences: SharedPreferences

    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
    }

    fun saveUserSession(token: String, userName: String) {
        with(sharedPreferences.edit()) {
            putString("session_$token", userName) // Store username per session
            putString("currentSession", token) // Track active session
            apply()
        }
    }

    fun getLoggedInUser(): String? {
        val currentSession = sharedPreferences.getString("currentSession", null)
        return sharedPreferences.getString("session_$currentSession", null)
    }

    fun getSessionToken(): String? {
        return sharedPreferences.getString("currentSession", null)
    }

    fun logout() {
        sharedPreferences.edit().remove("currentSession").apply()
    }
}

