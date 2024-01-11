package com.example.chatapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.IncomingFriendRequestAdapter
import com.example.chatapp.Adapters.UserAdapter
import com.example.chatapp.dataclass.FriendRequestDataClass
import com.example.chatapp.dataclass.GetFriendRequestsAuthUser
import com.example.chatapp.dataclass.GetIncomingFriendRequests
import com.example.chatapp.dataclass.IncomingFriendRequestDataClass
import com.example.chatapp.dataclass.IncomingFriendRequests
import com.example.chatapp.dataclass.PendingFriendRequests
import com.example.chatapp.dataclass.ResponseIncomingFriend
import com.example.chatapp.dataclass.ResponsePendingFriend
import com.example.chatapp.dataclass.UserFriendRequest
import com.example.chatapp.dataclass.UserFriendRequestActivity
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

class FriendRequestActivity : AppCompatActivity() {

  private lateinit var edtEmail: EditText
  private lateinit var btnAddFriend: Button

  private lateinit var recyclerViewPendingInvites: RecyclerView
  private lateinit var recyclerViewFriendInvitations: RecyclerView
  private lateinit var pendingUserAdapter: UserAdapter
  private lateinit var incomingFriendRequestAdapter: IncomingFriendRequestAdapter

  private val gson = Gson()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_friend_request)

    edtEmail = findViewById(R.id.edt_email)
    btnAddFriend = findViewById(R.id.btnAddFriend)

    recyclerViewPendingInvites = findViewById(R.id.recyclerViewPendingInvites)
    pendingUserAdapter = UserAdapter(emptyList())
    recyclerViewPendingInvites.layoutManager = LinearLayoutManager(this)
    recyclerViewPendingInvites.adapter = pendingUserAdapter

    recyclerViewFriendInvitations = findViewById(R.id.recyclerViewFriendInvitations)
    incomingFriendRequestAdapter = IncomingFriendRequestAdapter(emptyList())
    recyclerViewFriendInvitations.layoutManager = LinearLayoutManager(this)
    recyclerViewFriendInvitations.adapter = incomingFriendRequestAdapter

    val loggedInUserJson = intent?.getSerializableExtra(LOGGED_IN_USER_KEY) as? String ?: ""
    println("loggedInUserJson in ProfileActivity - $loggedInUserJson")

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
        val getFriendRequestsAuthUserJSON = getFriendRequestsAuthUser(base64EmailSender)

        // new
        val getIncomingFriendRequestsJSON = getIncomingFriendRequests(base64EmailSender)
        println("getPendingFriendRequestsJSON - $getIncomingFriendRequestsJSON")

        val responsePendingFriend =
          Json { ignoreUnknownKeys = true }
            .decodeFromString<ResponsePendingFriend>(getFriendRequestsAuthUserJSON)

        // new
        val responseIncomingFriend =
          Json { ignoreUnknownKeys = true }
            .decodeFromString<ResponseIncomingFriend>(getIncomingFriendRequestsJSON)

        val friendRequests: List<FriendRequestDataClass> =
          responsePendingFriend.response.friendRequests ?: emptyList()
        val pendingUsers = friendRequests.map { it.sender }

        // new
        val incomingFriendRequests: List<IncomingFriendRequestDataClass> =
          responseIncomingFriend.response.friendRequests ?: emptyList()
        val incomingUsers = incomingFriendRequests.map { it.recipient }

        pendingUserAdapter.updateData(pendingUsers)
        // new
        incomingFriendRequestAdapter.updateData(incomingUsers)
      } catch (e: Exception) {
        e.printStackTrace()
        println("Error in getFriendRequestsAuthUser: ${e.message}")
      }
    }

    btnAddFriend.setOnClickListener {
      val email = edtEmail.text.toString()
      if (email.isBlank()) {
        utils.showToast(this, "Fill the empty field")
      } else {
        if (utils.isValidEmail(email)) {
          GlobalScope.launch(Dispatchers.Main) {
            println("UserEmail in button - $userEmail")

            val base64EmailReceiver = utils.base64(email)

            val receivedMessageFromServer =
              performFriendRequest(base64EmailSender, base64EmailReceiver)

            val status = utils.gsonResponse(receivedMessageFromServer)
            println("Status for friend request: $status")

            if (status == "Success") {

              utils.showToast(this@FriendRequestActivity, "Friend request sent")
            } else {
              utils.showToast(this@FriendRequestActivity, "Email doesn't exist")
            }
          }
        } else {
          utils.showToast(this, "Incorrect email")
        }
      }
    }
  }

  private suspend fun performFriendRequest(senderEmail: String, emailRecipient: String): String {
    try {
      val utils = Utils()
      val eventType: String = "SendFriendRequest"
      val encodedEventType = utils.base64(eventType)
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val friendRequestActivity =
        UserFriendRequestActivity(
          eventType = encodedEventType,
          data = UserFriendRequest(emailSender = senderEmail, emailRecipient = emailRecipient)
        )

      val json = gson.toJson(friendRequestActivity)

      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  private suspend fun getFriendRequestsAuthUser(email: String): String {
    try {
      val utils = Utils()
      val eventType: String = "GetFriendRequestsAuthUser"
      val encodedEventType = utils.base64(eventType)
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getFriendRequestsAuthUser =
        GetFriendRequestsAuthUser(
          eventType = encodedEventType,
          data = PendingFriendRequests(email = email)
        )

      val json = gson.toJson(getFriendRequestsAuthUser)

      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  private suspend fun getIncomingFriendRequests(email: String): String {
    try {
      val utils = Utils()
      val eventType: String = "GetIncomingFriendRequests"
      val encodedEventType = utils.base64(eventType)
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getIncomingFriendRequests =
        GetIncomingFriendRequests(
          eventType = encodedEventType,
          data = IncomingFriendRequests(email = email)
        )

      val json = gson.toJson(getIncomingFriendRequests)

      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }
}
