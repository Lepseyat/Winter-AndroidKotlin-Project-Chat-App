package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.chatapp.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ProfileActivity : AppCompatActivity() {

  private lateinit var btnChat: Button
  private lateinit var btnFriends: Button
  private lateinit var usernameTextView: TextView
  private lateinit var emailTextView: TextView
  private lateinit var signOutButton: Button

  private var username = ""
  private var email = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_profile)

    btnChat = findViewById(R.id.btnChat)
    btnFriends = findViewById(R.id.btnFriends)
    usernameTextView = findViewById(R.id.usernameTextView)
    emailTextView = findViewById(R.id.emailTextView)
    signOutButton = findViewById(R.id.signOutButton)

    val loggedInUserJson = intent?.getSerializableExtra(User.LOGGED_IN_USER_KEY) as? String ?: ""
    println("loggedInUserJson in FriendsActivity - $loggedInUserJson")

    btnChat.setOnClickListener {
      val intent = Intent(this, GroupChatsHomeScreenActivity::class.java)
      intent.putExtra(User.LOGGED_IN_USER_KEY, loggedInUserJson)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(intent)
    }

    btnFriends.setOnClickListener {
      val intent = Intent(this, FriendsActivity::class.java)
      intent.putExtra(User.LOGGED_IN_USER_KEY, loggedInUserJson)
      startActivity(intent)
      onPause()
    }

    signOutButton.setOnClickListener {
      val loggedInUser = intent?.getSerializableExtra(User.LOGGED_IN_USER_KEY) as? User
      loggedInUser?.clearSensitiveInformation()

      val intent = Intent(this, LoginActivity::class.java)
      startActivity(intent)
      finish()
    }

    if (loggedInUserJson.isNotEmpty()) {
      lifecycleScope.launch(Dispatchers.Main) {
        try {
          val user: User = Json.decodeFromString(User.serializer(), loggedInUserJson)

          username = user.username
          email = user.email

          usernameTextView.text = username
          emailTextView.text = email

          println("ProfileActivity username - ${username} email - $email")
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }
}
