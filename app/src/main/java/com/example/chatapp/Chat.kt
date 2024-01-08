package com.example.chatapp

import android.content.Intent
import android.os.Bundle
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
import com.example.chatapp.dataclass.GetGroupChats
import com.example.chatapp.dataclass.ResponseWrapper
import com.example.chatapp.dataclass.UserChats
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.GroupChat
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_ID
import com.example.chatapp.model.User
import com.example.chatapp.model.User.Companion.LOGGED_IN_USER_KEY
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class Chat : AppCompatActivity() {

  private lateinit var btnFriends: Button
  private lateinit var btnProfile: Button
  private lateinit var recyclerView: RecyclerView
  private lateinit var editTextSearch: EditText
  private lateinit var groupChatAdapter: GroupChatAdapter
  private lateinit var overlayLayout: RelativeLayout

  private var userEmail = ""
  private var groupChatList = mutableListOf<GroupChat>()
  private val gson = Gson()
  private val utils = Utils
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

    val fabCreateChat: FloatingActionButton = findViewById(R.id.fabCreateChat)
    fabCreateChat.setOnClickListener {
      val intent = Intent(this, NewChatActivity::class.java)
      startActivity(Intent(intent))
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

    if (loggedInUserJson.isNotEmpty()) {
      lifecycleScope.launch(Dispatchers.Main) {
        try {
          val user: User = Json.decodeFromString(User.serializer(), loggedInUserJson)
          userEmail = user.email
          val base64Email = utils.base64(userEmail)
          groupChatsAuthUser = getGroupChatsAuthUser(base64Email)
          val responseWrapper =
            Json { ignoreUnknownKeys = true }.decodeFromString<ResponseWrapper>(groupChatsAuthUser)

          val response = responseWrapper.response

          withContext(Dispatchers.Main) {
            if (response.status == "Success") {
              val groupChats: List<GroupChat> =
                response.groupchats.map { groupChatResponse ->
                  GroupChat(
                    id = groupChatResponse.id,
                    name = groupChatResponse.name,
                    users = groupChatResponse.users,
                    messages = groupChatResponse.message
                  )
                }

              if (groupChats.isEmpty()) {
                recyclerView.visibility = View.GONE
                overlayLayout.visibility = View.VISIBLE
              } else {
                recyclerView.visibility = View.VISIBLE
                overlayLayout.visibility = View.GONE

                println("GroupChats size: ${groupChats.size}")
                updateUIWithGroupChats(groupChats)
                setItemClickListeners(groupChats)
              }
            } else {
              // Handle other status values
            }
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }

    // Search field
    editTextSearch.addTextChangedListener(
      object : TextWatcher {
        override fun beforeTextChanged(
          charSequence: CharSequence?,
          start: Int,
          count: Int,
          after: Int
        ) {
          // Not used in this example
        }

        override fun onTextChanged(
          charSequence: CharSequence?,
          start: Int,
          before: Int,
          count: Int
        ) {
          // Filter your list or perform search based on the entered text
          filterChatList(charSequence.toString())
        }

        override fun afterTextChanged(editable: Editable?) {}
      }
    )
  }

  private suspend fun updateUIWithGroupChats(groupChats: List<GroupChat>) {
    withContext(Dispatchers.Main) {
      groupChatList.clear()
      groupChatList.addAll(groupChats)

      filteredGroupChats =
        groupChatList
          .filter { chat -> chat.name.contains(editTextSearch.text.toString(), ignoreCase = true) }
          .toMutableList()

      groupChatAdapter.updateData(filteredGroupChats)
    }
  }

  private fun setItemClickListeners(groupChats: List<GroupChat>) {
    recyclerView.addOnItemClickListener { position, _ ->
      val selectedGroupChat = filteredGroupChats.getOrNull(position)

      if (selectedGroupChat != null) {
        val groupChatId = selectedGroupChat.getIdGroupChat()

        val intent = Intent(this@Chat, GroupChatActivity::class.java)
        intent.putExtra(GROUP_CHAT_ID, groupChatId)
        intent.putExtra(LOGGED_IN_USER_KEY, loggedInUserJson)
        startActivity(intent)
        onPause()
      }
    }
  }

  private suspend fun getGroupChatsAuthUser(email: String): String {
    try {
      val utils = Utils()
      val eventType: String = "GetGroupChatsAuthUser"
      val encodedEventType = utils.base64(eventType)
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getGroupChats = GetGroupChats(eventType = encodedEventType, UserChats(email = email))

      val json = gson.toJson(getGroupChats)

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  private fun filterChatList(query: String) {
    val filteredChatList =
      groupChatList.filter { chat -> chat.name.contains(query, ignoreCase = true) }
    groupChatAdapter.updateData(filteredChatList)
  }

  private fun RecyclerView.addOnItemClickListener(onItemClick: (Int, View) -> Unit) {
    this.addOnChildAttachStateChangeListener(
      object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewDetachedFromWindow(view: View) {
          view.setOnClickListener(null)
        }

        override fun onChildViewAttachedToWindow(view: View) {
          view.setOnClickListener {
            val holder = getChildViewHolder(view)
            onItemClick(holder.adapterPosition, view)
          }
        }
      }
    )
  }
}
