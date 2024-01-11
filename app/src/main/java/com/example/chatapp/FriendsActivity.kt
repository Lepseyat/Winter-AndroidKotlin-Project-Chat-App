package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.dataclass.FriendsAuthUser
import com.example.chatapp.dataclass.GetFriendsAuthUser
import com.example.chatapp.dataclass.ResponseFriendsAuthUser
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.User
import com.example.chatapp.model.User.Companion.LOGGED_IN_USER_KEY
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class FriendsActivity : AppCompatActivity() {

  private lateinit var btnAddFriend: Button
  private lateinit var btnChat: Button
  private lateinit var btnProfile: Button

  private val gson = Gson()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_friends)

    btnAddFriend = findViewById(R.id.btnAddFriend)
    btnChat = findViewById(R.id.btnChat)
    btnProfile = findViewById(R.id.btnProfile)

    val loggedInUserJson = intent?.getSerializableExtra(LOGGED_IN_USER_KEY) as? String ?: ""
    println("loggedInUserJson in FriendsActivity - $loggedInUserJson")

    val utils = Utils()
    var userEmail = ""

    if (loggedInUserJson.isNotEmpty()) {
      try {
        val user: User = Json.decodeFromString(User.serializer(), loggedInUserJson)
        userEmail = user.email
        println("userEmail received to friendRequestActivity - ${user.email}")
      } catch (e: SerializationException) {
        println("Error decoding JSON: ${e.message}")
      }
    } else {
      println("Empty JSON string received.")
    }

    val base64EmailSender = utils.base64(userEmail)

    GlobalScope.launch(Dispatchers.Main) {
      try {
        val getFriendsAuthUserJSON = getFriendsAuthUser(base64EmailSender)

        val responseFriendsAuthUser =
          Json { ignoreUnknownKeys = true }
            .decodeFromString<ResponseFriendsAuthUser>(getFriendsAuthUserJSON)

        /*val friendRequests: List<FriendRequestDataClass> =
          responsePendingFriend.response.friendRequests ?: emptyList()
        val pendingUsers = friendRequests.map { it.recipient }

        pendingUserAdapter.updateData(pendingUsers)*/

      } catch (e: Exception) {
        e.printStackTrace()
        println("Error in getFriendRequestsAuthUser: ${e.message}")
      }
    }

    btnAddFriend.setOnClickListener {
      val intent = Intent(this, FriendRequestActivity::class.java)
      intent.putExtra(LOGGED_IN_USER_KEY, loggedInUserJson)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(Intent(intent))
    }

    btnProfile.setOnClickListener {
      val intent = Intent(this, ProfileActivity::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(Intent(intent))
    }

    btnChat.setOnClickListener {
      val intent = Intent(this, Chat::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(intent)
    }
  }

  private suspend fun getFriendsAuthUser(email: String): String {
    try {
      val utils = Utils()
      val eventType: String = "GetFriendsAuthUser"
      val encodedEventType = utils.base64(eventType)
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getFriendsAuthUser =
        GetFriendsAuthUser(eventType = encodedEventType, data = FriendsAuthUser(email = email))

      val json = gson.toJson(getFriendsAuthUser)

      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }
}
