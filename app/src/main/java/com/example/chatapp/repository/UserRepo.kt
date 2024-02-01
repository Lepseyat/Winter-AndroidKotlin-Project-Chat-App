package com.example.chatapp.repository

import com.example.chatapp.SocketConnection
import com.example.chatapp.dataclass.DataRequest
import com.example.chatapp.dataclass.ServerRequest
import com.example.chatapp.dataclass.UserData
import com.google.gson.Gson

class UserRepo {

  private val gson = Gson()

  suspend fun performSignUp(userName: String, email: String, password: String): String {
    try {
      val eventType: String = "SignUp"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val signUpActivity =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              user = UserData(id = 0, username = userName, email = email, password = password)
            ),
        )

      val json = gson.toJson(signUpActivity)
      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }

  suspend fun performLogin(email: String, password: String): String {
    try {
      val eventType: String = "Login"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val loginActivity =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(user = UserData(id = 0, username = "", email = email, password = password)),
        )

      val json = gson.toJson(loginActivity)

      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }
}
