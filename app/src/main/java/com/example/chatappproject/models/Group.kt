package com.example.chatappproject.models

data class Group(
    val id: Int,
    val name: String,
    val creator: String
)

data class GroupsResponse(
    val groups: List<Group>
)
