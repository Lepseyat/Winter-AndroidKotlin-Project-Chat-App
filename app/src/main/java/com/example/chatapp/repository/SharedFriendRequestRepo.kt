package com.example.chatapp.repository

import android.app.AlertDialog
import android.content.Context
import com.example.chatapp.SocketConnection
import com.example.chatapp.dataclass.DataRequest
import com.example.chatapp.dataclass.FilterRequest
import com.example.chatapp.dataclass.FriendRequestData
import com.example.chatapp.dataclass.ServerRequest
import com.example.chatapp.dataclass.UserData
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SharedFriendRequestRepo {

  private val gson = Gson()

  suspend fun getFriendsAuthUser(email: String): String {
    try {
      val eventType: String = "GetFriendRequests"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getFriendsAuthUser =
        ServerRequest(
          eventType = eventType,
          data = DataRequest(user = UserData(id = 0, username = "", email = email, password = "")),
          filter =
            FilterRequest(
              FriendRequestData(
                id = 0,
                status = "Accepted",
                sender = UserData(id = 0, username = "", email = email, password = ""),
                recipient = UserData(id = 0, username = "", email = email, password = ""),
              )
            ),
        )

      val json = gson.toJson(getFriendsAuthUser)
      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  suspend fun performFriendRequest(senderEmail: String, emailRecipient: String): String {
    try {
      val eventType: String = "SendFriendRequest"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val friendRequestActivity =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              friendrequest =
                FriendRequestData(
                  id = 0,
                  status = "Pending",
                  sender = UserData(id = 0, username = "", email = senderEmail, password = ""),
                  recipient = UserData(id = 0, username = "", email = emailRecipient, password = ""),
                )
            ),
        )

      val json = gson.toJson(friendRequestActivity)

      println("performFriendRequest json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  suspend fun getFriendRequestsAuthUser(email: String): String {
    try {
      val eventType: String = "GetFriendRequests"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getFriendRequestsAuthUser =
        ServerRequest(
          eventType = eventType,
          data = DataRequest(user = UserData(id = 0, username = "", email = email, password = "")),
          filter =
            FilterRequest(
              FriendRequestData(
                id = 0,
                status = "Pending",
                sender = UserData(id = 0, username = "", email = email, password = ""),
                recipient = UserData(id = 0, username = "", email = "", password = ""),
              )
            ),
        )

      val json = gson.toJson(getFriendRequestsAuthUser)

      println("getFriendRequestsAuthUser - json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  suspend fun getIncomingFriendRequests(email: String): String {
    try {
      val eventType: String = "GetFriendRequests"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getIncomingFriendRequests =
        ServerRequest(
          eventType = eventType,
          data = DataRequest(user = UserData(id = 0, username = "", email = email, password = "")),
          filter =
            FilterRequest(
              FriendRequestData(
                id = 0,
                status = "Pending",
                sender = UserData(id = 0, username = "", email = "", password = ""),
                recipient = UserData(id = 0, username = "", email = email, password = ""),
              )
            ),
        )

      val json = gson.toJson(getIncomingFriendRequests)

      println("getIncomingFriendRequests - json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  fun showFriendRequestDialog(context: Context, user: UserData, friendid: Int) {
    val alertDialogBuilder = AlertDialog.Builder(context)
    alertDialogBuilder.setTitle("Friend Request")
    alertDialogBuilder.setMessage("Do you want to add ${user.username} to your friends list?")

    alertDialogBuilder.setNegativeButton("Reject") { _, _ ->
      GlobalScope.launch { rejectFriendRequest(friendid) }
    }
    alertDialogBuilder.setPositiveButton("Accept") { _, _ ->
      GlobalScope.launch { acceptFriendRequest(friendid) }
    }

    val alertDialog: AlertDialog = alertDialogBuilder.create()
    alertDialog.show()
  }

  private suspend fun acceptFriendRequest(friendRequestId: Int): String {
    try {
      val eventType: String = "FriendRequestOperation"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getIncomingFriendRequests =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              friendrequest =
                FriendRequestData(
                  id = friendRequestId,
                  status = "Accepted",
                  sender = UserData(id = 0, username = "", email = "", password = ""),
                  recipient = UserData(id = 0, username = "", email = "", password = ""),
                )
            ),
        )

      val json = gson.toJson(getIncomingFriendRequests)

      println("getIncomingFriendRequests - json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  private suspend fun rejectFriendRequest(friendRequestId: Int): String {
    try {
      val eventType: String = "FriendRequestOperation"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getIncomingFriendRequests =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              friendrequest =
                FriendRequestData(
                  id = friendRequestId,
                  status = "Rejected",
                  sender = UserData(id = 0, username = "", email = "", password = ""),
                  recipient = UserData(id = 0, username = "", email = "", password = ""),
                )
            ),
        )

      val json = gson.toJson(getIncomingFriendRequests)

      println("getIncomingFriendRequests - json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }
}
