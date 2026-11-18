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

class MainActivity : AppCompatActivity() {
    private lateinit var page: LinearLayout
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()
    private var generation = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseApp.initializeApp(this) == null) {
            screen("AI Study Mentor")
            label("Chưa cấu hình Firebase. Thêm app/google-services.json từ Firebase Console, sau đó Sync và chạy lại.")
            return
        }
        if(auth.currentUser == null) login() else home()
    }
    private fun screen(title: String) {
        generation++
        val scroll = ScrollView(this)
        page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32,64,32,40); setBackgroundColor(Color.rgb(244,247,255)) }
        scroll.addView(page); setContentView(scroll)
        label(title,26f)
    }
    private fun label(text: String, size: Float = 16f): TextView = TextView(this).apply {
        this.text=text; textSize=size; setTextColor(Color.rgb(25,37,65)); setPadding(0,16,0,16); setTextIsSelectable(true); page.addView(this)
    }
    private fun input(hint: String, password: Boolean = false): EditText = EditText(this).apply {
        this.hint=hint; setSingleLine(!hint.contains("Câu hỏi")); if(password) inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD; page.addView(this)
    }
    private fun button(text: String, action: (Button)->Unit): Button = Button(this).apply { this.text=text; page.addView(this); setOnClickListener { action(this) } }
    private fun error(e: Exception) { Toast.makeText(this,"Thao tác thất bại. Kiểm tra kết nối, thông tin nhập và cấu hình dịch vụ rồi thử lại.",Toast.LENGTH_LONG).show() }
    private fun login(register: Boolean = false) {
        screen(if(register) "Tạo tài khoản" else "AI Study Mentor")
        label("Người bạn đồng hành học tập của bạn")
        val name=if(register) input("Họ và tên") else null
        val email=input("Email"); val pass=input("Mật khẩu",true)
        button(if(register) "Đăng ký" else "Đăng nhập") { b ->
            val mail=email.text.toString().trim(); val password=pass.text.toString()
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(mail).matches() || password.length<6 || (register && name!!.text.isBlank())) { label("Nhập tên, email hợp lệ và mật khẩu tối thiểu 6 ký tự."); return@button }
            b.isEnabled=false
            val task=if(register) auth.createUserWithEmailAndPassword(mail,password) else auth.signInWithEmailAndPassword(mail,password)
            task.addOnSuccessListener {
                if(register) auth.currentUser!!.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name!!.text.toString().trim()).build()).addOnCompleteListener { home() }
                else home()
            }.addOnFailureListener { b.isEnabled=true; error(it) }
        }
        button(if(register) "Đã có tài khoản" else "Tạo tài khoản mới") { login(!register) }
    }
    private fun home() {
        screen("Xin chào, "+(auth.currentUser?.displayName ?: "bạn"))
        label("Hôm nay bạn muốn tìm hiểu điều gì?")
        button("Hỏi AI Mentor") { mentor() }; button("Lịch sử học tập") { history() }; button("Hồ sơ cá nhân") { profile() }
    }
    private fun mentor() {
        screen("AI Mentor")
        label("AI có thể trả lời sai. Hãy kiểm tra lại tài liệu học và không gửi thông tin cá nhân.")
        val question=input("Câu hỏi học tập của bạn"); val status=label("")
        button("Gửi câu hỏi / Thử lại") { b ->
            val text=question.text.toString().trim()
            if(text.isEmpty() || text.length>4000) { status.text="Nhập câu hỏi từ 1 đến 4000 ký tự."; return@button }
            b.isEnabled=false; status.text="Mentor đang suy nghĩ…"; val current=generation
            FirebaseFunctions.getInstance().getHttpsCallable("askMentor").call(mapOf("question" to text)).addOnSuccessListener { result ->
                if(current==generation) { status.text=(result.data as? Map<*,*>)?.get("answer")?.toString() ?: "Không có câu trả lời."; b.isEnabled=true }
            }.addOnFailureListener { if(current==generation) { status.text="Không thể nhận câu trả lời. Kiểm tra mạng rồi thử lại."; b.isEnabled=true } }
        }
        button("Trang chủ") { home() }
    }
    private fun history() {
        screen("Lịch sử học tập"); val current=generation; val status=label("Đang tải…")
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
        screen("Hồ sơ cá nhân"); label(auth.currentUser?.email ?: "")
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
