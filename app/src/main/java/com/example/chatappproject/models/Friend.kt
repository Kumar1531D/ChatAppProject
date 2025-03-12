package com.example.chatappproject.models

data class Friend(
    val name: String
)

data class FriendsResponse(
    val friends: List<Friend>
)
