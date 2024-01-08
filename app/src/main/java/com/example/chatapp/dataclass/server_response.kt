package com.example.chatapp.dataclass

import com.example.chatapp.model.Message
import com.example.chatapp.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class UserSignUpData(val eventType: String, val data: UserDataResponse)

@Serializable
data class UserDataResponse(val username: String, val email: String, val password: String)

// @Serializable data class GroupChatResponse(@SerialName("response") val response: Response)

@Serializable data class ResponseWrapper(@SerialName("response") val response: Response)

@Serializable
data class Response(
  @SerialName("status") val status: String,
  @SerialName("message") val message: String,
  @SerialName("groupchats") val groupchats: List<GroupChatResponse>
)

// @Serializable data class GroupChatItem(@SerialName("groupchat") val groupchat: GroupChat)

@Serializable
data class GroupChatResponse(
  @SerialName("id") val id: Int,
  @SerialName("name") val name: String,
  @SerialName("users") val users: List<User>,
  @SerialName("message") val message: List<Message> = listOf()
)

@Serializable
data class ResponseContent(
  val status: String,
  val message: String,
  val user: User? = null,
  val groupchats: List<GroupChatDataClass>? = null,
  // val messages: List<MessageDataClass>? = null,
  val friendrequests: List<FriendRequestDataClass>? = null
)

@Serializable data class GroupChatDataClass(val groupchat: GroupChatData)

@Serializable data class GroupChatData(val id: Int, val name: String, val users: List<User>)

@Serializable data class FriendRequestDataClass(val friendrequest: FriendRequestData)

@Serializable data class FriendRequestData(val id: Int, val status: String, val recipient: User)
