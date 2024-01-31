package com.example.chatapp.helpers

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import com.example.chatapp.dataclass.ServerResponse
import com.google.gson.Gson
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Utils {

  private val logger = LogManager.getLogger(Utils::class.java)

  companion object {
    val logger: Logger? = LogManager.getLogger(Utils::class.java)
  }

  fun isValidEmail(email: String): Boolean {
    val isValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
    logger.debug("Email validation for $email: $isValid")
    return isValid
  }

  fun isValidPassword(password: String): Boolean {
    val passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$".toRegex()
    val isValid = password.matches(passwordRegex)
    logger.debug("Password validation for $password: $isValid")
    return isValid
  }

  fun showToast(context: Context, message: CharSequence) {
    val tag = "Utils"
    val logMessage = "Showing toast: $message"

    logger.info("$tag - $logMessage")
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
  }

  fun gsonResponse(response: String): String {
    val gson = Gson()
    val jsonResponse = gson.fromJson(response, ServerResponse::class.java)
    val status = jsonResponse.status

    logger.debug("Parsing JSON response: $response, Status: $status")
    return status
  }
}
