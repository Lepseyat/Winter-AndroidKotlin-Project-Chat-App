package com.example.chatapp.model

import com.example.chatapp.dataclass.UserData

class GroupChat(var id: Int, var name: String, var users: List<UserData>) {
  fun getIdGroupChat(): Int {
    return id
  }

  companion object {
    const val GROUP_CHAT_ID = "groupChatId"
    const val GROUP_CHAT_USERNAMES = "groupChatUsernames"
    const val GROUP_CHAT_EMAILS = "groupChatEmails"
  }
}
