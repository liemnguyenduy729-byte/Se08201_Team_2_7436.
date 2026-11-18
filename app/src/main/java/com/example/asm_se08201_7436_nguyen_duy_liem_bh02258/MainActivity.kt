package com.example.asm_se08201_7436_nguyen_duy_liem_bh02258

import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.*
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.FirebaseNetworkException

class MainActivity : AppCompatActivity() {
    private lateinit var page: LinearLayout
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()
    private var generation = 0
    private var preview = false
    private var sessionApiKey = ""
    private val aiWorker = java.util.concurrent.Executors.newSingleThreadExecutor()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseApp.initializeApp(this) == null) {
            preview = true
            home()
            return
        }
        if(auth.currentUser == null) login() else home()
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
    private fun error(e: Exception) { val message = when(e) {
        is FirebaseNetworkException -> "Không có kết nối mạng. Kiểm tra mạng rồi thử lại."
        is FirebaseAuthInvalidCredentialsException -> "Email hoặc mật khẩu không đúng."
        is FirebaseAuthUserCollisionException -> "Email đã được sử dụng. Hãy đăng nhập."
        else -> "Thao tác thất bại. Vui lòng thử lại."
    }; Toast.makeText(this,message,Toast.LENGTH_LONG).show() }
    private fun login(register: Boolean = false) {
        screen(if(register) "Tạo tài khoản" else "AI Study Mentor")
        label("Người bạn đồng hành học tập của bạn")
        val name=if(register) input("Họ và tên") else null
        val email=input("Email"); val pass=input("Mật khẩu",true)
        button(if(register) "Đăng ký" else "Đăng nhập") { b ->
            val mail=email.text.toString().trim(); val password=pass.text.toString()
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches() || password.length<6 || (register && (name!!.text.isBlank() || name.text.length>100))) { label("Nhập tên, email hợp lệ và mật khẩu tối thiểu 6 ký tự."); return@button }
            b.isEnabled=false
            val task=if(register) auth.createUserWithEmailAndPassword(mail,password) else auth.signInWithEmailAndPassword(mail,password)
            task.addOnSuccessListener {
                if(register) {
                    val fullName=name!!.text.toString().trim()
                    auth.currentUser!!.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(fullName).build()).addOnSuccessListener {
                        db.collection("users").document(auth.uid!!).set(mapOf("userId" to auth.uid,"fullName" to fullName,"email" to mail,"updatedAt" to FieldValue.serverTimestamp())).addOnSuccessListener { home() }.addOnFailureListener { error(it); home() }
                    }.addOnFailureListener { error(it); home() }
                }
                else home()
            }.addOnFailureListener { b.isEnabled=true; error(it) }
        }
        button(if(register) "Đã có tài khoản" else "Tạo tài khoản mới") { login(!register) }
    }
    private fun home() {
        screen("Xin chào, "+(if(preview) "bạn" else auth.currentUser?.displayName ?: "bạn"))
        label("Học từng bước. Hiểu sâu hơn.",22f)
        label("Đặt câu hỏi học tập, nhận hướng dẫn từ AI và ôn lại kiến thức đã học.")
        button("Hỏi AI Mentor") { mentor() }; button("Lịch sử học tập") { history() }; button("Hồ sơ cá nhân") { profile() }
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
    private fun history() {
        screen("Lịch sử học tập"); val current=generation; val status=label("Đang tải…")
        if(preview) { status.text="Chưa có lịch sử. Câu hỏi và câu trả lời sẽ xuất hiện tại đây sau khi kết nối tài khoản."; button("Trang chủ") { home() }; return }
        db.collection("learningHistory").whereEqualTo("userId",auth.uid).limit(100).get().addOnSuccessListener { result ->
            if(current!=generation) return@addOnSuccessListener
            status.text=if(result.isEmpty) "Chưa có câu hỏi nào." else "Các câu hỏi gần đây"
            result.documents.sortedByDescending { it.getTimestamp("createdAt")?.seconds ?: 0 }.forEach { doc ->
                label(doc.getString("question") ?: "",20f); label(doc.getString("answer") ?: "")
            }
        }.addOnFailureListener { if(current==generation) status.text="Không tải được lịch sử. Vui lòng thử lại." }
        button("Tải lại") { history() }; button("Trang chủ") { home() }
    }
    private fun profile() {
        screen("Hồ sơ cá nhân")
        if(preview) { label("Thông tin cá nhân sẽ hiển thị sau khi bạn đăng nhập."); button("Trang chủ") { home() }; return }
        label(auth.currentUser?.email ?: "")
        val name=input("Họ và tên"); name.setText(auth.currentUser?.displayName)
        button("Lưu hồ sơ") { b ->
            val value=name.text.toString().trim()
            if(value.isEmpty() || value.length>100) { label("Tên cần có từ 1 đến 100 ký tự."); return@button }
            b.isEnabled=false
            auth.currentUser!!.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(value).build()).addOnSuccessListener {
                db.collection("users").document(auth.uid!!).set(mapOf("userId" to auth.uid,"fullName" to value,"email" to auth.currentUser!!.email,"updatedAt" to FieldValue.serverTimestamp())).addOnSuccessListener { b.isEnabled=true; label("Đã lưu hồ sơ.") }.addOnFailureListener { b.isEnabled=true; error(it) }
            }.addOnFailureListener { b.isEnabled=true; error(it) }
        }
        button("Đăng xuất") { auth.signOut(); login() }; button("Trang chủ") { home() }
    }
}
