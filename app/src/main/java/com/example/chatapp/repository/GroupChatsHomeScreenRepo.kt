package com.example.chatapp.repository

import android.app.Activity
import android.content.Intent
import android.view.View
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.GroupChatAdapter
import com.example.chatapp.ChatActivity
import com.example.chatapp.SocketConnection
import com.example.chatapp.dataclass.DataRequest
import com.example.chatapp.dataclass.ServerRequest
import com.example.chatapp.dataclass.UserData
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.GroupChat
import com.example.chatapp.model.User
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupChatsHomeScreenRepo {

  private val gson = Gson()
  private val utils = Utils()

  private lateinit var filteredGroupChats: MutableList<GroupChat>

  suspend fun getGroupChatsAuthUser(email: String): String {
    try {
      val eventType: String = "GetGroupChats"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getGroupChats =
        ServerRequest(
          eventType = eventType,
          data = DataRequest(user = UserData(id = 0, username = "", email = email, password = "")),
        )

      val json = gson.toJson(getGroupChats)

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  companion object {
    fun addOnItemClickListener(recyclerView: RecyclerView, onItemClick: (Int, View) -> Unit) {
      recyclerView.addOnChildAttachStateChangeListener(
        object : RecyclerView.OnChildAttachStateChangeListener {
          override fun onChildViewDetachedFromWindow(view: View) {
            view.setOnClickListener(null)
          }

          override fun onChildViewAttachedToWindow(view: View) {
            view.setOnClickListener {
              val holder = recyclerView.getChildViewHolder(view)
              onItemClick(holder.adapterPosition, view)
            }
          }
        }
      )
    }
  }

  suspend fun updateUIWithGroupChats(
    groupChats: List<GroupChat>,
    groupChatList: MutableList<GroupChat>,
    editTextSearch: EditText,
    groupChatAdapter: GroupChatAdapter,
  ) {
    withContext(Dispatchers.Main) {
      groupChatList.clear()
      groupChatList.addAll(groupChats)

      filteredGroupChats =
        groupChatList
          .filter { chat -> chat.name.contains(editTextSearch.text.toString(), ignoreCase = true) }
          .toMutableList()

      println("FilteredGroupChats size in updateUIWithGroupChats: ${filteredGroupChats.size}")
      groupChatAdapter.updateData(filteredGroupChats)
    }
  }

  private fun updateRecyclerView(groupChats: List<GroupChat>, groupChatAdapter: GroupChatAdapter) {
    groupChatAdapter.updateData(groupChats)
  }

  fun setItemClickListeners(
    activity: Activity,
    recyclerView: RecyclerView,
    loggedInUserJson: String,
  ) {
    addOnItemClickListener(recyclerView) { position, _ ->
      val selectedGroupChat = filteredGroupChats.getOrNull(position)

      println("Position: $position")
      println("FilteredGroupChats size: ${filteredGroupChats.size}")
      println("SelectedGroupChat: $selectedGroupChat")

      if (selectedGroupChat != null) {
        val groupChatId = selectedGroupChat.getIdGroupChat()

        val intent = Intent(activity.applicationContext, ChatActivity::class.java)
        intent.putExtra(GroupChat.GROUP_CHAT_ID, groupChatId)
        intent.putExtra(User.LOGGED_IN_USER_KEY, loggedInUserJson)

        val usernames = selectedGroupChat.users.map { it.username }
        intent.putStringArrayListExtra(GroupChat.GROUP_CHAT_USERNAMES, ArrayList(usernames))

        val userEmails = selectedGroupChat.users.map { it.email }
        intent.putStringArrayListExtra(GroupChat.GROUP_CHAT_EMAILS, ArrayList(userEmails))

        activity.startActivity(intent)
      }
    }
  }

  fun filterChatList(
    query: String,
    groupChatList: MutableList<GroupChat>,
    groupChatAdapter: GroupChatAdapter,
    filteredGroupChats: MutableList<GroupChat>,
  ) {
    filteredGroupChats.clear()
    filteredGroupChats.addAll(
      groupChatList.filter { chat -> chat.name.contains(query, ignoreCase = true) }
    )
    groupChatAdapter.updateData(filteredGroupChats)
  }

  suspend fun fetchAndUpdateGroupChats(
    userEmail: String,
    groupChatList: MutableList<GroupChat>,
    editTextSearch: EditText,
    groupChatAdapter: GroupChatAdapter,
    filteredGroupChats: MutableList<GroupChat>,
  ) {
    GlobalScope.launch(Dispatchers.Main) {
      try {
        val responseGroupChatId = getGroupChatsAuthUser(userEmail)
        println("Received JSON data for group chats: $responseGroupChatId")

        val groupChats =
          utils.ignoreUnknownKeysJson(responseGroupChatId).data.groupchats?.map { groupChatResponse
            ->
            GroupChat(
              id = groupChatResponse.id,
              name = groupChatResponse.name,
              users = groupChatResponse.users,
            )
          } ?: emptyList()

        updateUIWithGroupChats(groupChats, groupChatList, editTextSearch, groupChatAdapter)

        filterChatList(
          editTextSearch.text.toString(),
          groupChatList,
          groupChatAdapter,
          filteredGroupChats,
        )

        updateRecyclerView(filteredGroupChats, groupChatAdapter)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}
