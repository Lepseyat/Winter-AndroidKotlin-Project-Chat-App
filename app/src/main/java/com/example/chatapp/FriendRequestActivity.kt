package com.example.chatapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.dataclass.UserFriendRequest
import com.example.chatapp.dataclass.UserFriendRequestActivity
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.User
import com.example.chatapp.model.User.Companion.LOGGED_IN_USER_KEY
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class FriendRequestActivity : AppCompatActivity() {

  private lateinit var edtEmail: EditText
  private lateinit var btnAddFriend: Button
  private val gson = Gson()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_friend_request)

    edtEmail = findViewById(R.id.edt_email)
    btnAddFriend = findViewById(R.id.btnAddFriend)

    val loggedInUserJson = intent?.getSerializableExtra(LOGGED_IN_USER_KEY) as? String ?: ""
    println("loggedInUserJson in ProfileActivity - $loggedInUserJson")

    btnAddFriend.setOnClickListener {
      val email = edtEmail.text.toString()

      val utils = Utils()
      val base64EmailReceiver = utils.base64(email)

      if (loggedInUserJson.isNotEmpty()) {
        try {
          val user: User = Json.decodeFromString(User.serializer(), loggedInUserJson)
          val userEmail = user.email
          println("userEmail received to friendRequestActivity - ${user.email}")

          if (email.isBlank()) {
            utils.showToast(this, "Fill the empty field")
          } else {
            if (utils.isValidEmail(email)) {
              GlobalScope.launch(
                Dispatchers.Main
              ) { // work with database - reading/writing to files / network calls
                //   var userEmail: String = processLoggedInUser(loggedInUser = null)

                println("UserEmail in button - $userEmail")

                val base64EmailSender = utils.base64(userEmail)

                val receivedMessageFromServer =
                  performFriendRequest(base64EmailSender, base64EmailReceiver)

                val status = utils.gsonResponse(receivedMessageFromServer)
                println("Status for friend request: $status")

                if (status == "Success") {

                  utils.showToast(this@FriendRequestActivity, "Friend request sent")
                } else {
                  utils.showToast(this@FriendRequestActivity, "Email doesn't exist")
                }
              }
            } else {
              utils.showToast(this, "Incorrect email")
            }
          }
        } catch (e: SerializationException) {
          println("Error decoding JSON: ${e.message}")
        }
      } else {
        println("Empty JSON string received.")
      }
    }
  }

  private suspend fun performFriendRequest(senderEmail: String, emailRecipient: String): String {
    try {
      val utils = Utils()
      val eventType: String = "SendFriendRequest"
      val encodedEventType = utils.base64(eventType)
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val friendRequestActivity =
        UserFriendRequestActivity(
          eventType = encodedEventType,
          data = UserFriendRequest(emailSender = senderEmail, emailRecipient = emailRecipient)
        )

      val json = gson.toJson(friendRequestActivity)

      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }
}
