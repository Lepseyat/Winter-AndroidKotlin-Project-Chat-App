package com.example.chatapp

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
import com.example.chatapp.dataclass.GetGroupChatsData
import com.example.chatapp.dataclass.GroupChatId
import com.example.chatapp.dataclass.SendMessageByGroupID
import com.example.chatapp.dataclass.User
import com.example.chatapp.dataclass.Userdata
import com.example.chatapp.helpers.Utils
import com.example.chatapp.items.MessageItemDecoration
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_ID
import com.example.chatapp.model.Message
import com.example.chatapp.model.User.Companion.LOGGED_IN_USER_KEY
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date

class GroupChatActivity : AppCompatActivity() {
  private val gson = Gson()
  private val utils = Utils()

  private lateinit var editTextMessage: EditText
  private lateinit var btnSendMessage: Button
  private lateinit var imageView: ImageView
  private lateinit var textViewNoChats: TextView

  private lateinit var base64GroupChatId: String

  private val handler = Handler()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_group_chat_messages)

    btnSendMessage = findViewById(R.id.btnSendMessage)
    editTextMessage = findViewById(R.id.editTextMessage)
    imageView = findViewById(R.id.imageView)
    textViewNoChats = findViewById(R.id.textViewNoChats)

    val intent = intent
    val loggedInUserJson = intent?.getStringExtra(LOGGED_IN_USER_KEY) ?: ""
    println(" $loggedInUserJson")

    val groupChatId = intent?.getSerializableExtra(GROUP_CHAT_ID) as? Int ?: 0
    println("groupChatId in GroupChatActivity - $groupChatId")

    if (loggedInUserJson.isNotEmpty()) {

      lifecycleScope.launch(Dispatchers.Main) {
        try {
          val user: User = Json.decodeFromString(User.serializer(), loggedInUserJson)

          var username = user.username
          var email = user.email

          println("Groupchat activity username, email, password - $username, $email, $groupChatId")

          var base64Username = utils.base64(username)
          var base64Email = utils.base64(email)
          base64GroupChatId = utils.base64(groupChatId.toString())

          GlobalScope.launch(Dispatchers.Main) {
            val responseGroupChatId = getMessagesByGroupID(base64GroupChatId)
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
              utils.showToast(this@GroupChatActivity, "Fill the empty field")
            } else {
              GlobalScope.launch(Dispatchers.Main) {
                val timestamp = getCurrentTimestamp()

                var content = editTextMessage.text.toString()
                var base64Content = utils.base64(content)
                var base64Timestamp = utils.base64(timestamp)
                var attachmentURL = ""

                sendMessageByGroupID(
                  base64Content,
                  base64Timestamp,
                  attachmentURL,
                  base64GroupChatId,
                  base64Username,
                  base64Email
                )
                editTextMessage.text.clear()
              }
            }
          }
          handler.postDelayed(updateRunnable, 1000)
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
    email: String
  ): String {
    try {
      val utils = Utils()
      val eventType: String = "SendMessageByGroupID"
      val encodedEventType = utils.base64(eventType)
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val sendMessageByGroupID =
        SendMessageByGroupID(
          eventType = encodedEventType,
          Userdata(
            id = utils.base64("0"),
            content = content,
            timestamp = timestamp,
            attachmentURL = attachmentURL,
            groupchatid = groupChatId,
            User(username = username, email = email, password = "")
          )
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

  private fun parseMessagesResponse(response: String): List<Message> {
    return try {
      val gson = Gson()
      val responseJson = JsonParser.parseString(response).asJsonObject
      val responseObj = responseJson.getAsJsonObject("response")
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

    // Apply custom item decoration to reduce the gap between items
    val itemDecoration =
      MessageItemDecoration(spaceHeight = resources.getDimensionPixelSize(R.dimen.item_space))
    recyclerView.addItemDecoration(itemDecoration)

    // Scroll to the bottom
    recyclerView.scrollToPosition(adapter.itemCount - 1)
  }

  private fun getCurrentTimestamp(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
    return dateFormat.format(Date())
  }

  private fun fetchAndUpdateMessages() {
    // Fetch new messages and update RecyclerView
    GlobalScope.launch(Dispatchers.Main) {
      val responseGroupChatId = getMessagesByGroupID(base64GroupChatId)
      println("Received JSON data: $responseGroupChatId")

      val messages = parseMessagesResponse(responseGroupChatId)
      println("messages - $messages")
      updateRecyclerView(messages)
    }
  }

  override fun onDestroy() {
    // Remove the scheduled updates when the activity is destroyed
    handler.removeCallbacks(updateRunnable)
    super.onDestroy()
  }

  private val updateRunnable =
    object : Runnable {
      override fun run() {
        // Fetch and update messages
        fetchAndUpdateMessages()

        // Schedule the next update after 3 seconds
        handler.postDelayed(this, 3000)
      }
    }
}
