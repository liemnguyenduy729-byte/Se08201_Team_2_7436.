
package com.example.baitapnhom

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        val txtWelcome = findViewById<TextView>(
            R.id.txtWelcome
        )

        val btnLogout = findViewById<Button>(
            R.id.btnLogout
        )

        val name = intent.getStringExtra("USER_NAME")
            ?: "Người dùng"

        txtWelcome.text = "Xin chào, $name!"

        btnLogout.setOnClickListener {

            val intent = Intent(
                this,
                MainActivity::class.java
            )

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }
}
