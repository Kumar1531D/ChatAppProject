package com.example.chatappproject

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatappproject.api.RetrofitClient
import com.example.chatappproject.chat.ChatActivity
import com.example.chatappproject.chat.FriendsAdapter
import com.example.chatappproject.chat.GroupsAdapter
import com.example.chatappproject.databinding.ActivityMainBinding
import com.example.chatappproject.login.LoginActivity
import com.example.chatappproject.login.SessionManager
import com.example.chatappproject.models.Friend
import com.example.chatappproject.models.Group
import com.example.chatappproject.models.InsertFriend
import com.example.chatappproject.models.InsertGroup
import com.example.chatappproject.websocket.WebSocketService
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocketListener
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var friendsAdapter: FriendsAdapter
    private lateinit var groupsAdapter: GroupsAdapter
    private var ws: okhttp3.WebSocket? = null

    private val sendMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "SEND_MESSAGE") {
                val type = intent.getStringExtra("type")
                val sender = intent.getStringExtra("sender")
                val message = intent.getStringExtra("message")

                val jsonMessage = JSONObject().apply {
                    put("type", type)
                    put("sender", sender)
                    put("message", message)
                    if (type == "private") {
                        put("receiver", intent.getStringExtra("receiver"))
                    } else if (type == "group") {
                        put("groupId", intent.getIntExtra("groupId", 0))
                    }
                }

                // Send the message over the WebSocket connection
                ws?.send(jsonMessage.toString())
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        binding.recyclerFriends.layoutManager = LinearLayoutManager(this)
        binding.recyclerGroups.layoutManager = LinearLayoutManager(this)

        friendsAdapter = FriendsAdapter(emptyList()){ friend ->
            openChat(friend.name)
        }
        binding.recyclerFriends.adapter = friendsAdapter

        groupsAdapter = GroupsAdapter(emptyList()){
            group ->
            openGroup(group.id)
        }
        binding.recyclerGroups.adapter = groupsAdapter

        setSupportActionBar(binding.toolbar)

        val uName : String = SessionManager.getLoggedInUser().toString()
        fetchFriends(uName)
        fetchGroups(uName)

        connectWebSocket()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            sendMessageReceiver,
            IntentFilter("SEND_MESSAGE")
        )

    }

    private fun connectWebSocket() {
        val token = SessionManager.getSessionToken() ?: return
        val request = Request.Builder().url("ws://192.168.1.6:8002/ChatApp/chat/$token").build()
        val client = OkHttpClient()

        ws = client.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                runOnUiThread {
                    val messageData = JSONObject(text)
                    val type = messageData.getString("type")
                    val sender = messageData.getString("sender")
                    val message = messageData.getString("message")

                    // Forward the message to the active ChatActivity
                    if (type == "private") {
                        val intent = Intent("NEW_MESSAGE")
                        intent.putExtra("sender", sender)
                        intent.putExtra("message", message)
                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                    } else if (type == "group") {
                        val groupId = messageData.getInt("groupId")
                        val intent = Intent("NEW_GROUP_MESSAGE")
                        intent.putExtra("groupId", groupId)
                        intent.putExtra("sender", sender)
                        intent.putExtra("message", message)
                        LocalBroadcastManager.getInstance(this@MainActivity).sendBroadcast(intent)
                    }
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }

    //TODO Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Settings Clicked", Toast.LENGTH_SHORT).show()
                true
            }
            R.id.action_logout -> {
                showLogoutDialog()
                true
            }
            R.id.action_addFriend-> {
                showAddFriendDialog()
                true
            }
            R.id.action_createGroup -> {
                showCreateGroupDialog()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showAddFriendDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Friend")

        // Create an EditText for input
        val input = EditText(this)
        input.hint = "Enter friend's username"
        builder.setView(input)

        builder.setPositiveButton("Add") { _, _ ->
            val friendUsername = input.text.toString().trim()
            if (friendUsername.isNotEmpty()) {
                addFriend(friendUsername)
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun addFriend(friendUsername: String) {

        RetrofitClient.instance.insertNewFriend("insertFriend", InsertFriend(SessionManager.getLoggedInUser().toString(),friendUsername))
            .enqueue(object : retrofit2.Callback<Void>{
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                Toast.makeText(this@MainActivity,"Friend Added Successfully",Toast.LENGTH_SHORT).show()
                fetchFriends(SessionManager.getLoggedInUser().toString())
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(this@MainActivity,"Friend cannot be added",Toast.LENGTH_SHORT).show()
            }

        })

    }

    private fun showCreateGroupDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create Group")

        val input = EditText(this)
        input.hint = "Enter group name"
        builder.setView(input)

        builder.setPositiveButton("Create") { _, _ ->
            val groupName = input.text.toString().trim()
            if (groupName.isNotEmpty()) {
                createGroup(groupName) // Call function to handle group creation
            } else {
                Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun createGroup(groupName: String) {
        RetrofitClient.instance.insertNewGroup("insertGroup", InsertGroup(groupName,SessionManager.getLoggedInUser().toString()))
            .enqueue(object : retrofit2.Callback<Void>{
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    Toast.makeText(this@MainActivity,"New Group created Successfully",Toast.LENGTH_SHORT).show()
                    fetchGroups(SessionManager.getLoggedInUser().toString())
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Toast.makeText(this@MainActivity,"Group cannot be created",Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
//                SessionManager.logout(this)

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun openChat(friendName: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("friendName", friendName) // Pass username to ChatActivity
        startActivity(intent)
    }

    private fun openGroup(groupId : Int){
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("groupId", groupId) // Pass groupId to ChatActivity
        startActivity(intent)
    }


    private fun fetchFriends(uName :String ) {
        val request = getRequest("listFriends",uName)

        println("Uname $uName")

        RetrofitClient.instance.getFriends(request.action,request.userName).enqueue(object : Callback<List<Friend>> {
            override fun onResponse(call: Call<List<Friend>>, response: Response<List<Friend>>) {
                if (response.isSuccessful && response.body() != null) {
                    val friendsList = response.body() ?: emptyList()

                    friendsAdapter.updateData(friendsList)
                } else {
                    Toast.makeText(this@MainActivity, "No friends found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Friend>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed to load friends", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchGroups(uName :String) {
        val request = getRequest("listGroups",uName)

        println("in get group "+uName)

        RetrofitClient.instance.getGroups(request.action,request.userName).enqueue(object : Callback<List<Group>> {
            override fun onResponse(call: Call<List<Group>>, response: Response<List<Group>>) {
                if (response.isSuccessful) {
                    if (response.isSuccessful && response.body() != null) {
                        val groupList = response.body() ?: emptyList()

                        groupsAdapter.updateData(groupList)
                    } else {
                        Toast.makeText(this@MainActivity, "No friends found", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<List<Group>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed to load groups", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the BroadcastReceiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sendMessageReceiver)
    }

}

data class getRequest(val action : String,val userName : String){
}
