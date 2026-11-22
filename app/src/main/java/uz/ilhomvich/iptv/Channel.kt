package uz.ilhomvich.iptv

data class Channel(
    val id: Int,
    val name: String,
    val logoRes: String? = null, // res/drawable-nodpi/<logoRes>.png
    val emoji: String? = null
) {
    fun url(server: String) = "https://$server.cinerama.uz/$id/tracks-v1a1/mono.m3u8"
}

object Channels {
    val all = listOf(
        Channel(1004, "Sport TV", "sport"),
        Channel(1010, "Futbol TV ⚽", null, "⚽"),
        Channel(1016, "ZoʻrTV", "zor_tv"),
        Channel(1263, "Setanta Sports 1", "setanta"),
        Channel(1264, "Setanta Sports 2", "setanta")
    )
}
