package uz.ilhomvich.iptv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ActivationActivity : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etCode: EditText
    private lateinit var tvError: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val ticker = object : Runnable {
        override fun run() {
            val left = Session.lockRemainingSec(this@ActivationActivity)
            if (left > 0) {
                tvError.text = "Ko‘p xato urinish. $left soniyadan keyin qayta urining."
                handler.postDelayed(this, 1000)
            } else if (tvError.text.startsWith("Ko‘p")) {
                tvError.text = ""
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        if (Session.isActive(this)) {
            goMain(); return
        }
        setContentView(R.layout.activity_activation)
        etName = findViewById(R.id.etName)
        etCode = findViewById(R.id.etCode)
        tvError = findViewById(R.id.tvError)

        findViewById<Button>(R.id.btnActivate).setOnClickListener { submit() }
        findViewById<Button>(R.id.btnGetCode).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ilhomvich_sh")))
        }
        etCode.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) { submit(); true } else false
        }
        handler.post(ticker)
    }

    private fun submit() {
        val name = etName.text.toString().trim()
        val code = etCode.text.toString().trim()
        if (Session.lockRemainingSec(this) > 0) { handler.post(ticker); return }
        if (name.length < 2) { tvError.text = "Foydalanuvchi nomini kiriting"; return }
        if (code.length != 6) { tvError.text = "Kod 6 xonali bo‘lishi kerak"; return }

        if (Totp.verify(code)) {
            Session.activate(this, name)
            goMain()
        } else {
            Session.registerFail(this)
            etCode.setText("")
            tvError.text = "Kod noto‘g‘ri yoki eskirgan"
            if (Session.lockRemainingSec(this) > 0) handler.post(ticker)
        }
    }

    private fun goMain() {
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
