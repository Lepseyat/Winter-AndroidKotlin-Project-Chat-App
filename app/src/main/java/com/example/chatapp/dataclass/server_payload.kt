package com.example.chatapp.dataclass

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable data class UserSignUpActivity(val eventType: String, val data: UserDataPayload)

@Serializable
data class UserDataPayload(val username: String, val email: String, val password: String)

@Serializable data class UserLoginActivity(val eventType: String, val data: UserDataPayloadLogin)

@Serializable data class UserDataPayloadLogin(val email: String, val password: String)

@Serializable
data class UserFriendRequestActivity(val eventType: String, val data: UserFriendRequest)

@Serializable data class UserFriendRequest(val emailSender: String?, val emailRecipient: String)

@Serializable data class GetGroupChats(val eventType: String, val data: UserChats)

@Serializable data class UserChats(val email: String)

@Serializable data class GetGroupChatsData(val eventType: String, val data: GroupChatId)

@Serializable data class GroupChatId(val id: String)

@Serializable data class SendMessageByGroupID(val eventType: String, val data: Userdata)

@Serializable
data class Userdata(
  val id: String?,
  val content: String,
  val timestamp: String,
  val attachmentURL: String,
  val groupchatid: String,
  val sender: User
)

@Serializable data class User(val username: String, val email: String, val password: String)

// @Serializable data class GroupChatDataClass(val groupchat: GroupChatData)

// @Serializable data class GroupChatData(val id: Int, val name: String, val users: List<UserData>)

// @Serializable data class UserData(val username: String, val email: String, val password: String)

// @Serializable data class FriendRequestDataClass(val friendrequest: FriendRequestData)

// @Serializable
// data class FriendRequestData(val id: Int, val status: String, val recipient: UserData)

data class ResponseData(
  @SerializedName("status") val status: String,
  @SerializedName("message") val message: String
)

data class JsonResponse(@SerializedName("response") val response: ResponseData)
