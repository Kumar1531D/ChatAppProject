package com.example.chatappproject.websocket

import android.annotation.SuppressLint
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import okhttp3.*
import okio.ByteString
import org.json.JSONObject

class WebSocketService : Service() {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val token = intent?.getStringExtra("token") ?: return START_NOT_STICKY
        connectWebSocket(token)
        return START_STICKY
    }

    private fun connectWebSocket(token: String) {
        val request = Request.Builder()
            .url("ws://192.168.1.6:8002/ChatApp/chat/$token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Received message: $text")

                val messageData = JSONObject(text)
                val intent = Intent("CHAT_MESSAGE")
                intent.putExtra("message", text)
                sendBroadcast(intent) // 🔥 Broadcast message to activities
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket Closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket Error: ${t.message}")
            }
        })
    }

    fun sendMessage(messageJson: JSONObject) {
        webSocket?.send(messageJson.toString())
    }

    override fun onDestroy() {
        webSocket?.close(1000, "Service Stopped")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private val sendMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val messageJson = intent?.getStringExtra("messageJson") ?: return
            webSocket?.send(messageJson)
        }
    }

}