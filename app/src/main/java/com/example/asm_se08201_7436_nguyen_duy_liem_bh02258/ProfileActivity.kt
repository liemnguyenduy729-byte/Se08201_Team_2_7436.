
package com.example.asm_se08201_7436_nguyen_duy_liem_bh02258

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText

class ProfileActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val edtName = findViewById<EditText>(R.id.edtName)
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val preferences = getSharedPreferences(
            "UserProfile",
            MODE_PRIVATE
        )

        edtName.setText(
            preferences.getString("name", "")
        )

        edtEmail.setText(
            preferences.getString("email", "")
        )

        btnSave.setOnClickListener {

            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()

            if (name.isEmpty()) {
                edtName.error = "Vui lòng nhập họ và tên"
                edtName.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty() ||
                !Patterns.EMAIL_ADDRESS.matcher(email).matches()
            ) {
                edtEmail.error = "Email không hợp lệ"
                edtEmail.requestFocus()
                return@setOnClickListener
            }

            val saved = preferences.edit()
                .putString("name", name)
                .putString("email", email)
                .commit()

            if (saved) {
                AlertDialog.Builder(this)
                    .setTitle("Thông báo")
                    .setMessage("Lưu thông tin thành công!")
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Lỗi")
                    .setMessage("Không thể lưu thông tin. Hãy thử lại.")
                    .setPositiveButton("OK", null)
                    .show()
            }
        }

        btnLogout.setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất") { _, _ ->
                    // Quay về MainActivity trong bản tích hợp tạm
                    finish()
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }
}
