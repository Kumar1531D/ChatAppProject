package com.example.chatappproject.models

data class Friend(
    val name: String,
    val profilePhoto : String?,
    val mimeType: String?
)

data class FriendsResponse(
    val friends: List<Friend>
)
