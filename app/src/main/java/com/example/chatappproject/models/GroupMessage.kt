package com.example.chatappproject.models

data class GroupMessage(
    val id: Int,
    val group_id: Int,
    val group_name: String,
    val sender_name: String,
    val message: String,
    val timestamp: String
)

data class GroupMessagesResponse(
    val messages: List<GroupMessage>
)
