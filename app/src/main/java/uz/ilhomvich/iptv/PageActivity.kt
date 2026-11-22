package uz.ilhomvich.iptv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

/** Mening obunalarim / Akkaunt / Yordam / Haqida sahifalari. */
class PageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Session.isActive(this)) {
            startActivity(Intent(this, ActivationActivity::class.java)); finish(); return
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_page)
        val title = findViewById<TextView>(R.id.tvPageTitle)
        val body = findViewById<TextView>(R.id.tvBody)
        val box = findViewById<LinearLayout>(R.id.buttons)
        findViewById<Button>(R.id.btnClose).setOnClickListener { finish() }

        fun addButton(text: String, onClick: () -> Unit) {
            val b = Button(this)
            b.text = text
            b.setOnClickListener { onClick() }
            box.addView(b, LinearLayout.LayoutParams(-1, -2))
        }
        fun openTelegram() =
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/ilhomvich_sh")))

        when (intent.getStringExtra("page")) {
            "subs" -> {
                title.text = "Mening obunalarim"
                body.text = buildString {
                    append("Reja: Private access\n")
                    append("Holat: Faol ✅\n")
                    append("Faollashtirilgan: ${Session.activatedText(this@PageActivity)}\n\n")
                    append("Kanallar:\n")
                    Channels.all.forEach { append("• ${it.name}  (ID ${it.id})\n") }
                }
                addButton("Obunani yangilash / kod olish") { openTelegram() }
            }
            "account" -> {
                title.text = "Akkaunt"
                body.text = "Foydalanuvchi: ${Session.userName(this)}\n" +
                    "ID: ${Session.userId(this)}\n" +
                    "Faollashtirilgan: ${Session.activatedText(this)}\n" +
                    "Server: ${Session.server(this)}"
                addButton("Chiqish (qayta aktivatsiya kerak bo‘ladi)") {
                    AlertDialog.Builder(this)
                        .setTitle("Chiqish")
                        .setMessage("Akkauntdan chiqsangiz, ilovadan foydalanish uchun yangi kod kerak bo‘ladi.")
                        .setPositiveButton("Chiqish") { _, _ ->
                            Session.logout(this)
                            startActivity(
                                Intent(this, ActivationActivity::class.java)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            )
                        }
                        .setNegativeButton("Bekor", null)
                        .show()
                }
            }
            "help" -> {
                title.text = "Yordam"
                body.text = "• Ilovadan foydalanish uchun 6 xonali aktivatsiya kodi kerak.\n" +
                    "• Kod Authenticator orqali vaqt asosida yaratiladi (30 soniya amal qiladi).\n" +
                    "• Kodni olish uchun Telegram: t.me/ilhomvich_sh\n" +
                    "• Kanal ishlamasa, bosh sahifada boshqa serverni (stream2 / stream8) tanlang.\n" +
                    "• Player ichida PiP tugmasi — rasm ustida rasm rejimi.\n" +
                    "• TV pultida: ⬆/⬇ (CH+/CH-) kanalni almashtiradi."
                addButton("Telegram: @ilhomvich_sh") { openTelegram() }
            }
            else -> {
                title.text = "Haqida"
                body.text = "IPTV Player v${BuildConfig.VERSION_NAME}\n\n" +
                    "by Shakhzodjon Khudaybediyev\n\n" +
                    "Yopiq (private) ilova. Faqat aktivatsiya kodiga ega foydalanuvchilar uchun."
                addButton("Telegram: @ilhomvich_sh") { openTelegram() }
            }
        }
    }
}
