package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.Adapters.FriendsListAdapter
import com.example.chatapp.dataclass.FriendRequestData
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.User
import com.example.chatapp.model.User.Companion.LOGGED_IN_USER_KEY
import com.example.chatapp.repository.SharedFriendRequestRepo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class FriendsActivity : AppCompatActivity() {

  private lateinit var btnAddFriend: FloatingActionButton
  private lateinit var btnChat: Button
  private lateinit var btnProfile: Button
  private lateinit var txtUsername: TextView
  private lateinit var txtEmail: TextView

  private lateinit var recyclerViewFriends: RecyclerView
  private lateinit var friendsAdapter: FriendsListAdapter

  private lateinit var overlayLayout: RelativeLayout

  private val sharedFriendRequestRepo = SharedFriendRequestRepo()
  private val utils = Utils()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_friends)

    btnAddFriend = findViewById(R.id.btnAddFriend)
    btnChat = findViewById(R.id.btnChat)
    btnProfile = findViewById(R.id.btnProfile)
    txtUsername = findViewById(R.id.txtUsername)
    txtEmail = findViewById(R.id.txtEmail)

    recyclerViewFriends = findViewById(R.id.recyclerViewFriends)
    friendsAdapter = FriendsListAdapter(emptyList())
    recyclerViewFriends.layoutManager = LinearLayoutManager(this)
    recyclerViewFriends.adapter = friendsAdapter

    overlayLayout = findViewById(R.id.overlayLayout)

    val loggedInUserJson = intent?.getSerializableExtra(LOGGED_IN_USER_KEY) as? String ?: ""
    println("loggedInUserJson in FriendsActivity - $loggedInUserJson")

    var userEmail = ""

    if (loggedInUserJson.isNotEmpty()) {
      try {
        val user: User = Json.decodeFromString(User.serializer(), loggedInUserJson)
        userEmail = user.email
        println("userEmail received to friendRequestActivity - ${user.email}")
      } catch (e: SerializationException) {
        println("Error decoding JSON: ${e.message}")
      }
    } else {
      println("Empty JSON string received.")
    }

    GlobalScope.launch(Dispatchers.Main) {
      try {
        val getFriendsAuthUserJSON = sharedFriendRequestRepo.getFriendsAuthUser(userEmail)

        val responseFriends = utils.ignoreUnknownKeysJson(getFriendsAuthUserJSON)
        val friendsList: List<FriendRequestData> =
          responseFriends.data.friendrequests ?: emptyList()

        if (friendsList.isEmpty()) {
          txtUsername.visibility = View.GONE
          txtEmail.visibility = View.GONE
          recyclerViewFriends.visibility = View.GONE
          overlayLayout.visibility = View.VISIBLE
        } else {
          txtUsername.visibility = View.VISIBLE
          txtEmail.visibility = View.VISIBLE
          recyclerViewFriends.visibility = View.VISIBLE
          overlayLayout.visibility = View.GONE
          val friends = friendsList.map { it.recipient }
          friendsAdapter.updateData(friends)
          recyclerViewFriends.adapter = friendsAdapter
          println("Number of friends: ${friendsList.size}")
        }
      } catch (e: Exception) {
        e.printStackTrace()
        println("Error in getFriendRequestsAuthUser: ${e.message}")
      }
    }

    btnAddFriend.setOnClickListener {
      val intent = Intent(this, FriendRequestActivity::class.java)
      intent.putExtra(LOGGED_IN_USER_KEY, loggedInUserJson)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(Intent(intent))
    }

    btnProfile.setOnClickListener {
      val intent = Intent(this, ProfileActivity::class.java)
      intent.putExtra(LOGGED_IN_USER_KEY, loggedInUserJson)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(Intent(intent))
    }

    btnChat.setOnClickListener {
      val intent = Intent(this, GroupChatsHomeScreenActivity::class.java)
      intent.putExtra(LOGGED_IN_USER_KEY, loggedInUserJson)
      intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      startActivity(intent)
    }
  }
}
