
package com.example.baitapnhom

import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val edtName = findViewById<EditText>(R.id.edtName)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtConfirm = findViewById<EditText>(R.id.edtConfirm)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnBack = findViewById<Button>(R.id.btnBack)

        val database = AccountStore(this)

        btnRegister.setOnClickListener {

            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString()
            val confirm = edtConfirm.text.toString()

            when {
                name.isEmpty() ||
                        email.isEmpty() ||
                        password.isEmpty() ||
                        confirm.isEmpty() -> {
                    Toast.makeText(
                        this,
                        "Vui lòng nhập đầy đủ thông tin",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(
                        this,
                        "Email không hợp lệ",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                password.length < 8 -> {
                    Toast.makeText(
                        this,
                        "Mật khẩu phải có ít nhất 8 ký tự",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                password != confirm -> {
                    Toast.makeText(
                        this,
                        "Mật khẩu xác nhận không khớp",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    try {
                        if (database.emailExists(email)) {
                            Toast.makeText(
                                this,
                                "Email này đã được đăng ký!",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val success = database.register(
                                name,
                                email,
                                password
                            )

                            if (success) {
                                Toast.makeText(
                                    this,
                                    "Đăng ký thành công!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Không thể lưu tài khoản. Vui lòng thử lại!",
                                    Toast.LENGTH_LONG
                                ).show()
                            }git init
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "RegisterActivity",
                            "Lỗi đăng ký tài khoản",
                            e
                        )

                        Toast.makeText(
                            this,
                            "Đăng ký gặp lỗi. Vui lòng thử lại!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
