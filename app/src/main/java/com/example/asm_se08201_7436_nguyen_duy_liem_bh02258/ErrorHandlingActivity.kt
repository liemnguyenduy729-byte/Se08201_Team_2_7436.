
package com.example.asm_se08201_7436_nguyen_duy_liem_bh02258

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context

class ErrorHandlingActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_error_handling)

        val btnNetwork = findViewById<Button>(R.id.btnNetwork)
        val btnInvalid = findViewById<Button>(R.id.btnInvalid)
        val btnTimeout = findViewById<Button>(R.id.btnTimeout)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val edtQuestion = findViewById<EditText>(R.id.edtQuestion)

        btnNetwork.setOnClickListener {
            val manager = getSystemService(
                Context.CONNECTIVITY_SERVICE
            ) as ConnectivityManager

            val network = manager.activeNetwork
            val capabilities =
                manager.getNetworkCapabilities(network)

            val connected = capabilities?.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
            ) == true

            if (connected) {
                Toast.makeText(
                    this,
                    "Thiết bị có kết nối mạng!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                showError("Không có kết nối Internet!")
            }
        }

        btnInvalid.setOnClickListener {
            val question = edtQuestion.text.toString().trim()

            if (question.isEmpty()) {
                edtQuestion.error = "Vui lòng nhập câu hỏi!"
            } else if (question.length > 4000) {
                edtQuestion.error = "Câu hỏi quá dài!"
            } else {
                Toast.makeText(
                    this,
                    "Dữ liệu hợp lệ!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        btnTimeout.setOnClickListener {
            btnTimeout.isEnabled = false

            Toast.makeText(
                this,
                "Đang kiểm tra AI...",
                Toast.LENGTH_SHORT
            ).show()

            Handler(Looper.getMainLooper()).postDelayed({
                if (!isFinishing && !isDestroyed) {
                    btnTimeout.isEnabled = true
                    showError(
                        "AI không phản hồi. Vui lòng thử lại!"
                    )
                }
            }, 3000)
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Thông báo lỗi")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}
