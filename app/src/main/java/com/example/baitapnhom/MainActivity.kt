
package com.example.baitapnhom

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val edtEmail = findViewById<EditText>(
            R.id.edtLoginEmail
        )

        val edtPassword = findViewById<EditText>(
            R.id.edtLoginPassword
        )

        val btnLogin = findViewById<Button>(
            R.id.btnLogin
        )

        val btnRegister = findViewById<Button>(
            R.id.btnGoRegister
        )

        val database = AccountStore(this)

        btnLogin.setOnClickListener {

            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this,
                    "Vui lòng nhập email và mật khẩu",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val name = database.login(email, password)

                if (name != null) {
                    Toast.makeText(
                        this,
                        "Đăng nhập thành công!",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(
                        this@MainActivity,
                        HomeActivity::class.java
                    )

                    intent.putExtra("USER_NAME", name)
                    startActivity(intent)

                } else {
                    Toast.makeText(
                        this,
                        "Email hoặc mật khẩu không đúng!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        btnRegister.setOnClickListener {
            val intent = Intent(
                this@MainActivity,
                RegisterActivity::class.java
            )
            startActivity(intent)
        }
    }
}
