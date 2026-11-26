package com.example.asm_se08201_7436_nguyen_duy_liem_bh02258

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudentAccountsTest {
    @Test
    fun accountLifecycleKeepsIdentityAndUpdatesCredentials() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = "account_test_${UUID.randomUUID()}"
        val prefs = context.getSharedPreferences(file, Context.MODE_PRIVATE)
        val accounts = StudentAccounts(prefs)
        try {
            val original = accounts.register("  Test Student  ", " Student@Example.com ", "OldPassword1")
            assertEquals("Test Student", original.name)
            assertEquals(original, accounts.signIn("STUDENT@example.com", "OldPassword1"))
            assertThrows(IllegalArgumentException::class.java) {
                accounts.register("Another Student", "STUDENT@example.com", "Password123")
            }

            accounts.setActive(original.id)
            val updated = accounts.update(original.copy(name = "Updated Student", email = " New@Example.com "))
            assertEquals(original.id, updated.id)
            assertEquals(updated, StudentAccounts(prefs).active())
            assertNull(accounts.signIn("student@example.com", "OldPassword1"))
            assertThrows(IllegalArgumentException::class.java) {
                accounts.changePassword(updated.id, "WrongPassword", "NewPassword2")
            }
            accounts.changePassword(updated.id, "OldPassword1", "NewPassword2")
            assertNull(accounts.signIn("new@example.com", "OldPassword1"))
            assertEquals(updated, accounts.signIn("new@example.com", "NewPassword2"))

            accounts.setActive(null)
            assertNull(StudentAccounts(prefs).active())
            assertEquals(updated, accounts.signIn("new@example.com", "NewPassword2"))
        } finally {
            context.deleteSharedPreferences(file)
        }
    }
}
