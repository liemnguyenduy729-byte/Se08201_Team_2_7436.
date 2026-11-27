package com.example.asm_se08201_7436_nguyen_duy_liem_bh02258

import android.content.SharedPreferences
import android.util.Base64
import android.util.Patterns
import org.json.JSONObject
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

internal data class StudentAccount(val id: String, val name: String, val email: String)

internal class StudentAccounts(private val prefs: SharedPreferences) {
    private fun record(id: String) = prefs.getString("student:$id", null)?.let(::JSONObject)

    private fun JSONObject.student() = StudentAccount(getString("id"), getString("name"), getString("email"))

    fun active(): StudentAccount? = prefs.getString("activeStudent", null)?.let { record(it)?.student() }

    fun setActive(id: String?) {
        prefs.edit().putString("activeStudent", id).apply()
    }

    @Synchronized
    fun register(name: String, email: String, password: String): StudentAccount {
        val fullName = validatedName(name)
        val address = validatedEmail(email)
        validateNewPassword(password)
        require(!prefs.contains("email:$address")) { "Email này đã được đăng ký trên thiết bị." }
        val id = UUID.randomUUID().toString()
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val record = JSONObject().put("id", id).put("name", fullName).put("email", address)
            .put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .put("hash", Base64.encodeToString(hash(password, salt), Base64.NO_WRAP))
        check(prefs.edit().putString("student:$id", record.toString()).putString("email:$address", id).commit()) {
            "Không thể lưu tài khoản trên thiết bị."
        }
        return record.student()
    }

    fun signIn(email: String, password: String): StudentAccount? {
        if (password.length !in 1..128) return null
        val address = validatedEmail(email)
        val id = prefs.getString("email:$address", null) ?: return null
        val record = record(id) ?: return null
        return if (record.matchesPassword(password)) record.student() else null
    }

    @Synchronized
    fun changePassword(id: String, current: String, replacement: String) {
        require(current.length in 1..128) { "Nhập mật khẩu hiện tại hợp lệ." }
        validateNewPassword(replacement)
        require(current != replacement) { "Hãy chọn mật khẩu mới khác mật khẩu hiện tại." }
        val record = requireNotNull(record(id))
        require(record.matchesPassword(current)) { "Mật khẩu hiện tại chưa đúng." }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        record.put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .put("hash", Base64.encodeToString(hash(replacement, salt), Base64.NO_WRAP))
        check(prefs.edit().putString("student:$id", record.toString()).commit()) {
            "Không thể lưu mật khẩu mới."
        }
    }

    private fun JSONObject.matchesPassword(password: String): Boolean {
        val salt = Base64.decode(getString("salt"), Base64.NO_WRAP)
        val expectedHash = Base64.decode(getString("hash"), Base64.NO_WRAP)
        return MessageDigest.isEqual(hash(password, salt), expectedHash)
    }

    @Synchronized
    fun update(student: StudentAccount): StudentAccount {
        val fullName = validatedName(student.name)
        val address = validatedEmail(student.email)
        val record = requireNotNull(record(student.id))
        val previousEmail = record.getString("email")
        val owner = prefs.getString("email:$address", null)
        require(owner == null || owner == student.id) { "Email này đã được đăng ký trên thiết bị." }
        record.put("name", fullName).put("email", address)
        val edit = prefs.edit().putString("student:${student.id}", record.toString())
        if (address != previousEmail) {
            edit.remove("email:$previousEmail").putString("email:$address", student.id)
        }
        check(edit.commit()) { "Không thể lưu hồ sơ trên thiết bị." }
        return record.student()
    }

    private fun validatedName(name: String) = name.trim().also {
        require(it.length in 1..80) { "Nhập họ và tên từ 1 đến 80 ký tự." }
    }

    private fun validatedEmail(email: String) = email.trim().lowercase(Locale.ROOT).also {
        require(it.length <= 254 && Patterns.EMAIL_ADDRESS.matcher(it).matches()) { "Nhập email hợp lệ." }
    }

    private fun validateNewPassword(password: String) {
        require(password.length in 8..128 && password.isNotBlank()) { "Mật khẩu cần từ 8 đến 128 ký tự." }
    }

    private fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 600_000, 256)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
