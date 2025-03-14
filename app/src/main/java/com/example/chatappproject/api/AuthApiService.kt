package com.example.chatappproject.api


import com.example.chatappproject.login.LoginRequest
import com.example.chatappproject.login.LoginResponse
import com.example.chatappproject.models.Friend
import com.example.chatappproject.models.FriendsResponse
import com.example.chatappproject.models.Group
import com.example.chatappproject.models.GroupMessagesResponse
import com.example.chatappproject.models.GroupsResponse
import com.example.chatappproject.models.InsertFriend
import com.example.chatappproject.models.InsertGroup
import com.example.chatappproject.models.InsertGroupMsg
import com.example.chatappproject.models.InsertMsg
import com.example.chatappproject.models.MessagesResponse
import com.example.chatappproject.signup.SignupRequest
import com.example.chatappproject.signup.SignupResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Query

interface AuthApiService {
    @POST("ChatApp/login") //For Login
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("ChatApp/signup")        //For Signup
    fun signup(@Body request: SignupRequest): Call<SignupResponse>

    @GET("ChatApp/ChatServlet")
    fun getFriends(
        @Query("action") action: String = "listFriends",
        @Query("userName") userName: String
    ): Call<List<Friend>>

    @GET("ChatApp/ChatServlet")
    fun getMessages(
        @Query("action") action: String = "loadMessages",
        @Query("userName") userId: String,
        @Query("friendName") friendId: String
    ): Call<List<MessagesResponse>>

    @GET("ChatApp/ChatServlet")
    fun getGroups(
        @Query("action") action: String = "listGroups",
        @Query("userName") userName: String
    ): Call<List<Group>>

    @GET("ChatApp/ChatServlet")
    fun getGroupMessages(
        @Query("action") action: String = "getGroupMessages",
        @Query("groupId") userId: Int
    ): Call<List<MessagesResponse>>

    @POST("ChatApp/ChatServlet")
    fun insertMessages(
        @Query("action") action : String ,
        @Body request : InsertMsg
    ) : Call<Void>

    @POST("ChatApp/ChatServlet")
    fun insertGroupMessages(
        @Query("action") action : String ,
        @Body request : InsertGroupMsg
    ) : Call<Void>

    @POST("ChatApp/ChatServlet")
    fun insertNewFriend(
        @Query("action") action : String,
        @Body request: InsertFriend
    ) : Call<Void>

    @POST("ChatApp/ChatServlet")
    fun insertNewGroup(
        @Query("action") action : String,
        @Body request: InsertGroup
    ) : Call<Void>

    @Multipart
    @POST("ChatApp/ChatServlet")
    fun uploadProfilePhoto(
        @Query("action") action: String,
        @Part("username") username: RequestBody,
        @Part photo: MultipartBody.Part
    ): Call<ResponseBody>
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.6:8002/"

    val instance: AuthApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Convert JSON to Kotlin objects
            .build()
            .create(AuthApiService::class.java)
    }
}