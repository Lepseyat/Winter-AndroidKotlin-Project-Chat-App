package com.example.chatapp.repository

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.MessageAdapter
import com.example.chatapp.R
import com.example.chatapp.SocketConnection
import com.example.chatapp.dataclass.DataRequest
import com.example.chatapp.dataclass.GroupChatData
import com.example.chatapp.dataclass.MessageData
import com.example.chatapp.dataclass.ServerRequest
import com.example.chatapp.dataclass.UserData
import com.example.chatapp.items.MessageItemDecoration
import com.example.chatapp.model.Message
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SharedChatRepo {

  private val gson = Gson()

  suspend fun sendMessageByGroupID(
    content: String,
    timestamp: String,
    attachmentURL: String,
    groupChatId: String,
    username: String,
    email: String,
  ): String {
    try {
      val eventType: String = "SendMessage"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val sendMessageByGroupID =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              id = groupChatId,
              message =
                MessageData(
                  id = 0,
                  content = content,
                  attachmentURL = attachmentURL,
                  timestamp = timestamp,
                  sender = UserData(id = 0, username = username, email = email, password = ""),
                ),
            ),
        )

      val json = gson.toJson(sendMessageByGroupID)
      println("sendMessageByGroupID json - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  suspend fun getMessagesByGroupID(groupChatId: String): String {
    try {
      val eventType: String = "GetMessages"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getGroupChatsData =
        ServerRequest(eventType = eventType, data = DataRequest(id = groupChatId))

      val json = gson.toJson(getGroupChatsData)

      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  fun parseMessagesResponse(response: String): List<Message> {
    return try {
      val responseJson = JsonParser.parseString(response).asJsonObject
      val responseObj = responseJson.getAsJsonObject("data")
      val messagesJsonElement = responseObj.get("messages") ?: responseObj.get("message")

      if (messagesJsonElement != null && messagesJsonElement.isJsonArray) {
        val messages =
          messagesJsonElement.asJsonArray.map {
            gson.fromJson(it.asJsonObject, Message::class.java)
          }

        println("Parsed messages: $messages")
        messages
      } else {
        println("Unexpected JSON structure. Response JSON: $responseJson")
        emptyList()
      }
    } catch (e: Exception) {
      e.printStackTrace()
      emptyList()
    }
  }

  fun updateRecyclerView(context: Context, messages: List<Message>, recyclerView: RecyclerView) {
    Log.d("UpdateRecyclerView", "Messages: $messages")
    val adapter = MessageAdapter(context, messages)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(context)

    val itemDecoration =
      MessageItemDecoration(
        spaceHeight = context.resources.getDimensionPixelSize(R.dimen.item_space)
      )
    recyclerView.addItemDecoration(itemDecoration)

    recyclerView.scrollToPosition(adapter.itemCount - 1)
  }

  fun fetchAndUpdateMessages(
    context: Context,
    GroupChatId: String,
    imageView: ImageView,
    textViewNoChats: TextView,
    recyclerView: RecyclerView,
  ) {
    GlobalScope.launch(Dispatchers.Main) {
      val responseGroupChatId = getMessagesByGroupID(GroupChatId)
      println("Received JSON data: $responseGroupChatId")

      val messages = parseMessagesResponse(responseGroupChatId)
      println("messages - $messages")

      updateRecyclerView(context, messages, recyclerView)

      if (messages.isEmpty()) {
        imageView.visibility = View.VISIBLE
        textViewNoChats.visibility = View.VISIBLE
      } else {
        imageView.visibility = View.GONE
        textViewNoChats.visibility = View.GONE
      }
    }
  }

  suspend fun createGroupChat(groupChatName: String, userEmail: String): String {
    try {
      val eventType: String = "CreateGroupChat"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val createGroupChat =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              groupchat =
                GroupChatData(
                  id = 0,
                  name = groupChatName,
                  users = listOf(UserData(id = 0, username = "", email = userEmail, password = "")),
                )
            ),
        )

      val json = gson.toJson(createGroupChat)
      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }
}
