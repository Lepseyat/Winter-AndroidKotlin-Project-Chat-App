package com.example.chatapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Message(
  var id: String, // Change the type to String if your JSON contains a String for id
  var content: String,
  var attachmentURL: String,
  var timestamp: String,
  var sender: SenderClass
) {
  @Serializable
  data class SenderClass(
    val id: String,
    val email: String,
    val password: String,
    val username: String
  )

  override fun toString(): String {
    return "Message(id=$id, content='$content', attachmentURL='$attachmentURL', timestamp='$timestamp', sender=$sender)"
  }
}
