package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.GroupChatAdapter
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.GroupChat
import com.example.chatapp.model.User
import com.example.chatapp.model.User.Companion.LOGGED_IN_USER_KEY
import com.example.chatapp.repository.GroupChatsHomeScreenRepo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class GroupChatsHomeScreenActivity : AppCompatActivity() {

  private lateinit var btnFriends: Button
  private lateinit var btnProfile: Button
  private lateinit var recyclerView: RecyclerView
  private lateinit var editTextSearch: EditText
  private lateinit var groupChatAdapter: GroupChatAdapter
  private lateinit var overlayLayout: RelativeLayout

  private val groupChatsHomeScreenRepo = GroupChatsHomeScreenRepo()
  private val handler = Handler()
  private val utils = Utils()

  private var userEmail = ""
  private var groupChatList = mutableListOf<GroupChat>()
  private var groupChatsAuthUser = ""
  private var filteredGroupChats = mutableListOf<GroupChat>()
  private var loggedInUserJson = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_home)

    recyclerView = findViewById(R.id.recyclerViewChats)
    editTextSearch = findViewById(R.id.editTextSearch)
    overlayLayout = findViewById(R.id.overlayLayout)
    recyclerView.layoutManager = LinearLayoutManager(this)
    groupChatAdapter = GroupChatAdapter(this, groupChatList)
    recyclerView.adapter = groupChatAdapter

    loggedInUserJson = intent?.getSerializableExtra(LOGGED_IN_USER_KEY) as? String ?: ""

    val btnCreateChat: FloatingActionButton = findViewById(R.id.btnCreateChat)
    btnCreateChat.setOnClickListener {
      val intent = Intent(this, NewChatActivity::class.java)
      intent.putExtra(LOGGED_IN_USER_KEY, loggedInUserJson)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(intent)
    }

    btnFriends = findViewById(R.id.btnFriends)
    btnFriends.setOnClickListener {
      val intent = Intent(this, FriendsActivity::class.java)
      intent.putExtra(LOGGED_IN_USER_KEY, loggedInUserJson)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(intent)
    }

    btnProfile = findViewById(R.id.btnProfile)
    btnProfile.setOnClickListener {
      val intent = Intent(this, ProfileActivity::class.java)
      intent.putExtra(LOGGED_IN_USER_KEY, loggedInUserJson)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(intent)
    }

    groupChatAdapter.setOnItemClickListener { position ->
      val selectedGroupChat = filteredGroupChats.getOrNull(position)

      if (selectedGroupChat != null) {
        val groupChatId = selectedGroupChat.getIdGroupChat()

        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(GroupChat.GROUP_CHAT_ID, groupChatId)
        intent.putExtra(LOGGED_IN_USER_KEY, loggedInUserJson)

        val usernames = selectedGroupChat.users.map { it.username }
        intent.putStringArrayListExtra(GroupChat.GROUP_CHAT_USERNAMES, ArrayList(usernames))

        val userEmails = selectedGroupChat.users.map { it.email }
        intent.putStringArrayListExtra(GroupChat.GROUP_CHAT_EMAILS, ArrayList(userEmails))

        startActivity(intent)
      }
    }

    if (loggedInUserJson.isNotEmpty()) {
      lifecycleScope.launch(Dispatchers.Main) {
        try {
          val user: User = Json.decodeFromString(User.serializer(), loggedInUserJson)
          userEmail = user.email
          groupChatsAuthUser = groupChatsHomeScreenRepo.getGroupChatsAuthUser(userEmail)
          val responseContent = utils.ignoreUnknownKeysJson(groupChatsAuthUser)

          withContext(Dispatchers.Main) {
            if (responseContent.status == "Success") {
              val groupChats: List<GroupChat> =
                responseContent.data.groupchats!!.map { groupChatResponse ->
                  GroupChat(
                    id = groupChatResponse.id,
                    name = groupChatResponse.name,
                    users = groupChatResponse.users,
                  )
                }

              if (groupChats.isEmpty()) {
                recyclerView.visibility = View.GONE
                overlayLayout.visibility = View.VISIBLE
              } else {
                recyclerView.visibility = View.VISIBLE
                overlayLayout.visibility = View.GONE

                println("GroupChats size: ${groupChats.size}")
                groupChatsHomeScreenRepo.updateUIWithGroupChats(
                  groupChats,
                  groupChatList,
                  editTextSearch,
                  groupChatAdapter,
                )
                groupChatsHomeScreenRepo.setItemClickListeners(
                  this@GroupChatsHomeScreenActivity,
                  recyclerView,
                  loggedInUserJson,
                )
              }
            }
          }
          handler.postDelayed(updateRunnable, 10000)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }

    editTextSearch.addTextChangedListener(
      object : TextWatcher {
        override fun beforeTextChanged(
          charSequence: CharSequence?,
          start: Int,
          count: Int,
          after: Int,
        ) {}

        override fun onTextChanged(
          charSequence: CharSequence?,
          start: Int,
          before: Int,
          count: Int,
        ) {
          groupChatsHomeScreenRepo.filterChatList(
            charSequence.toString(),
            groupChatList,
            groupChatAdapter,
            filteredGroupChats,
          )
        }

        override fun afterTextChanged(editable: Editable?) {}
      }
    )
  }

  val updateRunnable: Runnable =
    object : Runnable {
      override fun run() {
        lifecycleScope.launch {
          groupChatsHomeScreenRepo.fetchAndUpdateGroupChats(
            userEmail,
            groupChatList,
            editTextSearch,
            groupChatAdapter,
            filteredGroupChats,
          )
        }
        handler.postDelayed(this, 10000)
      }
    }

  override fun onDestroy() {
    handler.removeCallbacks(updateRunnable)
    super.onDestroy()
  }
}
