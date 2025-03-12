package com.example.chatappproject.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatappproject.R
import com.example.chatappproject.databinding.ItemChatMessageBinding

class ChatAdapter(private val messages: MutableList<Pair<String, String>>, private val currentUser: String) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        println("onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        println("onBindViewHolder")
        val (sender, message) = messages[position]
        holder.bind(sender, message, currentUser)
    }

    override fun getItemCount() : Int = messages.size

    fun addMessage(sender: String, message: String) {
        println("in addMessage")
        messages.add(Pair(sender, message))
        notifyItemInserted(messages.size - 1)
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val leftMessageLayout: View = itemView.findViewById(R.id.leftMessageLayout)
        private val rightMessageLayout: View = itemView.findViewById(R.id.rightMessageLayout)
        private val leftMessageText: TextView = itemView.findViewById(R.id.leftMessageText)
        private val rightMessageText: TextView = itemView.findViewById(R.id.rightMessageText)

        fun bind(sender: String, message: String, currentUser: String) {
            println("sender $sender currentUser $currentUser")
            if (sender == currentUser) {
                println("In right")
                rightMessageLayout.visibility = View.VISIBLE
                leftMessageLayout.visibility = View.GONE
                rightMessageText.text = message
            } else {
                println("in left")
                leftMessageLayout.visibility = View.VISIBLE
                rightMessageLayout.visibility = View.GONE
                leftMessageText.text = message
            }
        }
    }
}
