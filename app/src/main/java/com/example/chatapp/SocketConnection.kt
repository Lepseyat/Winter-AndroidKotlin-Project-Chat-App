package com.example.chatapp

import com.example.chatapp.helpers.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class SocketConnection constructor() {
  companion object SocketSingleton {
    private const val SERVER_ADDRESS = "10.0.2.2"

    // private const val SERVER_ADDRESS = "192.168.10.103"

    private const val PORT = 8081

    @Volatile private var instance: SocketConnection? = null

    fun getInstance(): SocketConnection {
      return instance
        ?: synchronized(this) { instance ?: SocketConnection().also { instance = it } }
    }
  }

  suspend fun connectToServer(message: String): String =
    withContext(Dispatchers.IO) {
      try {
        Socket(SERVER_ADDRESS, PORT).use { socket ->
          BufferedWriter(OutputStreamWriter(socket.getOutputStream())).use { writer ->
            BufferedReader(InputStreamReader(socket.getInputStream())).use { reader ->
              Utils.logger.info("Sending data: $message")

              // Use the instance to send the message
              writer.write(message)
              writer.newLine()
              writer.flush()

              val serverResponse = reader.readLine()
              println("Received from server: $serverResponse")

              serverResponse
            }
          }
        }
      } catch (e: Exception) {
        e.printStackTrace()
        "Connection failed"
      }
    }
}
