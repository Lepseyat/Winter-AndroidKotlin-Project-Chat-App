package com.example.chatapp.model

import com.example.chatapp.helpers.Utils
import kotlinx.serialization.Serializable

@Serializable
data class Message(
  var id: String, // Change the type to String if your JSON contains a String for id
  var content: String,
  var attachmentURL: String,
  var timestamp: String,
  var sender: YourSenderClass
) {
  @Serializable
  data class YourSenderClass(
    val id: String,
    val email: String,
    val password: String,
    val username: String
  )

  fun base64EncodeMessage(): Array<String> {
    return arrayOf(
      Utils.base64(id),
      Utils.base64(content),
      // Don't encode the attachmentURL, since it's already done via filePicker
      attachmentURL,
      Utils.base64(timestamp),
    )
  }

  override fun toString(): String {
    return "Message(id=$id, content='$content', attachmentURL='$attachmentURL', timestamp='$timestamp', sender=$sender)"
  }
}
