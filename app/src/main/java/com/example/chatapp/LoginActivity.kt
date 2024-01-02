package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.dataclass.UserDataPayloadLogin
import com.example.chatapp.dataclass.UserLoginActivity
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.User
import com.example.chatapp.model.User.Companion.LOGGED_IN_USER_KEY
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class LoginActivity : AppCompatActivity() {

  private lateinit var edtEmail: EditText
  private lateinit var edtPassword: EditText
  private lateinit var btnLogin: Button
  private lateinit var btnSignUp: Button

  private val gson = Gson()
  val userList: MutableList<User> = mutableListOf()

  private var usernameUser = ""
  private var emailUser = ""
  private var passwordUser = ""

  private var loggedInUser: User? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    edtEmail = findViewById(R.id.edt_email)
    edtPassword = findViewById(R.id.edt_password)
    btnLogin = findViewById(R.id.btnLogin)
    btnSignUp = findViewById(R.id.btnSignUp)

    btnSignUp.setOnClickListener {
      val intent = Intent(this, SignUpActivity::class.java)
      startActivity(Intent(intent))
      finish()
    }

    btnLogin.setOnClickListener {
      val email = edtEmail.text.toString()
      val password = edtPassword.text.toString()

      val utils = Utils()
      val base64Email = utils.base64(email)
      val base64Password = utils.base64(password)

      if (email.isBlank() || password.isBlank()) {
        utils.showToast(this, "Fill the empty field")
      } else {
        if (utils.isValidEmail(email)) {
          GlobalScope.launch(
            Dispatchers.Main
          ) { // work with database - reading/writing to files / network calls
            val receivedMessageFromServer = performLogin(base64Email, base64Password)

            val status = utils.gsonResponse(receivedMessageFromServer)
            println("Status for login: $status")

            if (status == "Success") {

              try {

                val jsonElement = JsonParser.parseString(receivedMessageFromServer)
                val jsonObject =
                  jsonElement.asJsonObject.getAsJsonObject("response").getAsJsonObject("user")

                usernameUser = jsonObject.getAsJsonPrimitive("username").asString
                emailUser = jsonObject.getAsJsonPrimitive("email").asString
                passwordUser = jsonObject.getAsJsonPrimitive("password").asString

                val loggedInUser = User(usernameUser, emailUser, passwordUser)
                val json = Json.encodeToString(User.serializer(), loggedInUser)

                println("Logged in User Json - $json")

                val intentChat =
                  Intent(this@LoginActivity, Chat::class.java).apply {
                    putExtra(LOGGED_IN_USER_KEY, json)
                  }
                startActivity(intentChat)
                finish()
              } catch (e: Exception) {

                println("Error: ${e.message}")
              }
              utils.showToast(this@LoginActivity, "You have Logged in")
            } else {
              utils.showToast(this@LoginActivity, "Incorrect email or password")
            }
          }
        } else {
          utils.showToast(this, "Incorrect email")
        }
        finish()
      }
    }
  }

  private suspend fun performLogin(email: String, password: String): String {
    try {
      val utils = Utils()
      val eventType: String = "Login"
      val encodedEventType = utils.base64(eventType)
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val loginActivity =
        UserLoginActivity(
          eventType = encodedEventType,
          data = UserDataPayloadLogin(email = email, password = password)
        )

      val json = gson.toJson(loginActivity)

      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }
}
