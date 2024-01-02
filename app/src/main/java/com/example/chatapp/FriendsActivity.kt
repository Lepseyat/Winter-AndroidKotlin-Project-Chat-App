package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

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

    btnAddFriend.setOnClickListener {
      val intent = Intent(this, FriendRequestActivity::class.java)
      startActivity(Intent(intent))
    }

    btnProfile.setOnClickListener {
      val intent = Intent(this, ProfileActivity::class.java)
      startActivity(Intent(intent))
    }

    btnChat.setOnClickListener { finish() }
  }
}
