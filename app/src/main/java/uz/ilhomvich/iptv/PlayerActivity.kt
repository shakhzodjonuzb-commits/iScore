package uz.ilhomvich.iptv

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Rational
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
class PlayerActivity : AppCompatActivity() {
    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var topBar: View
    private lateinit var tvTitle: TextView
    private lateinit var tvError: TextView
    private var index = 0
    private val handler = Handler(Looper.getMainLooper())
    private val retry = Runnable { playCurrent() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Session.isActive(this)) {
            startActivity(Intent(this, ActivationActivity::class.java)); finish(); return
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_player)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        playerView = findViewById(R.id.playerView)
        topBar = findViewById(R.id.topBar)
        tvTitle = findViewById(R.id.tvTitle)
        tvError = findViewById(R.id.tvError)
        index = intent.getIntExtra("index", 0).coerceIn(0, Channels.all.size - 1)

        playerView.controllerShowTimeoutMs = 3000
        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { v ->
            topBar.visibility = if (v == View.VISIBLE) View.VISIBLE else View.GONE
        })

        findViewById<Button>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnPrev).setOnClickListener { change(-1) }
        findViewById<Button>(R.id.btnNext).setOnClickListener { change(1) }
        findViewById<Button>(R.id.btnPip).setOnClickListener { enterPip() }
        if (!pipSupported()) findViewById<Button>(R.id.btnPip).visibility = View.GONE
    }

    private fun pipSupported() =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)

    private fun enterPip() {
        if (!pipSupported()) return
        try {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9)).build()
            )
        } catch (_: Exception) {
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (player?.isPlaying == true) enterPip()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (!::playerView.isInitialized) return
        playerView.useController = !isInPictureInPictureMode
        topBar.visibility = if (isInPictureInPictureMode) View.GONE else View.VISIBLE
    }

    override fun onStart() {
        super.onStart()
        if (!Session.isActive(this)) { finish(); return }
        player = ExoPlayer.Builder(this).build().also { p ->
            playerView.player = p
            p.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    tvError.visibility = View.VISIBLE
                    tvError.text = "Kanal ochilmadi. Qayta uriniladi…\nBoshqa serverni tanlab ko‘ring."
                    handler.removeCallbacks(retry)
                    handler.postDelayed(retry, 4000)
                }

                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) tvError.visibility = View.GONE
                }
            })
        }
        playCurrent()
    }

    private fun playCurrent() {
        val p = player ?: return
        val ch = Channels.all[index]
        tvTitle.text = ch.name
        tvError.visibility = View.GONE
        val item = MediaItem.Builder()
            .setUri(ch.url(Session.server(this)))
            .setMimeType(MimeTypes.APPLICATION_M3U8)
            .build()
        p.setMediaItem(item)
        p.prepare()
        p.playWhenReady = true
    }

    private fun change(delta: Int) {
        val n = Channels.all.size
        index = (index + delta + n) % n
        handler.removeCallbacks(retry)
        playCurrent()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_CHANNEL_UP, KeyEvent.KEYCODE_PAGE_UP -> { change(1); return true }
                KeyEvent.KEYCODE_CHANNEL_DOWN, KeyEvent.KEYCODE_PAGE_DOWN -> { change(-1); return true }
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(retry)
        player?.release()
        player = null
        if (::playerView.isInitialized) playerView.player = null
    }
}
