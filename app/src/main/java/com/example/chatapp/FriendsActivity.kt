package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.model.User.Companion.LOGGED_IN_USER_KEY

class FriendsActivity : AppCompatActivity() {

  private lateinit var btnAddFriend: Button
  private lateinit var btnChat: Button
  private lateinit var btnProfile: Button

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_friends)

    btnAddFriend = findViewById(R.id.btnAddFriend)
    btnChat = findViewById(R.id.btnChat)
    btnProfile = findViewById(R.id.btnProfile)

    val loggedInUserJson = intent?.getSerializableExtra(LOGGED_IN_USER_KEY) as? String ?: ""
    println("loggedInUserJson in FriendsActivity - $loggedInUserJson")

    btnAddFriend.setOnClickListener {
      val intent = Intent(this, FriendRequestActivity::class.java)
      intent.putExtra(LOGGED_IN_USER_KEY, loggedInUserJson)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(Intent(intent))
    }

    btnProfile.setOnClickListener {
      val intent = Intent(this, ProfileActivity::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(Intent(intent))
    }

    btnChat.setOnClickListener {
      val intent = Intent(this, Chat::class.java)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(intent)
    }
  }
}
