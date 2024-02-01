package com.example.chatapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.User
import com.example.chatapp.repository.SharedChatRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class NewChatActivity : AppCompatActivity() {

  private lateinit var edtGroupName: EditText
  private lateinit var btnCreateGroupChat: Button

  private val sharedChatRepo = SharedChatRepo()

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

            val receivedMessageFromServer = sharedChatRepo.createGroupChat(groupChatName, userEmail)

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
}
