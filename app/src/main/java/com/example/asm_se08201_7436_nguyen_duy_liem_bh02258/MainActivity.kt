package com.example.asm_se08201_7436_nguyen_duy_liem_bh02258

import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.*
import android.text.InputType
import android.app.Activity

class MainActivity : Activity() {
    private lateinit var page: LinearLayout
    private var generation = 0
    private var sessionApiKey = ""
    private val aiWorker = java.util.concurrent.Executors.newSingleThreadExecutor()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        home()
    }
    override fun onDestroy() {
        sessionApiKey=""
        aiWorker.shutdownNow()
        super.onDestroy()
    }
    private fun screen(title: String) {
        generation++
        val scroll = ScrollView(this)
        page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32,64,32,40); setBackgroundColor(Color.rgb(244,247,255)) }
        scroll.addView(page); setContentView(scroll)
        label("STUDY / MENTOR",12f)
        label(title,26f)

    }
    private fun label(text: String, size: Float = 16f): TextView = TextView(this).apply {
        this.text=text; textSize=size; setTextColor(Color.rgb(25,37,65)); setPadding(0,16,0,16); setTextIsSelectable(true); page.addView(this)
    }
    private fun input(hint: String, password: Boolean = false): EditText = EditText(this).apply {
        this.hint=hint; minHeight=56; setPadding(16,16,16,16); setSingleLine(!hint.contains("Câu hỏi")); if(password) inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD; page.addView(this)
    }
    private fun button(text: String, action: (Button)->Unit): Button = Button(this).apply { this.text=text; isAllCaps=false; minHeight=56; setTextColor(Color.WHITE); backgroundTintList=android.content.res.ColorStateList.valueOf(Color.rgb(79,70,229)); page.addView(this, LinearLayout.LayoutParams(-1,-2).apply { topMargin=12 }); setOnClickListener { action(this) } }


    private fun home() {
        screen("AI Study Mentor")

        label("Học từng bước. Hiểu sâu hơn.", 22f)

        label(
            "Đặt câu hỏi học tập, nhận hướng dẫn từ AI " +
                    "và ôn lại kiến thức đã học."
        )

        button("Hỏi AI Mentor") {
            mentor()
        }

        button("Hồ sơ cá nhân") {
            val intent = android.content.Intent(
                this,
                ProfileActivity::class.java
            )
            startActivity(intent)
        }
    }


    private fun mentor() {
        screen("AI Mentor")
        label("AI có thể trả lời sai. Hãy kiểm tra lại tài liệu học và không gửi thông tin cá nhân.")
        label("ViLao • occ/claude-opus-4-8",13f)
        val keyInput=input("Khóa API ViLao",true)
        keyInput.setText(sessionApiKey)
        keyInput.isSaveEnabled=false
        label("Khóa chỉ dùng trong phiên hiện tại, không lưu xuống máy.",13f)
        val question=input("Câu hỏi học tập của bạn"); val status=label("")
        button("Gửi câu hỏi / Thử lại") { b ->
            val key=keyInput.text.toString().trim()
            val text=question.text.toString().trim()
            if(key.isBlank()) { status.text="Nhập khóa API ViLao để tiếp tục."; return@button }
            if(text.isEmpty() || text.length>4000) { status.text="Nhập câu hỏi từ 1 đến 4000 ký tự."; return@button }
            sessionApiKey=key
            b.isEnabled=false; status.text="Mentor đang suy nghĩ…"; val current=generation
            aiWorker.execute {
                val result=runCatching { VilaoClient().ask(key,text) }
                runOnUiThread {
                    if(!isDestroyed && current==generation) {
                        status.text=result.getOrElse { e -> when(e) {
                            is java.net.SocketTimeoutException -> "Yêu cầu quá thời gian. Hãy thử lại."
                            is java.io.IOException -> "Không thể kết nối. Kiểm tra mạng rồi thử lại."
                            is IllegalStateException -> e.message ?: "Dịch vụ AI đang lỗi."
                            else -> "Không đọc được câu trả lời. Hãy thử lại."
                        } }
                        b.isEnabled=true
                    }
                }
            }
        }
        button("Trang chủ") { home() }
    }
}
