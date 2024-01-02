package com.example.chatapp.model

class GroupChat(
  var id: Int,
  var name: String,
  var users: List<User>,
  var messages: List<Message> = listOf()
) {
  fun getIdGroupChat(): Int {
    return id
  }

  companion object {
    const val GROUP_CHAT_ID = "groupChatId"
  }
}
