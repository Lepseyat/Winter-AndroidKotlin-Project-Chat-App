package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.helpers.Utils
import com.example.chatapp.model.User
import com.example.chatapp.model.User.Companion.LOGGED_IN_USER_KEY
import com.example.chatapp.repository.LoginRepo
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

  private var usernameUser = ""
  private var emailUser = ""
  private var passwordUser = ""

  private val loginrepo = LoginRepo()

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

      if (email.isBlank() || password.isBlank()) {
        utils.showToast(this, "Fill the empty field")
      } else {
        if (utils.isValidEmail(email)) {
          GlobalScope.launch(Dispatchers.Main) {
            val receivedMessageFromServer = loginrepo.performLogin(email, password)

            val status = utils.gsonResponse(receivedMessageFromServer)
            println("Status for login: $status")

            if (status == "Success") {
              utils.showToast(this@LoginActivity, "You have logged in")
              try {
                val jsonElement = JsonParser.parseString(receivedMessageFromServer)

                if (jsonElement.isJsonObject) {
                  val jsonObject =
                    jsonElement.asJsonObject.getAsJsonObject("data")?.getAsJsonObject("user")

                  if (jsonObject != null) {
                    usernameUser = jsonObject.getAsJsonPrimitive("username")?.asString ?: ""
                    emailUser = jsonObject.getAsJsonPrimitive("email")?.asString ?: ""
                    passwordUser = jsonObject.getAsJsonPrimitive("password")?.asString ?: ""

                    val loggedInUser = User(usernameUser, emailUser, passwordUser)
                    val json = Json.encodeToString(User.serializer(), loggedInUser)

                    println("Logged in User Json - $json")

                    val intentChat =
                      Intent(this@LoginActivity, GroupChatsHomeScreenActivity::class.java).apply {
                        putExtra(LOGGED_IN_USER_KEY, json)
                      }
                    startActivity(intentChat)
                  } else {
                    println("Error in LoginActivity: 'user' object is null in JSON")
                  }
                } else {
                  println("Error in LoginActivity: JSON is not an object")
                }
              } catch (e: Exception) {
                println("Error in LoginActivity: ${e.message}")
              }
            } else {
              utils.showToast(this@LoginActivity, "Incorrect email or password")
            }
          }
        } else {
          utils.showToast(this, "Incorrect email or password")
        }
      }
    }
  }
}
