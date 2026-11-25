package com.example.asm_se08201_7436_nguyen_duy_liem_bh02258

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.*
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val ink = Color.rgb(13, 24, 57)
    private val muted = Color.rgb(65, 83, 128)
    private val blue = Color.rgb(28, 86, 240)
    private val paper = Color.rgb(246, 248, 255)
    private val tint = Color.rgb(232, 238, 254)
    private val white = Color.WHITE

    private enum class Screen { WELCOME, LOGIN, REGISTER, HOME, PROFILE }
    private lateinit var root: LinearLayout
    private lateinit var page: LinearLayout
    private var screen = Screen.WELCOME
    private var account: StudentAccount? = null
    private val worker = Executors.newCachedThreadPool()
    private val prefs by lazy { getSharedPreferences("study_mentor", MODE_PRIVATE) }
    private val accounts by lazy { StudentAccounts(prefs) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        window.statusBarColor = paper
        window.navigationBarColor = paper
        account = accounts.active()
        show(if (account == null) Screen.WELCOME else Screen.HOME)
    }
    override fun onDestroy() {
        worker.shutdownNow()
        super.onDestroy()
    }

    @Deprecated("Back navigation is handled by the Activity")
    override fun onBackPressed() {
        if (screen == Screen.HOME || screen == Screen.WELCOME) super.onBackPressed() else back()
    }

    private fun back() = show(when (screen) {
        Screen.LOGIN -> Screen.WELCOME
        Screen.REGISTER -> Screen.LOGIN
        else -> Screen.HOME
    })

    private fun dp(n: Int) = (n * resources.displayMetrics.density + .5f).toInt()
    private fun shape(color: Int, radius: Int = 20) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radius).toFloat()
    }

    private fun gradient(start: Int, end: Int, radius: Int = 24) =
        GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(start, end)).apply {
            cornerRadius = dp(radius).toFloat()
        }

    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun row() = LinearLayout(this).apply {
        gravity = Gravity.CENTER_VERTICAL
        isBaselineAligned = false
    }
    private fun LinearLayout.add(view: View, gap: Int = 0, height: Int = -2) {
        addView(view, LinearLayout.LayoutParams(-1, if (height < 0) height else dp(height)).apply {
            topMargin = dp(gap)
        })
    }

    private fun LinearLayout.equal(view: View, gap: Int = 0) {
        addView(view, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(gap) })
    }

    private fun View.inset(horizontal: Int, vertical: Int) =
        setPadding(dp(horizontal), dp(vertical), dp(horizontal), dp(vertical))

    private fun label(value: CharSequence, size: Int = 16, color: Int = ink, bold: Boolean = false) =
        TextView(this).apply {
            text = value
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }

    private fun action(value: String, active: Boolean = true, run: () -> Unit) =
        label(value, 15, if (active) white else blue, true).apply {
            gravity = Gravity.CENTER
            minHeight = dp(48)
            inset(12, 10)
            background = if (active) gradient(0xff397cfa.toInt(), blue, 18) else shape(tint, 16)
            setOnClickListener { run() }
        }

    private fun card(color: Int = white) = column().apply {
        background = shape(color, 20)
        inset(16, 14)
        elevation = dp(2).toFloat()
    }

    private fun title(value: String, backButton: Boolean = true) {
        val bar = row()
        val heading = label(value, 21, bold = true).apply { gravity = Gravity.CENTER }
        if (backButton) bar.addView(label("‹", 32, bold = true).apply {
            gravity = Gravity.CENTER
            contentDescription = "Quay lại"
            setOnClickListener { back() }
        }, dp(44), dp(48))
        bar.addView(heading, LinearLayout.LayoutParams(0, dp(52), 1f))
        if (backButton) bar.addView(View(this), dp(44), dp(48))
        page.add(bar)
    }

    private fun show(destination: Screen) {
        screen = destination
        root = column().apply { setBackgroundColor(paper) }
        root.setOnApplyWindowInsetsListener { view, insets ->
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                val padding = insets.getInsets(WindowInsets.Type.systemBars() or WindowInsets.Type.displayCutout() or WindowInsets.Type.ime())
                view.setPadding(padding.left, padding.top, padding.right, padding.bottom)
            }
            insets
        }
        if (android.os.Build.VERSION.SDK_INT < 30) root.fitsSystemWindows = true
        page = column().apply {
            setPadding(dp(20), dp(8), dp(20), dp(24))
            isFocusableInTouchMode = true
        }
        val scroll = ScrollView(this).apply {
            isFillViewport = true
            clipToPadding = false
            addView(page)
        }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        when (destination) {
            Screen.WELCOME -> welcome()
            Screen.LOGIN, Screen.REGISTER -> auth(destination == Screen.REGISTER)
            Screen.HOME -> home()
            Screen.PROFILE -> profile()
        }
        if (destination == Screen.HOME || destination == Screen.PROFILE) nav()
    }

    private fun nav() {
        val navigation = row().apply { setBackgroundColor(white); inset(8, 8) }
        listOf(Screen.HOME to "⌂\nTrang chủ", Screen.PROFILE to "♙\nHồ sơ")
            .forEach { (target, name) ->
                navigation.equal(label(name, 13, if (target == screen) blue else muted, target == screen).apply {
                    gravity = Gravity.CENTER
                    minHeight = dp(48)
                    if (target == screen) background = shape(tint, 14)
                    setOnClickListener { show(target) }
                }, 3)
            }
        root.add(navigation)
    }
    private fun centered(value: String, size: Int, color: Int = ink, bold: Boolean = false) =
        label(value, size, color, bold).apply { gravity = Gravity.CENTER }

    private fun image(resource: Int) = ImageView(this).apply {
        setImageResource(resource)
        scaleType = ImageView.ScaleType.CENTER_CROP
        background = shape(tint)
        clipToOutline = true
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    private fun welcome() {
        page.gravity = Gravity.CENTER
        val panel = card().apply { background = gradient(0xff66b8f5.toInt(), 0xff071e48.toInt(), 30) }
        panel.add(centered("🎓", 42))
        panel.add(centered("StudyAI Mentor", 31, bold = true), 4)
        panel.add(image(R.drawable.mentor_hero), 20, 230)
        panel.add(centered("Học dễ hơn\ncùng AI", 30, white, true), 16)
        panel.add(centered("Giải đáp, giải thích và đồng hành\ntrong quá trình học tập của bạn", 15, white), 10)
        panel.add(action("Bắt đầu  →") {
            show(Screen.LOGIN)
        }, 24)
        page.add(panel)
    }

    private fun auth(register: Boolean) {
        title(if (register) "Đăng ký" else "Đăng nhập")
        page.add(centered("StudyAI Mentor", 28, bold = true), 20)
        val form = card()
        val name = if (register) form.field("Họ và tên", "Nhập họ và tên") else null
        val email = form.field("Email", "Ví dụ: hocsinh@example.com").apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val password = form.field("Mật khẩu", if (register) "Ít nhất 8 ký tự" else "Nhập mật khẩu").apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            isSaveEnabled = false
        }
        val status = label("", 14, muted)
        form.add(status, 8)
        lateinit var submit: TextView
        submit = action(if (register) "Tạo tài khoản" else "Đăng nhập") {
            val fullName = name?.text?.toString()?.trim().orEmpty()
            val address = email.text.toString().trim().lowercase(java.util.Locale.ROOT)
            val secret = password.text.toString()
            status.text = when {
                register && fullName.length !in 1..80 -> "Nhập họ và tên từ 1 đến 80 ký tự."
                address.length > 254 || !android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches() -> "Nhập email hợp lệ."
                register && (secret.length !in 8..128 || secret.isBlank()) -> "Mật khẩu cần từ 8 đến 128 ký tự."
                secret.isEmpty() || secret.length > 128 -> "Nhập mật khẩu hợp lệ."
                else -> ""
            }
            if (status.text.isNotEmpty()) return@action
            submit.isEnabled = false
            status.text = "Đang xử lý…"
            worker.execute {
                val result = runCatching {
                    if (register) accounts.register(fullName, address, secret) else accounts.signIn(address, secret)
                }
                runOnUiThread {
                    if (isDestroyed || isFinishing || !form.isAttachedToWindow) return@runOnUiThread
                    val student = result.getOrNull()
                    if (student == null) {
                        status.text = result.exceptionOrNull()?.message ?: "Email hoặc mật khẩu chưa đúng."
                        submit.isEnabled = true
                    } else {
                        password.text.clear()
                        accounts.setActive(student.id)
                        account = student
                        getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(root.windowToken, 0)
                        show(Screen.HOME)
                    }
                }
            }
        }
        form.add(submit, 12)
        page.add(form, 16)
        page.add(action(if (register) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký", false) {
            show(if (register) Screen.LOGIN else Screen.REGISTER)
        }, 12)
        page.add(centered("Tài khoản được lưu trên thiết bị này.", 13, muted), 12)
    }

    private fun LinearLayout.field(name: String, hint: String, initial: String = ""): EditText {
        add(label(name, 15, bold = true), 12)
        return input(hint, initial).apply { add(this, 6) }
    }
    private fun listItem(icon: String, name: String, detail: String, run: () -> Unit): View = row().apply {
        background = shape(white, 18)
        elevation = dp(2).toFloat()
        inset(14, 12)
        addView(centered(icon, 22, blue, true).apply { background = shape(tint, 12) }, dp(44), dp(44))
        addView(column().apply {
            add(label(name, 16, bold = true))
            if (detail.isNotEmpty()) add(label(detail, 13, muted), 3)
        }, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(12); marginEnd = dp(8) })
        addView(label("›", 22, blue, true))
        setOnClickListener { run() }
    }

    private fun home() {
        val header = card().apply { background = gradient(0xff051d42.toInt(), 0xff2159c2.toInt()) }
        val greeting = row()
        greeting.addView(image(R.drawable.mentor_avatar), dp(46), dp(46))
        greeting.addView(column().apply {
            add(label("Xin chào, ${account?.name.orEmpty()} 👋", 21, white, true))
            add(label("Hôm nay bạn muốn học gì?", 14, white), 3)
        }, LinearLayout.LayoutParams(0, -2, 1f).apply { marginStart = dp(12) })
        header.add(greeting, 12)
        page.add(header)
        page.add(listItem("♙", "Hồ sơ cá nhân", "Xem và chỉnh sửa thông tin") { show(Screen.PROFILE) }, 10)
    }

    private fun input(hintText: String, initial: String = "", multiline: Boolean = false) =
        EditText(this).apply {
            hint = hintText
            setText(initial)
            setTextColor(ink)
            setHintTextColor(muted)
            textSize = 15f
            inputType = InputType.TYPE_CLASS_TEXT or if (multiline) InputType.TYPE_TEXT_FLAG_MULTI_LINE else 0
            gravity = if (multiline) Gravity.TOP else Gravity.CENTER_VERTICAL
            background = shape(tint, 16)
            inset(14, 12)
        }

    private fun profile() {
        title("Hồ sơ cá nhân", false)
        val student = requireNotNull(account)
        val cover = card().apply {
            background = gradient(0xff80d9ee.toInt(), 0xffd4f9ea.toInt())
            gravity = Gravity.CENTER_HORIZONTAL
            addView(image(R.drawable.mentor_avatar), dp(84), dp(84))
            add(centered(student.name, 25, bold = true), 12)
            add(centered(student.email, 15, muted), 4)
        }
        page.add(cover, 8)
        val form = card()
        val name = form.field("Họ và tên", "Nhập họ và tên", student.name)
        val email = form.field("Email", "Nhập email", student.email).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
        val status = label("", 14, muted)
        form.add(status, 8)
        form.add(action("Lưu hồ sơ") {
            val fullName = name.text.toString().trim()
            val address = email.text.toString().trim().lowercase(java.util.Locale.ROOT)
            status.text = when {
                fullName.length !in 1..80 -> "Nhập họ và tên từ 1 đến 80 ký tự."
                address.length > 254 || !android.util.Patterns.EMAIL_ADDRESS.matcher(address).matches() -> "Nhập email hợp lệ."
                else -> ""
            }
            if (status.text.isNotEmpty()) return@action
            runCatching { accounts.update(student.copy(name = fullName, email = address)) }.onSuccess { updated ->
                account = updated
                getSystemService(InputMethodManager::class.java).hideSoftInputFromWindow(root.windowToken, 0)
                show(Screen.PROFILE)
                Toast.makeText(this, "Đã lưu hồ sơ.", Toast.LENGTH_SHORT).show()
            }.onFailure {
                status.text = it.message ?: "Không thể lưu hồ sơ."
            }
        }, 8)
        page.add(form, 16)
        page.add(action("Đổi mật khẩu", false) { changePassword(student.id) }, 14)
        page.add(action("Đăng xuất", false) {
            accounts.setActive(null)
            account = null
            show(Screen.LOGIN)
        }, 14)
    }

    private fun changePassword(owner: String) {
        val form = column().apply { inset(22, 8) }
        val fields = listOf("Mật khẩu hiện tại", "Mật khẩu mới", "Nhập lại mật khẩu mới").map { title ->
            form.field(title, title).apply {
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                isSaveEnabled = false
            }
        }
        val status = label("", 14, muted)
        form.add(status, 8)
        val scroll = ScrollView(this).apply { addView(form) }
        val dialog = AlertDialog.Builder(this).setTitle("Đổi mật khẩu").setView(scroll)
            .setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create()
        dialog.setOnShowListener {
            val save = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            save.setOnClickListener {
                val (current, replacement, confirmation) = fields.map { it.text.toString() }
                if (current.length !in 1..128 || replacement != confirmation) {
                    status.text = "Kiểm tra mật khẩu hiện tại và phần nhập lại mật khẩu mới."
                    return@setOnClickListener
                }
                save.isEnabled = false
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = false
                dialog.setCancelable(false)
                worker.execute {
                    val result = runCatching { accounts.changePassword(owner, current, replacement) }
                    runOnUiThread {
                        if (isDestroyed || !dialog.isShowing) return@runOnUiThread
                        result.onSuccess {
                            fields.forEach { it.text.clear() }
                            dialog.dismiss()
                            Toast.makeText(this, "Đã đổi mật khẩu.", Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            status.text = it.message ?: "Không thể đổi mật khẩu."
                            save.isEnabled = true
                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).isEnabled = true
                            dialog.setCancelable(true)
                        }
                    }
                }
            }
        }
        dialog.show()
    }
}

