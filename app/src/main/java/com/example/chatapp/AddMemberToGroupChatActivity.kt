package com.example.chatapp

import android.app.AlertDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.AddGroupChatMemberAdapter
import com.example.chatapp.dataclass.DataRequest
import com.example.chatapp.dataclass.FilterRequest
import com.example.chatapp.dataclass.FriendRequestData
import com.example.chatapp.dataclass.ServerRequest
import com.example.chatapp.dataclass.ServerResponse
import com.example.chatapp.dataclass.UserData
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.GroupChat
import com.example.chatapp.model.User
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AddMemberToGroupChatActivity : AppCompatActivity() {

  private lateinit var recyclerViewGroupMembers: RecyclerView

  private var GroupChatId: Int? = null

  private lateinit var addGroupChatMemberAdapter: AddGroupChatMemberAdapter
  private var friendEmailList = listOf<String>()
  private var loggedInUserEmail = ""

  private val gson = Gson()
  private val utils = Utils()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_add_member_to_group_chat)

    recyclerViewGroupMembers = findViewById(R.id.recyclerViewGroupMembers)

    val intent = intent
    val groupChatId = intent?.getSerializableExtra(GroupChat.GROUP_CHAT_ID) as? Int ?: 0
    GroupChatId = groupChatId
    println("groupChatId in addMember - $groupChatId")

    val loggedInUserJson = intent?.getStringExtra(User.LOGGED_IN_USER_KEY) ?: ""
    println(" $loggedInUserJson")

    if (loggedInUserJson.isNotEmpty()) {

      lifecycleScope.launch(Dispatchers.Main) {
        try {
          val user: User = Json.decodeFromString(User.serializer(), loggedInUserJson)

          loggedInUserEmail = user.email

          println("loggedInUserUsername - $loggedInUserEmail")
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }

    addGroupChatMemberAdapter = AddGroupChatMemberAdapter(emptyList(), emptyList())
    recyclerViewGroupMembers.layoutManager = LinearLayoutManager(this)
    recyclerViewGroupMembers.adapter = addGroupChatMemberAdapter

    addGroupChatMemberAdapter.setOnItemClickListener { user, friendEmail ->
      showFriendsDialog(user, friendEmail)
    }

    GlobalScope.launch(Dispatchers.Main) {
      try {
        val getFriendsAuthUserJSON = getFriendsAuthUser(loggedInUserEmail)

        val responseFriends =
          Json { ignoreUnknownKeys = true }.decodeFromString<ServerResponse>(getFriendsAuthUserJSON)
        val friendsList: List<FriendRequestData> =
          responseFriends.data.friendrequests ?: emptyList()

        val friends = friendsList.map { it.recipient }
        friendEmailList = friendsList.map { it.recipient.email }

        addGroupChatMemberAdapter.updateData(friends, friendEmailList)
        recyclerViewGroupMembers.adapter = addGroupChatMemberAdapter
        println("Number of friends: ${friendsList.size}")
      } catch (e: Exception) {
        e.printStackTrace()
        println("Error in getFriendRequestsAuthUser: ${e.message}")
      }
    }
  }

  private fun showFriendsDialog(user: UserData, friendEmail: String) {
    val alertDialogBuilder = AlertDialog.Builder(this)
    alertDialogBuilder.setTitle("Friends")
    alertDialogBuilder.setMessage("Do you want to add ${user.username} to your group chat?")

    println("friendEmail - $friendEmail")

    alertDialogBuilder.setPositiveButton("Add member") { _, _ ->
      GlobalScope.launch {
        GroupChatId?.let {
          val addUserToGroupChatJson = addUserToGroupChat(it, friendEmail)

          val addUserResponse =
            Json { ignoreUnknownKeys = true }
              .decodeFromString<ServerResponse>(addUserToGroupChatJson)

          runOnUiThread {
            // Show Toast on the main UI thread
            utils.showToast(this@AddMemberToGroupChatActivity, addUserResponse.message)
          }
        }
      }
    }

    alertDialogBuilder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

    val alertDialog: AlertDialog = alertDialogBuilder.create()
    alertDialog.show()
  }

  private suspend fun getFriendsAuthUser(email: String): String {
    try {
      val eventType: String = "GetFriendRequests"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getFriendsAuthUser =
        ServerRequest(
          eventType = eventType,
          data = DataRequest(user = UserData(id = 0, username = "", email = email, password = "")),
          filter =
            FilterRequest(
              FriendRequestData(
                id = 0,
                status = "Accepted",
                sender = UserData(id = 0, username = "", email = email, password = ""),
                recipient = UserData(id = 0, username = "", email = email, password = "")
              )
            )
        )

      val json = gson.toJson(getFriendsAuthUser)
      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  private suspend fun addUserToGroupChat(groupChatId: Int, email: String): String {
    try {
      val eventType: String = "AddUserToGroupChat"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getFriendsAuthUser =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              id = groupChatId.toString(),
              user = UserData(id = 0, username = "", email = email, password = "")
            ),
        )

      val json = gson.toJson(getFriendsAuthUser)
      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }
}
