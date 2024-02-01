package com.example.chatapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.IncomingFriendRequestAdapter
import com.example.chatapp.Adapters.PendingFriendRequestAdapter
import com.example.chatapp.dataclass.FriendRequestData
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.User
import com.example.chatapp.model.User.Companion.LOGGED_IN_USER_KEY
import com.example.chatapp.repository.SharedFriendRequestRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class FriendRequestActivity : AppCompatActivity() {

  private lateinit var edtEmail: EditText
  private lateinit var btnAddFriend: Button

  private lateinit var recyclerViewPendingInvites: RecyclerView
  private lateinit var recyclerViewFriendInvitations: RecyclerView
  private lateinit var pendingUserAdapter: PendingFriendRequestAdapter
  private lateinit var incomingFriendRequestAdapter: IncomingFriendRequestAdapter

  private val sharedFriendRequestRepo = SharedFriendRequestRepo()
  private val utils = Utils()

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

    incomingFriendRequestAdapter.setOnItemClickListener { user, friendId ->
      sharedFriendRequestRepo.showFriendRequestDialog(this, user, friendId)
    }

    val loggedInUserJson = intent?.getSerializableExtra(LOGGED_IN_USER_KEY) as? String ?: ""
    println("loggedInUserJson in ProfileActivity - $loggedInUserJson")

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
        val getFriendRequestsAuthUserJSON =
          sharedFriendRequestRepo.getFriendRequestsAuthUser(userEmail)
        val responsePendingFriend = utils.ignoreUnknownKeysJson(getFriendRequestsAuthUserJSON)
        val friendRequests: List<FriendRequestData> =
          responsePendingFriend.data.friendrequests ?: emptyList()
        val pendingUsers = friendRequests.map { it.recipient }

        pendingUserAdapter.updateData(pendingUsers)

        // Incoming friend requests
        val getIncomingFriendRequestsJSON =
          sharedFriendRequestRepo.getIncomingFriendRequests(userEmail)

        val responseIncomingFriend = utils.ignoreUnknownKeysJson(getIncomingFriendRequestsJSON)
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

              val receivedMessageFromServer =
                sharedFriendRequestRepo.performFriendRequest(userEmail, emailRecipient)

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
}
