package com.example.chatapp.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class ServerRequest(
  val eventType: String,
  val data: DataRequest,
  val filter: FilterRequest? = null
)

@Serializable data class FilterRequest(var friendrequest: FriendRequestData? = null)

@Serializable
data class DataRequest(
  val id: String? = null,
  val groupchat: GroupChatData? = null,
  val user: UserData? = null,
  val message: MessageData? = null,
  val friendrequest: FriendRequestData? = null
)

@Serializable data class GroupChatData(val id: Int, val name: String, val users: List<UserData>)

@Serializable
data class UserData(val id: Int, val username: String, val email: String, val password: String)

@Serializable
data class MessageData(
  val id: Int,
  val content: String,
  val attachmentURL: String,
  val timestamp: String,
  val sender: UserData
)

@Serializable
data class FriendRequestData(
  val id: Int,
  val status: String,
  val recipient: UserData,
  val sender: UserData
)
