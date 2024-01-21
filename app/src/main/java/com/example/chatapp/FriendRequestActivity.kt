package com.example.chatapp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.IncomingFriendRequestAdapter
import com.example.chatapp.Adapters.PendingFriendRequestAdapter
import com.example.chatapp.dataclass.DataRequest
import com.example.chatapp.dataclass.FilterRequest
import com.example.chatapp.dataclass.FriendRequestData
import com.example.chatapp.dataclass.ServerRequest
import com.example.chatapp.dataclass.ServerResponse
import com.example.chatapp.dataclass.UserData
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
  private lateinit var pendingUserAdapter: PendingFriendRequestAdapter
  private lateinit var incomingFriendRequestAdapter: IncomingFriendRequestAdapter

  private val gson = Gson()

  private var userEmail = ""
  private var friendRequestId = listOf<Int>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_friend_request)

    edtEmail = findViewById(R.id.edt_email)
    btnAddFriend = findViewById(R.id.btnAddFriend)

    recyclerViewPendingInvites = findViewById(R.id.recyclerViewPendingInvites)
    pendingUserAdapter = PendingFriendRequestAdapter(emptyList())
    recyclerViewPendingInvites.layoutManager = LinearLayoutManager(this)
    recyclerViewPendingInvites.adapter = pendingUserAdapter

    recyclerViewFriendInvitations = findViewById(R.id.recyclerViewFriendInvitations)
    incomingFriendRequestAdapter = IncomingFriendRequestAdapter(emptyList(), emptyList())
    recyclerViewFriendInvitations.layoutManager = LinearLayoutManager(this)
    recyclerViewFriendInvitations.adapter = incomingFriendRequestAdapter

    incomingFriendRequestAdapter.setOnItemClickListener { user, friendid ->
      showFriendRequestDialog(user, friendid)
    }

    val loggedInUserJson = intent?.getSerializableExtra(LOGGED_IN_USER_KEY) as? String ?: ""
    println("loggedInUserJson in ProfileActivity - $loggedInUserJson")

    val utils = Utils()

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

    GlobalScope.launch(Dispatchers.Main) {
      try {

        // Pending friend requests
        val getFriendRequestsAuthUserJSON = getFriendRequestsAuthUser(userEmail)
        val responsePendingFriend =
          Json { ignoreUnknownKeys = true }
            .decodeFromString<ServerResponse>(getFriendRequestsAuthUserJSON)
        val friendRequests: List<FriendRequestData> =
          responsePendingFriend.data.friendrequests ?: emptyList()
        val pendingUsers = friendRequests.map { it.recipient }
        pendingUserAdapter.updateData(pendingUsers)

        // Incoming friend requests
        val getIncomingFriendRequestsJSON = getIncomingFriendRequests(userEmail)
        println("getPendingFriendRequestsJSON - $getIncomingFriendRequestsJSON")

        val responseIncomingFriend =
          Json { ignoreUnknownKeys = true }
            .decodeFromString<ServerResponse>(getIncomingFriendRequestsJSON)
        val incomingFriendRequests: List<FriendRequestData> =
          responseIncomingFriend.data.friendrequests ?: emptyList()

        friendRequestId = incomingFriendRequests.map { it.id }

        val incomingUsers = incomingFriendRequests.map { it.recipient }
        incomingFriendRequestAdapter.updateData(incomingUsers, friendRequestId)
      } catch (e: Exception) {
        e.printStackTrace()
        println("Error in getFriendRequestsAuthUser: ${e.message}")
      }
    }

    btnAddFriend.setOnClickListener {
      val emailRecipient = edtEmail.text.toString()
      if (emailRecipient.isBlank()) {
        utils.showToast(this, "Fill the empty field")
      } else {
        if (utils.isValidEmail(emailRecipient)) {
          try {
            GlobalScope.launch(Dispatchers.Main) {
              println("UserEmail in button - $userEmail")

              val receivedMessageFromServer = performFriendRequest(userEmail, emailRecipient)

              val status = utils.gsonResponse(receivedMessageFromServer)
              println("Status for friend request: $status")

              if (status == "Success") {

                utils.showToast(this@FriendRequestActivity, "Friend request sent")
              } else {
                utils.showToast(this@FriendRequestActivity, "Email doesn't exist")
              }
            }
          } catch (e: Exception) {
            e.printStackTrace()
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
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val friendRequestActivity =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              friendrequest =
                FriendRequestData(
                  id = 0,
                  status = "Pending",
                  sender = UserData(id = 0, username = "", email = senderEmail, password = ""),
                  recipient = UserData(id = 0, username = "", email = emailRecipient, password = "")
                )
            )
        )

      val json = gson.toJson(friendRequestActivity)

      println("performFriendRequest json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  private suspend fun getFriendRequestsAuthUser(email: String): String {
    try {
      val utils = Utils()
      val eventType: String = "GetFriendRequests"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getFriendRequestsAuthUser =
        ServerRequest(
          eventType = eventType,
          data = DataRequest(user = UserData(id = 0, username = "", email = email, password = "")),
          filter =
            FilterRequest(
              FriendRequestData(
                id = 0,
                status = "Pending",
                sender = UserData(id = 0, username = "", email = email, password = ""),
                recipient = UserData(id = 0, username = "", email = "", password = "")
              )
            )
        )

      val json = gson.toJson(getFriendRequestsAuthUser)

      println("getFriendRequestsAuthUser - json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  private suspend fun getIncomingFriendRequests(email: String): String {
    try {
      val utils = Utils()
      val eventType: String = "GetFriendRequests"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getIncomingFriendRequests =
        ServerRequest(
          eventType = eventType,
          data = DataRequest(user = UserData(id = 0, username = "", email = email, password = "")),
          filter =
            FilterRequest(
              FriendRequestData(
                id = 0,
                status = "Pending",
                sender = UserData(id = 0, username = "", email = "", password = ""),
                recipient = UserData(id = 0, username = "", email = email, password = "")
              )
            )
        )

      val json = gson.toJson(getIncomingFriendRequests)

      println("getIncomingFriendRequests - json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  private fun showFriendRequestDialog(user: UserData, friendid: Int) {
    val alertDialogBuilder = AlertDialog.Builder(this)
    alertDialogBuilder.setTitle("Friend Request")
    alertDialogBuilder.setMessage("Do you want to add ${user.username} to your friends list?")

    alertDialogBuilder.setNegativeButton("Reject") { _, _ ->
      GlobalScope.launch { rejectFriendRequest(friendid) }
    }
    alertDialogBuilder.setPositiveButton("Accept") { _, _ ->
      GlobalScope.launch { acceptFriendRequest(friendid) }
    }

    val alertDialog: AlertDialog = alertDialogBuilder.create()
    alertDialog.show()
  }

  private suspend fun acceptFriendRequest(friendRequestId: Int): String {
    try {
      val eventType: String = "FriendRequestOperation"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getIncomingFriendRequests =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              friendrequest =
                FriendRequestData(
                  id = friendRequestId,
                  status = "Accepted",
                  sender = UserData(id = 0, username = "", email = "", password = ""),
                  recipient = UserData(id = 0, username = "", email = "", password = "")
                )
            ),
        )

      val json = gson.toJson(getIncomingFriendRequests)

      println("getIncomingFriendRequests - json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  private suspend fun rejectFriendRequest(friendRequestId: Int): String {
    try {
      val eventType: String = "FriendRequestOperation"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getIncomingFriendRequests =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              friendrequest =
                FriendRequestData(
                  id = friendRequestId,
                  status = "Rejected",
                  sender = UserData(id = 0, username = "", email = "", password = ""),
                  recipient = UserData(id = 0, username = "", email = "", password = "")
                )
            ),
        )

      val json = gson.toJson(getIncomingFriendRequests)

      println("getIncomingFriendRequests - json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }
}
