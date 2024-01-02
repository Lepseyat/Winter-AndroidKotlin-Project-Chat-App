package com.example.chatapp

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chatapp.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ProfileActivity : AppCompatActivity() {

  private lateinit var btnChat: Button
  private lateinit var btnFriends: Button

  private var username = ""
  private var email = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_profile)

    btnChat = findViewById(R.id.btnChat)
    btnFriends = findViewById(R.id.btnFriends)

    // new activity or finish
    btnChat.setOnClickListener { finish() }

    btnFriends.setOnClickListener { finish() }

    val loggedInUserJson = intent?.getSerializableExtra(User.LOGGED_IN_USER_KEY) as? String ?: ""

    if (loggedInUserJson.isNotEmpty()) {
      lifecycleScope.launch(Dispatchers.Main) {
        try {
          val user: User = Json.decodeFromString(User.serializer(), loggedInUserJson)

          username = user.username
          email = user.email

          println("ProfileActivity username - ${username} email - $email")
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }
}
