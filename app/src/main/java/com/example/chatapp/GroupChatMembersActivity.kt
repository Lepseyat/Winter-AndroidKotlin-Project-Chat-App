package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.MembersInAGroupChatAdapter
import com.example.chatapp.dataclass.UserData
import com.example.chatapp.model.GroupChat
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_EMAILS
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_USERNAMES
import com.example.chatapp.model.User
import com.example.chatapp.repository.SharedGroupChatMembersRepo

class GroupChatMembersActivity : AppCompatActivity() {

  private lateinit var recycleViewMembers: RecyclerView
  private lateinit var btnAddMember: Button

  private lateinit var membersInAGroupChatAdapter: MembersInAGroupChatAdapter

  private var GroupChatId: Int? = null

  private val groupChatMembersRepo = SharedGroupChatMembersRepo()

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
        println("Item clicked - ${user.username}, $friendEmail")
        groupChatMembersRepo.showFriendsDialog(this, user.username, friendEmail, GroupChatId!!)
      }
      recycleViewMembers.adapter = membersInAGroupChatAdapter
    } else {
      println("Invalid userEmails or usernames data: $userEmails, $usernames")
    }
  }
}
