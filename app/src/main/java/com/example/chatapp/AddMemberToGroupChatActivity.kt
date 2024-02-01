package com.example.chatapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.AddGroupChatMemberAdapter
import com.example.chatapp.dataclass.FriendRequestData
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.GroupChat
import com.example.chatapp.model.User
import com.example.chatapp.repository.SharedGroupChatMembersRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AddMemberToGroupChatActivity : AppCompatActivity() {

  private lateinit var recyclerViewGroupMembers: RecyclerView

  private var GroupChatId: Int? = null

  private lateinit var addGroupChatMemberAdapter: AddGroupChatMemberAdapter
  private var friendEmailList = listOf<String>()
  private var loggedInUserEmail = ""

  private val sharedGroupChatMembersRepo = SharedGroupChatMembersRepo()
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
      sharedGroupChatMembersRepo.showFriendsDialog(this, user, friendEmail, GroupChatId!!)
    }

    GlobalScope.launch(Dispatchers.Main) {
      try {
        val getFriendsAuthUserJSON =
          sharedGroupChatMembersRepo.getFriendsAuthUser(loggedInUserEmail)

        val responseFriends = utils.ignoreUnknownKeysJson(getFriendsAuthUserJSON)
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
}
