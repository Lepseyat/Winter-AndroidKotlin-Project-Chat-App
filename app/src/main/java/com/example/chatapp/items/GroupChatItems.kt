package com.example.chatapp.items

import com.example.chatapp.model.GroupChat

data class GroupChatItem(
  val groupchat: GroupChat,
)

// Add a method to get the group chat ID directly
private fun convertGroupChatItemToGroupChat(groupChatItem: GroupChatItem): GroupChat {
  return groupChatItem.groupchat
}
