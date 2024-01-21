package com.example.chatapp.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class ServerResponse(val status: String, val message: String, val data: DataResponse)

@Serializable
data class DataResponse(
  val user: UserData? = null,
  val groupchats: List<GroupChatData>? = null,
  val messages: List<MessageData>? = null,
  val friendrequests: List<FriendRequestData>? = null
)
