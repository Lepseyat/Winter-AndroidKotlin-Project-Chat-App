package com.example.chatapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
  @SerialName("username") val username: String,
  @SerialName("email") val email: String,
  @SerialName("password") val password: String
) {
  companion object {
    const val LOGGED_IN_USER_KEY = "loggedInUser"
  }
}
