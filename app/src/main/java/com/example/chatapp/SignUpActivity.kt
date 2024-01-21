package com.example.chatapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.dataclass.DataRequest
import com.example.chatapp.dataclass.ServerRequest
import com.example.chatapp.dataclass.UserData
import com.example.chatapp.helpers.Utils
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SignUpActivity : AppCompatActivity() {

  private lateinit var edtName: EditText
  private lateinit var edtEmail: EditText
  private lateinit var edtPassword: EditText
  private lateinit var btnSignUp: Button
  private lateinit var btnBack: Button

  private val gson = Gson()

  @SuppressLint("MissingInflatedId")
  @OptIn(DelicateCoroutinesApi::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_sign_up)

    edtName = findViewById(R.id.edt_name)
    edtEmail = findViewById(R.id.edt_email)
    edtPassword = findViewById(R.id.edt_password)
    btnSignUp = findViewById(R.id.btnSignUp)
    btnBack = findViewById(R.id.btnBack)

    btnSignUp.setOnClickListener {
      val userName = edtName.text.toString()
      val email = edtEmail.text.toString()
      val password = edtPassword.text.toString()

      val utils = Utils()

      if (userName.isBlank() || email.isBlank() || password.isBlank()) {
        utils.showToast(this, "Fill the empty field")
      } else {
        if (utils.isValidEmail(email)) {
          GlobalScope.launch(
            Dispatchers.Main
          ) { // work with database - reading/writing to files / network calls
            val receivedMessageFromServer = performSignUp(userName, email, password)

            val status = utils.gsonResponse(receivedMessageFromServer)

            if (status == "Success") {
              val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
              startActivity(intent)
              utils.showToast(this@SignUpActivity, "You have registered")
            } else {
              utils.showToast(this@SignUpActivity, "Registration process failed")
            }
          }
        } else {
          utils.showToast(this, "Incorrect email")
        }
      }
    }

    btnBack.setOnClickListener {
      val intent = Intent(this, LoginActivity::class.java)
      startActivity(Intent(intent))
    }
  }

  suspend fun performSignUp(userName: String, email: String, password: String): String {
    try {
      val eventType: String = "SignUp"
      val connection = SocketConnection()
      SocketConnection.getInstance()

      val signUpActivity =
        ServerRequest(
          eventType = eventType,
          data =
            DataRequest(
              user = UserData(id = 0, username = userName, email = email, password = password)
            )
        )

      val json = gson.toJson(signUpActivity)
      println("json string - $json")

      return connection.connectToServer(json)
    } catch (e: Exception) {
      e.printStackTrace()
      return "Connection failed: ${e.message}"
    }
  }
}
