package com.example.chatapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_EMAILS
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_ID
import com.example.chatapp.model.GroupChat.Companion.GROUP_CHAT_USERNAMES
import com.example.chatapp.model.User
import com.example.chatapp.model.User.Companion.LOGGED_IN_USER_KEY
import com.example.chatapp.repository.SharedChatRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ChatActivity : AppCompatActivity() {
  private val chatRepo = SharedChatRepo()
  private val utils = Utils()
  private val handler = Handler()

  private lateinit var editTextMessage: EditText
  private lateinit var btnSendMessage: Button
  private lateinit var btnShowMembers: Button
  private lateinit var imageView: ImageView
  private lateinit var textViewNoChats: TextView
  private lateinit var recyclerViewMessages: RecyclerView
  private val context: Context = this

  private lateinit var GroupChatId: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_group_chat_messages)

    btnSendMessage = findViewById(R.id.btnSendMessage)
    btnShowMembers = findViewById(R.id.btnShowMembers)
    editTextMessage = findViewById(R.id.editTextMessage)
    imageView = findViewById(R.id.imageView)
    textViewNoChats = findViewById(R.id.textViewNoChats)
    recyclerViewMessages = findViewById(R.id.recyclerViewMessages)

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
            val responseGroupChatId = chatRepo.getMessagesByGroupID(GroupChatId)
            println("Received JSON data: $responseGroupChatId")

            val messages = chatRepo.parseMessagesResponse(responseGroupChatId)
            println("messages - $messages")

            if (messages.isEmpty()) {
              imageView.visibility = View.VISIBLE
              textViewNoChats.visibility = View.VISIBLE
            } else {
              imageView.visibility = View.GONE
              textViewNoChats.visibility = View.GONE
            }
            chatRepo.updateRecyclerView(context, messages, recyclerViewMessages)
          }

          btnSendMessage.setOnClickListener {
            var textMessage = editTextMessage.toString()
            if (textMessage.isBlank()) {
              utils.showToast(this@ChatActivity, "Fill the empty field")
            } else {
              GlobalScope.launch(Dispatchers.Main) {
                val timestamp = utils.getCurrentTimestamp()

                var content = editTextMessage.text.toString()
                var attachmentURL = ""

                chatRepo.sendMessageByGroupID(
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

  override fun onDestroy() {
    handler.removeCallbacks(updateRunnable)
    super.onDestroy()
  }

  private val updateRunnable =
    object : Runnable {
      override fun run() {
        chatRepo.fetchAndUpdateMessages(
          context,
          GroupChatId,
          imageView,
          textViewNoChats,
          recyclerViewMessages,
        )

        handler.postDelayed(this, 5000)
      }
    }
}
