package com.example.chatapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.helpers.Utils
import com.example.chatapp.repository.UserRepo
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

  private val userRepo = UserRepo()

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
        if (utils.isValidEmail(email) && utils.isValidPassword(password)) {
          GlobalScope.launch(Dispatchers.Main) {
            val receivedMessageFromServer = userRepo.performSignUp(userName, email, password)

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
          utils.showToast(this, "Incorrect email or password")
        }
      }
    }

    btnBack.setOnClickListener {
      val intent = Intent(this, LoginActivity::class.java)
      startActivity(Intent(intent))
    }
  }
}
