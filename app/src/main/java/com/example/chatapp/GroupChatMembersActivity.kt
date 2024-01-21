package com.example.chatapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.MembersInAGroupChatAdapter
import com.example.chatapp.dataclass.DataRequest
import com.example.chatapp.dataclass.ServerRequest
import com.example.chatapp.dataclass.ServerResponse
import com.example.chatapp.dataclass.UserData
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.GroupChat
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_EMAILS
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_USERNAMES
import com.example.chatapp.model.User
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class GroupChatMembersActivity : AppCompatActivity() {

  private lateinit var recycleViewMembers: RecyclerView
  private lateinit var btnAddMember: Button

  private lateinit var membersInAGroupChatAdapter: MembersInAGroupChatAdapter

  private var GroupChatId: Int? = null

  private val gson = Gson()
  private val utils = Utils()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_group_chat_members)

    recycleViewMembers = findViewById(R.id.recyclerViewGroupMembers)
    btnAddMember = findViewById(R.id.btnAddMember)

    membersInAGroupChatAdapter = MembersInAGroupChatAdapter(emptyList(), emptyList())
    recycleViewMembers.layoutManager = LinearLayoutManager(this)
    recycleViewMembers.adapter = membersInAGroupChatAdapter

    val loggedInUserJson = intent?.getStringExtra(User.LOGGED_IN_USER_KEY) ?: ""
    println(" $loggedInUserJson")

    val groupChatId = intent?.getSerializableExtra(GroupChat.GROUP_CHAT_ID) as? Int ?: 0
    GroupChatId = groupChatId
    println("groupChatId in members - $groupChatId")

    btnAddMember.setOnClickListener {
      val intent = Intent(this, AddMemberToGroupChatActivity::class.java)
      intent.putExtra(User.LOGGED_IN_USER_KEY, loggedInUserJson)
      intent.putExtra(GroupChat.GROUP_CHAT_ID, groupChatId)
      startActivity(intent)
    }

    val usernames = intent.getStringArrayListExtra(GROUP_CHAT_USERNAMES)
    val userEmails = intent.getStringArrayListExtra(GROUP_CHAT_EMAILS)

    if (userEmails != null && userEmails.isNotEmpty() && usernames != null) {
      val userDataList =
        userEmails.mapIndexed { index, email ->
          UserData(id = 0, username = usernames[index], email = email, password = "")
        }

      val membersInAGroupChatAdapter = MembersInAGroupChatAdapter(userDataList, userEmails)
      membersInAGroupChatAdapter.setOnItemClickListener { user, friendEmail ->
        println("Item clicked - ${user.username}, $friendEmail") // Add this line
        showFriendsDialog(user, friendEmail)
      }
      recycleViewMembers.adapter = membersInAGroupChatAdapter
    } else {
      println("Invalid userEmails or usernames data: $userEmails, $usernames")
    }
  }

  private fun showFriendsDialog(user: UserData, friendEmail: String) {
    val alertDialogBuilder = AlertDialog.Builder(this)
    alertDialogBuilder.setTitle("Members")
    alertDialogBuilder.setMessage("Do you want to remove ${user.username} from your group chat?")

    println("friendEmail - $friendEmail")

    alertDialogBuilder.setPositiveButton("Remove member") { _, _ ->
      GlobalScope.launch {
        GroupChatId?.let {
          val removeUserFromGroupChatJson = removeUserFromGroupChat(it, friendEmail)

          val removeUserResponse =
            Json { ignoreUnknownKeys = true }
              .decodeFromString<ServerResponse>(removeUserFromGroupChatJson)

          runOnUiThread {
            // Show Toast on the main UI thread
            utils.showToast(this@GroupChatMembersActivity, removeUserResponse.message)
          }
        }
      }
    }
    val alertDialog: AlertDialog = alertDialogBuilder.create()
    alertDialog.show()
  }

  private suspend fun removeUserFromGroupChat(groupChatId: Int, email: String): String {
    try {
      val eventType: String = "RemoveUserFromGroupChat"
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
