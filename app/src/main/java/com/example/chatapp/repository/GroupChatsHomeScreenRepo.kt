package com.example.chatapp.repository

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.SocketConnection
import com.example.chatapp.dataclass.DataRequest
import com.example.chatapp.dataclass.ServerRequest
import com.example.chatapp.dataclass.UserData
import com.google.gson.Gson

class GroupChatsHomeScreenRepo {

  private val gson = Gson()

  suspend fun getGroupChatsAuthUser(email: String): String {
    try {
      val eventType: String = "GetGroupChats"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val getGroupChats =
        ServerRequest(
          eventType = eventType,
          data = DataRequest(user = UserData(id = 0, username = "", email = email, password = "")),
        )

      val json = gson.toJson(getGroupChats)

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  companion object {
    fun addOnItemClickListener(recyclerView: RecyclerView, onItemClick: (Int, View) -> Unit) {
      recyclerView.addOnChildAttachStateChangeListener(
        object : RecyclerView.OnChildAttachStateChangeListener {
          override fun onChildViewDetachedFromWindow(view: View) {
            view.setOnClickListener(null)
          }

          override fun onChildViewAttachedToWindow(view: View) {
            view.setOnClickListener {
              val holder = recyclerView.getChildViewHolder(view)
              onItemClick(holder.adapterPosition, view)
            }
          }
        }
      )
    }
  }
}
