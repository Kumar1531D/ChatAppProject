package com.example.chatappproject.websocket

import android.util.Log
import com.example.chatappproject.login.SessionManager
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ChatWebSocketManager() : WebSocketListener() {

//    private fun connectWebSocket() {
//        val token = SessionManager.getSessionToken() ?: return
//        val request = Request.Builder().url("ws://192.168.1.3:8002/ChatApp/chat/$token").build()
//        val client = OkHttpClient()
//        var ws: okhttp3.WebSocket?
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
//                        else{
//                            showNotification(messageData.getString("sender"),messageData.getString("message"))
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
//                        else{
//                            showNotification(messageData.getString("sender"),messageData.getString("message"))
//                        }
//                    }
//                }
//            }
//
//            override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: Response?) {
//                super.onFailure(webSocket, t, response)
//                println("WebSocket failed: ${t.message}")
//                ws = null // ✅ Reset ws to null so we can reconnect
//            }
//
//            override fun onClosed(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
//                super.onClosed(webSocket, code, reason)
//                println("WebSocket closed: $reason")
//                ws = null // ✅ Mark WebSocket as disconnected
//            }
//        })
//    }

}
