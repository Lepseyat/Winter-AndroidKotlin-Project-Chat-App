package com.example.chatapp.dataclass

import com.example.chatapp.model.Message
import com.example.chatapp.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable data class UserSignUpData(val eventType: String, val data: UserDataResponse)

@Serializable
data class UserDataResponse(val username: String, val email: String, val password: String)

// @Serializable data class GroupChatResponse(@SerialName("response") val response: Response)

@Serializable
data class Response(
  @SerialName("status") val status: String,
  @SerialName("message") val message: String,
  @SerialName("groupchats") val groupchats: List<GroupChatResponse>
)

@Serializable
data class GroupChatResponse(
  @SerialName("id") val id: Int,
  @SerialName("name") val name: String,
  @SerialName("users") val users: List<User>,
  @SerialName("message") val message: List<Message> = listOf()
)

/*@Serializable
data class ResponseContent(
  val status: String,
  val message: String,
  val user: User? = null,
  val groupchats: List<GroupChatDataClass>? = null,
  // val messages: List<MessageDataClass>? = null,
  val friendrequests: List<FriendRequestDataClass>? = null
)*/

@Serializable data class ResponseWrapper(@SerialName("response") val response: Response)

@Serializable data class GroupChatDataClass(val groupchat: GroupChatData)

@Serializable data class GroupChatData(val id: Int, val name: String, val users: List<User>)

////////
@Serializable
data class ResponsePendingFriend(@SerialName("response") val response: FriendRequestResponse)

@Serializable
data class FriendRequestResponse(
  @SerialName("status") val status: String,
  @SerialName("message") val message: String,
  @SerialName("friendrequests") val friendRequests: List<FriendRequestDataClass>?
)

@Serializable
data class FriendRequestDataClass(val id: String, val status: String, val sender: SenderData)

// new
@Serializable
data class ResponseIncomingFriend(
  @SerialName("response") val response: IncomingFriendRequestResponse
)

@Serializable
data class IncomingFriendRequestResponse(
  @SerialName("status") val status: String,
  @SerialName("message") val message: String,
  @SerialName("friendrequests") val friendRequests: List<IncomingFriendRequestDataClass>?
)

@Serializable
data class IncomingFriendRequestDataClass(
  val id: String,
  val status: String,
  val recipient: IncomingInviteData
)

@Serializable
data class IncomingInviteData(
  val id: String,
  val email: String,
  val password: String,
  val username: String,
  val status: String? = null
)

// Friends

@Serializable
data class ResponseFriendsAuthUser(@SerialName("response") val response: FriendsResponse)

@Serializable
data class FriendsResponse(
  @SerialName("status") val status: String,
  @SerialName("message") val message: String,
  @SerialName("friendrequests") val friendRequests: List<FriendsDataClass>?
)

@Serializable
data class FriendsDataClass(
  val id: String,
  val status: String,
  val recipient: RecipientData,
  val sender: SenderData
)

@Serializable
data class RecipientData(
  val id: String,
  val email: String,
  val password: String,
  val username: String,
  val status: String? = null
)

@Serializable
data class SenderData(
  val id: String,
  val email: String,
  val password: String,
  val username: String,
  val status: String? = null
)
