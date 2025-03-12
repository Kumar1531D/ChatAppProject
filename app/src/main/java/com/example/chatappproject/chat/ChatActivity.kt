package com.example.chatappproject.chat

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatappproject.R
import com.example.chatappproject.api.RetrofitClient
import com.example.chatappproject.databinding.ActivityChatBinding
import com.example.chatappproject.login.LoginResponse
import com.example.chatappproject.login.SessionManager
import com.example.chatappproject.models.InsertGroupMsg
import com.example.chatappproject.models.InsertMsg
import com.example.chatappproject.models.MessagesResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocketListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
//    private var ws: okhttp3.WebSocket? = null
    private lateinit var friendName: String
    private lateinit var userName: String
    private var groupId : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the selected friend's username from intent
        friendName = intent.getStringExtra("friendName") ?: ""
        userName = SessionManager.getLoggedInUser().toString()

        //Get the selected group's name from intent
        groupId = intent.getIntExtra("groupId",0)

        // Register the BroadcastReceiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            messageReceiver,
            IntentFilter("NEW_MESSAGE")
        )
        LocalBroadcastManager.getInstance(this).registerReceiver(
            messageReceiver,
            IntentFilter("NEW_GROUP_MESSAGE")
        )

        val linearlayout = LinearLayoutManager(this)
        linearlayout.stackFromEnd = true

        binding.recyclerChat.layoutManager = linearlayout
        chatAdapter = ChatAdapter(mutableListOf(),userName)
        binding.recyclerChat.adapter = chatAdapter

        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.messageInput.text.clear() // Clear the input field
            }
        }

        displayChatHistory()

//        connectWebSocket()
    }

    private val messageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "NEW_MESSAGE") {
                val sender = intent.getStringExtra("sender").toString()
                val message = intent.getStringExtra("message").toString()

                if (sender == friendName) {
                    chatAdapter.addMessage(sender, message)
                    binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
                }
            } else if (intent?.action == "NEW_GROUP_MESSAGE") {
                val groupId = intent.getIntExtra("groupId", 0)
                val sender = intent.getStringExtra("sender").toString()
                val message = intent.getStringExtra("message").toString()

                if (groupId == this@ChatActivity.groupId && sender != userName) {
                    chatAdapter.addMessage(sender, "$sender : $message")
                    binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
                }
            }
        }
    }

    private fun sendMessage(message: String) {
        val intent = Intent("SEND_MESSAGE")
        if (groupId != 0) {
            // Group message
            intent.putExtra("type", "group")
            intent.putExtra("groupId", groupId)

            //InsertGroupMsg in the DB
            RetrofitClient.instance.insertGroupMessages("insertGroupMsg",InsertGroupMsg(message, userName, groupId))
                    .enqueue(object : retrofit2.Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                // Request was successful
                                println("Message inserted successfully")
                            } else {
                                println("Error: ${response.code()} - ${response.message()}")
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            println("API call failed: ${t.message}")
                        }
                    })
        } else {
            // Private message
            intent.putExtra("type", "private")
            intent.putExtra("receiver", friendName)

            // InsertMsg in the DB
            RetrofitClient.instance.insertMessages("insertMsg",InsertMsg(message, userName, friendName))
                    .enqueue(object : retrofit2.Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                // Request was successful
                                println("Message inserted successfully")
                            } else {
                                println("Error: ${response.code()} - ${response.message()}")
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            println("API call failed: ${t.message}")
                        }
                    })
        }
        intent.putExtra("sender", userName)
        intent.putExtra("message", message)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        // Add the message to the local chat adapter
        if (groupId != 0) {
            chatAdapter.addMessage(userName, "$userName : $message")
        } else {
            chatAdapter.addMessage(userName, message)
        }
        binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
    }

    private fun displayChatHistory() {
        if(groupId!=0){
            RetrofitClient.instance.getGroupMessages("getGroupMessages",groupId).enqueue(object : retrofit2.Callback<List<MessagesResponse>> {
                override fun onResponse(
                    call: Call<List<MessagesResponse>>,
                    response: Response<List<MessagesResponse>>
                ) {
                    if (response.body() == null) {
                        Toast.makeText(this@ChatActivity, "No messages found", Toast.LENGTH_SHORT).show()
                        return
                    }

                    for(m in response.body()!!){
                        println(" In displayChatHistory "+m.sender_name+" "+m.message)
                        if(m.sender_name==userName)
                            chatAdapter.addMessage(m.sender_name,m.message)
                        else
                            chatAdapter.addMessage(m.sender_name,m.sender_name+" : "+m.message)
                    }
                }

                override fun onFailure(call: Call<List<MessagesResponse>>, t: Throwable) {
                    Toast.makeText(this@ChatActivity,"Error displaying chat history :( ",Toast.LENGTH_SHORT).show()
                }

            })
        }
        else{
            RetrofitClient.instance.getMessages("loadMessages",userName,friendName).enqueue(object : retrofit2.Callback<List<MessagesResponse>> {
                override fun onResponse(
                    call: Call<List<MessagesResponse>>,
                    response: Response<List<MessagesResponse>>
                ) {
                    if (response.body() == null) {
                        Toast.makeText(this@ChatActivity, "No messages found", Toast.LENGTH_SHORT).show()
                        return
                    }

                    for(m in response.body()!!){
                        chatAdapter.addMessage(m.sender_name,m.message)
                    }
                }

                override fun onFailure(call: Call<List<MessagesResponse>>, t: Throwable) {
                    Toast.makeText(this@ChatActivity,"Error displaying chat history :( ",Toast.LENGTH_SHORT).show()
                }

            })

        }
    }

