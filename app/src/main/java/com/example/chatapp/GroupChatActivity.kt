package com.example.chatapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.MessageAdapter
import com.example.chatapp.dataclass.GetGroupChatsData
import com.example.chatapp.dataclass.GroupChatId
import com.example.chatapp.helpers.Utils
import com.example.chatapp.items.MessageItemDecoration
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_ID
import com.example.chatapp.model.Message
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class GroupChatActivity : AppCompatActivity() {
  private val gson = Gson()
  private val utils = Utils

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_group_chat_messages)

    val groupChatId = intent?.getSerializableExtra(GROUP_CHAT_ID) as? Int ?: 0
    println("groupChatId in GroupChatActivity - $groupChatId")

    val base64GroupChatId = utils.base64(groupChatId.toString())

    GlobalScope.launch(Dispatchers.Main) {
      val responseGroupChatId = getMessagesByGroupID(base64GroupChatId)
      println("Received JSON data: $responseGroupChatId")

      val messages = parseMessagesResponse(responseGroupChatId)

      println("messages - $messages")

      updateRecyclerView(messages)
    }
  }

  private suspend fun getMessagesByGroupID(groupChatId: String): String {
    try {
      val utils = Utils()
      val eventType: String = "GetMessagesByGroupID"
      val encodedEventType = utils.base64(eventType)
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getGroupChatsData =
        GetGroupChatsData(eventType = encodedEventType, GroupChatId(id = groupChatId))

      val json = gson.toJson(getGroupChatsData)

      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  /*private suspend fun sendMessageByGroupID(groupChatId: String): String {
    try {
      val utils = Utils()
      val eventType: String = "SendMessageByGroupID"
      val encodedEventType = utils.base64(eventType)
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val sendMessageByGroupID = SendMessageByGroupID(eventType = encodedEventType, data(id = groupChatId, content = ))

      val json = gson.toJson(getGroupChats)

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }*/

  private fun parseMessagesResponse(response: String): List<Message> {
    return try {
      val gson = Gson()
      val messagesJsonArray =
        JsonParser.parseString(response)
          .asJsonObject
          .getAsJsonObject("response")
          .getAsJsonArray("messages")

      val messages =
        messagesJsonArray.map {
          gson.fromJson(it.asJsonObject.getAsJsonObject("message"), Message::class.java)
        }

      println("Parsed messages: $messages")

      messages
    } catch (e: Exception) {
      e.printStackTrace()
      emptyList()
    }
  }

  private fun updateRecyclerView(messages: List<Message>) {
    Log.d("UpdateRecyclerView", "Messages: $messages")
    val adapter = MessageAdapter(this, messages)
    val recyclerView: RecyclerView = findViewById(R.id.recyclerViewMessages)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)

    // Apply custom item decoration to reduce the gap between items
    val itemDecoration =
      MessageItemDecoration(spaceHeight = resources.getDimensionPixelSize(R.dimen.item_space))
    recyclerView.addItemDecoration(itemDecoration)

    // Scroll to the bottom
    recyclerView.scrollToPosition(adapter.itemCount - 1)
  }
}
