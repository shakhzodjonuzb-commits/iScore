package uz.ilhomvich.iptv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Session.isActive(this)) {
            startActivity(Intent(this, ActivationActivity::class.java))
            finish()
            return
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        setContentView(R.layout.activity_main)

        val rb2 = findViewById<RadioButton>(R.id.rbServer2)
        val rb8 = findViewById<RadioButton>(R.id.rbServer8)
        if (Session.server(this) == "stream8") rb8.isChecked = true else rb2.isChecked = true
        rb2.setOnClickListener { Session.setServer(this, "stream2") }
        rb8.setOnClickListener { Session.setServer(this, "stream8") }

        val rv = findViewById<RecyclerView>(R.id.rvChannels)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = Adapter { index ->
            startActivity(Intent(this, PlayerActivity::class.java).putExtra("index", index))
        }

        fun page(p: String) = startActivity(Intent(this, PageActivity::class.java).putExtra("page", p))
        findViewById<Button>(R.id.btnSubs).setOnClickListener { page("subs") }
        findViewById<Button>(R.id.btnAccount).setOnClickListener { page("account") }
        findViewById<Button>(R.id.btnHelp).setOnClickListener { page("help") }
        findViewById<Button>(R.id.btnAbout).setOnClickListener { page("about") }
    }

    override fun onResume() {
        super.onResume()
        if (!Session.isActive(this)) {
            startActivity(Intent(this, ActivationActivity::class.java))
            finish()
        }
    }

    private class Adapter(val onPlay: (Int) -> Unit) : RecyclerView.Adapter<Adapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val logo: ImageView = v.findViewById(R.id.ivLogo)
            val emoji: TextView = v.findViewById(R.id.tvLogo)
            val name: TextView = v.findViewById(R.id.tvName)
            val id: TextView = v.findViewById(R.id.tvId)
            val play: Button = v.findViewById(R.id.btnPlay)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_channel, parent, false))

        override fun getItemCount() = Channels.all.size

        override fun onBindViewHolder(h: VH, position: Int) {
            val ch = Channels.all[position]
            val ctx = h.itemView.context
            h.name.text = ch.name
            h.id.text = "ID: ${ch.id}"
            // Logo: res/drawable-nodpi/<nom>.png bo'lsa ko'rsatiladi, bo'lmasa emoji/bosh harf
            val resId = ch.logoRes?.let { ctx.resources.getIdentifier(it, "drawable", ctx.packageName) } ?: 0
            if (resId != 0) {
                h.logo.visibility = View.VISIBLE
                h.logo.setImageResource(resId)
                h.emoji.visibility = View.GONE
            } else {
                h.logo.visibility = View.GONE
                h.emoji.visibility = View.VISIBLE
                h.emoji.text = ch.emoji ?: ch.name.take(1)
            }
            h.itemView.setOnClickListener { onPlay(position) }
            h.play.setOnClickListener { onPlay(position) }
        }
    }
}
