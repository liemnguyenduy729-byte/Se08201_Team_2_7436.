
package com.example.baitapnhom

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class AccountStore(context: Context) :
    SQLiteOpenHelper(context, "AIStudyMentor.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                salt TEXT NOT NULL,
                password_hash TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Giữ dữ liệu tài khoản cũ
    }

    private fun normalizeEmail(email: String): String {
        return email.trim().lowercase(Locale.ROOT)
    }

    private fun hashPassword(
        password: String,
        salt: ByteArray
    ): String {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            120000,
            256
        )

        return try {
            val factory = SecretKeyFactory.getInstance(
                "PBKDF2WithHmacSHA256"
            )
            val bytes = factory.generateSecret(spec).encoded
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } finally {
            spec.clearPassword()
        }
    }

    // Kiểm tra email đã được lưu hay chưa
    fun emailExists(email: String): Boolean {
        readableDatabase.query(
            "users",
            arrayOf("id"),
            "email = ?",
            arrayOf(normalizeEmail(email)),
            null,
            null,
            null
        ).use { cursor ->
            return cursor.moveToFirst()
        }
    }

    // Đăng ký tài khoản
    fun register(
        name: String,
        email: String,
        password: String
    ): Boolean {
        if (name.isBlank() ||
            email.isBlank() ||
            password.length < 8
        ) {
            return false
        }

        val normalizedEmail = normalizeEmail(email)

        if (emailExists(normalizedEmail)) {
            return false
        }

        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val values = ContentValues().apply {
            put("name", name.trim())
            put("email", normalizedEmail)
            put(
                "salt",
                Base64.encodeToString(salt, Base64.NO_WRAP)
            )
            put("password_hash", hashPassword(password, salt))
        }

        return writableDatabase.insertWithOnConflict(
            "users",
            null,
            values,
            SQLiteDatabase.CONFLICT_IGNORE
        ) != -1L
    }

    // Đăng nhập tài khoản
    fun login(
        email: String,
        password: String
    ): String? {
        readableDatabase.query(
            "users",
            arrayOf("name", "salt", "password_hash"),
            "email = ?",
            arrayOf(normalizeEmail(email)),
            null,
            null,
            null
        ).use { cursor ->

            if (!cursor.moveToFirst()) {
                return null
            }

            val name = cursor.getString(0)

            val salt = Base64.decode(
                cursor.getString(1),
                Base64.NO_WRAP
            )

            val savedHash = Base64.decode(
                cursor.getString(2),
                Base64.NO_WRAP
            )

            val enteredHash = Base64.decode(
                hashPassword(password, salt),
                Base64.NO_WRAP
            )

            return if (
                MessageDigest.isEqual(savedHash, enteredHash)
            ) {
                name
            } else {
                null
            }
        }
    }
}
