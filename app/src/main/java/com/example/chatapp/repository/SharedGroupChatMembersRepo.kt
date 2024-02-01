package com.example.chatapp.repository

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import com.example.chatapp.SocketConnection
import com.example.chatapp.dataclass.DataRequest
import com.example.chatapp.dataclass.FilterRequest
import com.example.chatapp.dataclass.FriendRequestData
import com.example.chatapp.dataclass.ServerRequest
import com.example.chatapp.dataclass.ServerResponse
import com.example.chatapp.dataclass.UserData
import com.example.chatapp.helpers.Utils
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class SharedGroupChatMembersRepo {

  private val gson = Gson()
  private val utils = Utils()

  suspend fun removeUserFromGroupChat(groupChatId: Int, email: String): String {
    try {
      val eventType: String = "RemoveUserFromGroupChat"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getFriendsAuthUser =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              id = groupChatId.toString(),
              user = UserData(id = 0, username = "", email = email, password = ""),
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

  @OptIn(DelicateCoroutinesApi::class)
  fun showFriendsDialog(context: Context, username: String, friendEmail: String, groupChatId: Int) {
    val alertDialogBuilder = AlertDialog.Builder(context)
    alertDialogBuilder.setTitle("Members")
    alertDialogBuilder.setMessage("Do you want to remove $username from your group chat?")

    println("friendEmail - $friendEmail")

    alertDialogBuilder.setPositiveButton("Remove member") { _, _ ->
      GlobalScope.launch {
        groupChatId.let {
          val removeUserFromGroupChatJson = removeUserFromGroupChat(it, friendEmail)

          val removeUserResponse =
            Json { ignoreUnknownKeys = true }
              .decodeFromString<ServerResponse>(removeUserFromGroupChatJson)

          Handler(context.mainLooper).post { utils.showToast(context, removeUserResponse.message) }
        }
      }
    }
    val alertDialog: AlertDialog = alertDialogBuilder.create()
    alertDialog.show()
  }

  @OptIn(DelicateCoroutinesApi::class)
  fun showFriendsDialog(context: Context, user: UserData, friendEmail: String, GroupChatId: Int) {
    val alertDialogBuilder = AlertDialog.Builder(context)
    alertDialogBuilder.setTitle("Friends")
    alertDialogBuilder.setMessage("Do you want to add ${user.username} to your group chat?")

    println("friendEmail - $friendEmail")

    alertDialogBuilder.setPositiveButton("Add member") { _, _ ->
      GlobalScope.launch {
        GroupChatId.let {
          val addUserToGroupChatJson = addUserToGroupChat(it, friendEmail)

          val addUserResponse = utils.ignoreUnknownKeysJson(addUserToGroupChatJson)

          withContext(Dispatchers.Main) { utils.showToast(context, addUserResponse.message) }
        }
      }
    }

    alertDialogBuilder.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

    val alertDialog: AlertDialog = alertDialogBuilder.create()
    alertDialog.show()
  }

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

  private suspend fun addUserToGroupChat(groupChatId: Int, email: String): String {
    try {
      val eventType: String = "AddUserToGroupChat"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getFriendsAuthUser =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              id = groupChatId.toString(),
              user = UserData(id = 0, username = "", email = email, password = ""),
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
}
