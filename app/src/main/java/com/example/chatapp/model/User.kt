package com.example.chatapp.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
  @SerialName("username") var username: String,
  @SerialName("email") var email: String,
  @SerialName("password") var password: String
) {

  fun clearSensitiveInformation() {
    username = ""
    email = ""
    password = ""
  }

  companion object {
    const val LOGGED_IN_USER_KEY = "loggedInUser"
  }
}
