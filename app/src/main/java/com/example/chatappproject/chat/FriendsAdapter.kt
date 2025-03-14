package com.example.chatappproject.chat

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.chatappproject.R
import com.example.chatappproject.databinding.FriendItemBinding
import com.example.chatappproject.models.Friend

class FriendsAdapter(private var friendsList: List<Friend> ,private val onClick: (Friend) -> Unit) :
    RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(private val binding: FriendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: Friend) {
            println("In adapter ${friend.name}")
            println("In adapter ${friend.name}, Photo URL: ${friend.profilePhoto}")
            binding.friendName.text = friend.name
            val photoUrl = friend.profilePhoto

            val bitmap = photoUrl?.let { decodeBase64(it) }
            if (bitmap != null) {
                Glide.with(binding.root.context)
                    .load(bitmap)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(binding.profileImage)
            } else {
                binding.profileImage.setImageResource(R.drawable.default_profile)
            }

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

    fun decodeBase64(base64Str: String): Bitmap? {
        return try {
            val pureBase64Encoded = base64Str.substringAfter(",") // Remove prefix like "data:image/png;base64,"
            val decodedBytes = Base64.decode(pureBase64Encoded, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


}
