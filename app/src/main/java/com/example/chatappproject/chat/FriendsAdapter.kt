package com.example.chatappproject.chat

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappproject.databinding.FriendItemBinding
import com.example.chatappproject.models.Friend

class FriendsAdapter(private var friendsList: List<Friend> ,private val onClick: (Friend) -> Unit) :
    RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(private val binding: FriendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: Friend) {
            println("In adapter ${friend.name}")
            binding.friendName.text = friend.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        println(" In onCreateViewHolder")
        val binding = FriendItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendsList[position]
        holder.bind(friend)
        holder.itemView.setOnClickListener { onClick(friend) }
    }

    override fun getItemCount() = friendsList.size

    fun updateData(newFriendsList: List<Friend>) {
        println(" In updateData")
        friendsList = newFriendsList
        notifyDataSetChanged() // Refresh RecyclerView
    }
}
