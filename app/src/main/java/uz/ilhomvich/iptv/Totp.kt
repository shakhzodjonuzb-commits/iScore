package uz.ilhomvich.iptv

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/** RFC 6238 TOTP (SHA-1, 6 xona, 30 soniya) — Google Authenticator bilan mos. */
object Totp {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private const val STEP_MS = 30_000L

    fun base32Decode(input: String): ByteArray {
        val out = ByteArrayOutputStream()
        var buffer = 0
        var bits = 0
        for (c in input.uppercase()) {
            val v = ALPHABET.indexOf(c)
            if (v < 0) continue
            buffer = (buffer shl 5) or v
            bits += 5
            if (bits >= 8) {
                bits -= 8
                out.write((buffer shr bits) and 0xFF)
                buffer = buffer and ((1 shl bits) - 1)
            }
        }
        return out.toByteArray()
    }

    fun codeAt(secret: String, counter: Long): String {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(base32Decode(secret), "HmacSHA1"))
        val msg = ByteBuffer.allocate(8).putLong(counter).array()
        val h = mac.doFinal(msg)
        val off = h[h.size - 1].toInt() and 0x0F
        val bin = ((h[off].toInt() and 0x7F) shl 24) or
            ((h[off + 1].toInt() and 0xFF) shl 16) or
            ((h[off + 2].toInt() and 0xFF) shl 8) or
            (h[off + 3].toInt() and 0xFF)
        return (bin % 1_000_000).toString().padStart(6, '0')
    }

    /** Joriy, oldingi va keyingi 30 soniyalik oynani qabul qiladi (soat farqi uchun). */
    fun verify(input: String, secret: String = BuildConfig.TOTP_SECRET, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (input.length != 6 || !input.all { it.isDigit() }) return false
        val counter = nowMs / STEP_MS
        var ok = false
        for (d in -1L..1L) {
            val expected = codeAt(secret, counter + d)
            if (MessageDigest.isEqual(expected.toByteArray(), input.toByteArray())) ok = true
        }
        return ok
    }
}
