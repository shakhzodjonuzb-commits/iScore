package uz.ilhomvich.iptv

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/** Lokal foydalanuvchi sessiyasi + aktivatsiya holati. */
object Session {
    private fun p(c: Context): SharedPreferences =
        c.applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun isActive(c: Context): Boolean = p(c).getBoolean("active", false) && userName(c).isNotEmpty()
    fun userName(c: Context): String = p(c).getString("name", "") ?: ""
    fun userId(c: Context): String = p(c).getString("uid", "") ?: ""
    fun activatedAt(c: Context): Long = p(c).getLong("at", 0L)

    fun activatedText(c: Context): String {
        val t = activatedAt(c)
        if (t == 0L) return "-"
        return SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(t))
    }

    fun activate(c: Context, name: String) {
        p(c).edit()
            .putBoolean("active", true)
            .putString("name", name)
            .putString("uid", UUID.randomUUID().toString().substring(0, 8).uppercase())
            .putLong("at", System.currentTimeMillis())
            .putInt("fails", 0)
            .putLong("lockUntil", 0L)
            .apply()
    }

    fun logout(c: Context) {
        p(c).edit().putBoolean("active", false).remove("name").remove("uid").remove("at").apply()
    }

    // ---- brute-force himoyasi: 5 ta xato -> 60 soniya blok ----
    fun lockRemainingSec(c: Context): Long {
        val left = p(c).getLong("lockUntil", 0L) - System.currentTimeMillis()
        return if (left > 0) (left + 999) / 1000 else 0
    }

    fun registerFail(c: Context) {
        val f = p(c).getInt("fails", 0) + 1
        val e = p(c).edit().putInt("fails", f)
        if (f >= 5) {
            e.putInt("fails", 0).putLong("lockUntil", System.currentTimeMillis() + 60_000L)
        }
        e.apply()
    }

    fun clearFails(c: Context) {
        p(c).edit().putInt("fails", 0).apply()
    }

    // ---- server tanlovi ----
    fun server(c: Context): String = p(c).getString("server", "stream2") ?: "stream2"
    fun setServer(c: Context, s: String) {
        p(c).edit().putString("server", s).apply()
    }
}
