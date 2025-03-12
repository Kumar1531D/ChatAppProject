package com.example.chatappproject.models

import retrofit2.http.Query

data class Message(
    val id: Int,
    val sender_id: Int,
    val sender_name: String,
    val message: String,
    val timestamp: String
)

data class MessagesResponse(
    val sender_name : String,
    val message: String
)

data class InsertMsg(
    val msg : String,
    val sender : String,
    val receiver : String
)

data class InsertGroupMsg(
    val msg : String,
    val sender : String,
    val groupId : Int
)

data class InsertFriend(
    val name : String,
    val friend : String
)

data class InsertGroup(
    val gName : String,
    val creator : String
)
