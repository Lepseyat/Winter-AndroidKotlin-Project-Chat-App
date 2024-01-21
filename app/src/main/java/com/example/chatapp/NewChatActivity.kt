package com.example.chatapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.dataclass.DataRequest
import com.example.chatapp.dataclass.GroupChatData
import com.example.chatapp.dataclass.ServerRequest
import com.example.chatapp.dataclass.UserData
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.User
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class NewChatActivity : AppCompatActivity() {

  private lateinit var edtGroupName: EditText
  private lateinit var btnCreateGroupChat: Button

  private val gson = Gson()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_new_chat)

    edtGroupName = findViewById(R.id.edtGroupName)
    btnCreateGroupChat = findViewById(R.id.btnCreateGroupChat)

    val loggedInUserJson = intent?.getSerializableExtra(User.LOGGED_IN_USER_KEY) as? String ?: ""
    println("loggedInUserJson in FriendsActivity - $loggedInUserJson")

    val utils = Utils()
    var userEmail = ""

    if (loggedInUserJson.isNotEmpty()) {
      try {
        val user: User = Json.decodeFromString(User.serializer(), loggedInUserJson)
        userEmail = user.email
        println("userEmail received to friendRequestActivity - $userEmail")
      } catch (e: SerializationException) {
        println("Error decoding JSON: ${e.message}")
      }
    } else {
      println("Empty JSON string received.")
    }

    btnCreateGroupChat.setOnClickListener {
      var groupChatName = edtGroupName.text.toString()
      if (groupChatName.isBlank()) {
        utils.showToast(this, "Fill the empty field")
      } else {
        try {

          GlobalScope.launch(Dispatchers.Main) {
            println("UserEmail in button - $userEmail")

            val receivedMessageFromServer = createGroupChat(groupChatName, userEmail)

            val status = utils.gsonResponse(receivedMessageFromServer)
            println("Status for new group chat: $status")

            if (status == "Success") {

              utils.showToast(this@NewChatActivity, "Group chat created successfully")
            } else {}
          }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  private suspend fun createGroupChat(groupChatName: String, userEmail: String): String {
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
                  users = listOf(UserData(id = 0, username = "", email = userEmail, password = ""))
                )
            )
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
