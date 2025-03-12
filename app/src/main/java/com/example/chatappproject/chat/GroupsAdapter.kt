package com.example.chatappproject.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappproject.databinding.GroupItemBinding
import com.example.chatappproject.models.Friend
import com.example.chatappproject.models.Group

class GroupsAdapter(private var groupsList: List<Group>,private val onClick: (Group) -> Unit) :
    RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(private val binding: GroupItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(group: Group) {
            binding.groupName.text = group.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val binding = GroupItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groupsList[position]
        holder.bind(group)
        holder.itemView.setOnClickListener{onClick(group)}
    }

    override fun getItemCount() = groupsList.size

    fun updateData(newGroupList: List<Group>) {
        groupsList = newGroupList
        notifyDataSetChanged()
    }
}