//    private fun connectWebSocket() {
//        val token = SessionManager.getSessionToken() ?: return
//        val request = Request.Builder().url("ws://192.168.1.6:8002/ChatApp/chat/$token").build()
//        val client = OkHttpClient()
//
//        ws = client.newWebSocket(request, object : WebSocketListener() {
//            override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
//                runOnUiThread {
//                    val messageData = JSONObject(text)
//                    if (messageData.getString("type") == "private" ){
//                        if(messageData.getString("sender") == friendName || messageData.getString("receiver") == friendName) {
//                            chatAdapter.addMessage(
//                                messageData.getString("sender"),
//                                messageData.getString("message")
//                            )
//                            binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
//                        }
//                    }
//                    else if(messageData.getString("type")=="group"){
//                        if(messageData.getInt("groupId")==groupId && messageData.getString("sender") != userName) {
//                            chatAdapter.addMessage(
//                                messageData.getString("sender"),
//                                messageData.getString("sender") + " : " + messageData.getString("message")
//                            )
//                            binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
//                        }
//                    }
//                }
//            }
//        })
//    }
//
//    private fun sendMessage() {
//        println("In sendMessage")
//        val messageText = binding.messageInput.text.toString().trim()
//        if (messageText.isNotEmpty() && ws != null) {
//
//            if (ws == null) { // ✅ Fix: Prevent crashes if ws is null
//                Toast.makeText(this, "WebSocket not connected", Toast.LENGTH_SHORT).show()
//                return
//            }
//
//            if(groupId!=0){
//                println("In group")
//                val messageJson = JSONObject().apply {
//                    put("type", "group")
//                    put("sender", userName)
//                    put("groupId", groupId)
//                    put("message", messageText)
//                }
//                ws!!.send(messageJson.toString())
//                chatAdapter.addMessage(userName, messageText)
//                binding.messageInput.setText("")
//                RetrofitClient.instance.insertGroupMessages("insertGroupMsg",InsertGroupMsg(messageText, userName, groupId))
//                    .enqueue(object : retrofit2.Callback<Void> {
//                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                            if (response.isSuccessful) {
//                                // Request was successful
//                                println("Message inserted successfully")
//                            } else {
//                                println("Error: ${response.code()} - ${response.message()}")
//                            }
//                        }
//
//                        override fun onFailure(call: Call<Void>, t: Throwable) {
//                            println("API call failed: ${t.message}")
//                        }
//                    })
//            }
//            else {
//                println("In private")
//                val messageJson = JSONObject().apply {
//                    put("type", "private")
//                    put("sender", userName)
//                    put("receiver", friendName)
//                    put("m", messageText)
//                }
//                ws!!.send(messageJson.toString())
//                chatAdapter.addMessage(userName, messageText)
//                binding.messageInput.setText("")
//                RetrofitClient.instance.insertMessages("insertMsg",InsertMsg(messageText, userName, friendName))
//                    .enqueue(object : retrofit2.Callback<Void> {
//                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
//                            if (response.isSuccessful) {
//                                // Request was successful
//                                println("Message inserted successfully")
//                            } else {
//                                println("Error: ${response.code()} - ${response.message()}")
//                            }
//                        }
//
//                        override fun onFailure(call: Call<Void>, t: Throwable) {
//                            println("API call failed: ${t.message}")
//                        }
//                    })
//            }
//
//            binding.recyclerChat.scrollToPosition(chatAdapter.itemCount-1)
//            scrollToBottom()
//        }
//    }

//    private fun showNotification(sender: String, message: String) {
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                "chat_notifications",
//                "Chat Notifications",
//                NotificationManager.IMPORTANCE_HIGH
//            )
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        val notificationBuilder = NotificationCompat.Builder(this, "chat_notifications")
//            .setSmallIcon(R.drawable.ic_message)
//            .setContentTitle("New message from $sender")
//            .setContentText(message)
//            .setStyle(NotificationCompat.BigTextStyle().bigText(message)) // ✅ Show full message
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setAutoCancel(true)
//
//        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
//    }



    private fun scrollToBottom() {
        val layoutManager = binding.recyclerChat.layoutManager as LinearLayoutManager
        val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()

        if (lastVisibleItem == chatAdapter.itemCount - 2) { // User is at the bottom
            binding.recyclerChat.scrollToPosition(chatAdapter.itemCount - 1)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        ws?.close(1000, "Activity Destroyed")
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver)
    }
}