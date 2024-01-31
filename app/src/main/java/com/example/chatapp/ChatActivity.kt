package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.MessageAdapter
import com.example.chatapp.dataclass.DataRequest
import com.example.chatapp.dataclass.MessageData
import com.example.chatapp.dataclass.ServerRequest
import com.example.chatapp.dataclass.UserData
import com.example.chatapp.helpers.Utils
import com.example.chatapp.items.MessageItemDecoration
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_EMAILS
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_ID
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_USERNAMES
import com.example.chatapp.model.Message
import com.example.chatapp.model.User
import com.example.chatapp.model.User.Companion.LOGGED_IN_USER_KEY
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date

class ChatActivity : AppCompatActivity() {
  private val gson = Gson()
  private val utils = Utils()

  private lateinit var editTextMessage: EditText
  private lateinit var btnSendMessage: Button
  private lateinit var btnShowMembers: Button
  private lateinit var imageView: ImageView
  private lateinit var textViewNoChats: TextView

  private lateinit var GroupChatId: String

  private val handler = Handler()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_group_chat_messages)

    btnSendMessage = findViewById(R.id.btnSendMessage)
    btnShowMembers = findViewById(R.id.btnShowMembers)
    editTextMessage = findViewById(R.id.editTextMessage)
    imageView = findViewById(R.id.imageView)
    textViewNoChats = findViewById(R.id.textViewNoChats)

    val usernames = intent.getStringArrayListExtra(GROUP_CHAT_USERNAMES)
    val userEmails = intent.getStringArrayListExtra(GROUP_CHAT_EMAILS)

    val intent = intent
    val loggedInUserJson = intent?.getStringExtra(LOGGED_IN_USER_KEY) ?: ""
    println(" $loggedInUserJson")

    val groupChatId = intent?.getSerializableExtra(GROUP_CHAT_ID) as? Int ?: 0
    GroupChatId = groupChatId.toString()
    println("groupChatId in GroupChatActivity - $groupChatId")

    btnShowMembers.setOnClickListener {
      val intent = Intent(this, GroupChatMembersActivity::class.java)
      intent.putExtra(LOGGED_IN_USER_KEY, loggedInUserJson)
      intent.putExtra(GROUP_CHAT_ID, groupChatId)
      intent.putExtra(GROUP_CHAT_USERNAMES, usernames?.let { it1 -> ArrayList(it1) })
      intent.putExtra(GROUP_CHAT_EMAILS, userEmails?.let { it2 -> ArrayList(it2) })
      startActivity(intent)
    }

    if (loggedInUserJson.isNotEmpty()) {

      lifecycleScope.launch(Dispatchers.Main) {
        try {
          val user: User = Json.decodeFromString(User.serializer(), loggedInUserJson)

          var username = user.username
          var email = user.email
          var GroupChatId = groupChatId.toString()

          println("Groupchat activity username, email, password - $username, $email, $groupChatId")

          GlobalScope.launch(Dispatchers.Main) {
            val responseGroupChatId = getMessagesByGroupID(GroupChatId)
            println("Received JSON data: $responseGroupChatId")

            val messages = parseMessagesResponse(responseGroupChatId)
            println("messages - $messages")

            if (messages.isEmpty()) {
              imageView.visibility = View.VISIBLE
              textViewNoChats.visibility = View.VISIBLE
            } else {
              imageView.visibility = View.GONE
              textViewNoChats.visibility = View.GONE
            }
            updateRecyclerView(messages)
          }

          btnSendMessage.setOnClickListener {
            var textMessage = editTextMessage.toString()
            if (textMessage.isBlank()) {
              utils.showToast(this@ChatActivity, "Fill the empty field")
            } else {
              GlobalScope.launch(Dispatchers.Main) {
                val timestamp = getCurrentTimestamp()

                var content = editTextMessage.text.toString()
                var attachmentURL = ""

                sendMessageByGroupID(
                  content,
                  timestamp,
                  attachmentURL,
                  GroupChatId,
                  username,
                  email,
                )
                editTextMessage.text.clear()
              }
            }
          }
          handler.postDelayed(updateRunnable, 6000)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  private suspend fun sendMessageByGroupID(
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

  private suspend fun getMessagesByGroupID(groupChatId: String): String {
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

  private fun parseMessagesResponse(response: String): List<Message> {
    return try {
      val gson = Gson()
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

  private fun updateRecyclerView(messages: List<Message>) {
    Log.d("UpdateRecyclerView", "Messages: $messages")
    val adapter = MessageAdapter(this, messages)
    val recyclerView: RecyclerView = findViewById(R.id.recyclerViewMessages)
    recyclerView.adapter = adapter
    recyclerView.layoutManager = LinearLayoutManager(this)

    val itemDecoration =
      MessageItemDecoration(spaceHeight = resources.getDimensionPixelSize(R.dimen.item_space))
    recyclerView.addItemDecoration(itemDecoration)

    recyclerView.scrollToPosition(adapter.itemCount - 1)
  }

  private fun getCurrentTimestamp(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
    return dateFormat.format(Date())
  }

  private fun fetchAndUpdateMessages() {
    GlobalScope.launch(Dispatchers.Main) {
      val responseGroupChatId = getMessagesByGroupID(GroupChatId)
      println("Received JSON data: $responseGroupChatId")

      val messages = parseMessagesResponse(responseGroupChatId)
      println("messages - $messages")
      updateRecyclerView(messages)
      if (messages.isEmpty()) {
        imageView.visibility = View.VISIBLE
        textViewNoChats.visibility = View.VISIBLE
      } else {
        imageView.visibility = View.GONE
        textViewNoChats.visibility = View.GONE
      }
    }
  }

  override fun onDestroy() {
    handler.removeCallbacks(updateRunnable)
    super.onDestroy()
  }

  private val updateRunnable =
    object : Runnable {
      override fun run() {
        // Fetch and update messages
        fetchAndUpdateMessages()

        handler.postDelayed(this, 3000)
      }
    }
}
