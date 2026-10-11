package io.vonley.mi.di.network.protocols.webman

import io.vonley.mi.di.network.protocols.common.PSXNotifier
import io.vonley.mi.di.network.protocols.common.PSXProtocol
import io.vonley.mi.di.network.protocols.common.cmds.Boot
import io.vonley.mi.di.network.protocols.common.cmds.Buzzer
import io.vonley.mi.di.network.protocols.webman.models.Game
import io.vonley.mi.di.network.protocols.webman.models.GameType
import io.vonley.mi.models.enums.Feature
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import java.io.IOException
import java.net.URLEncoder

/** webMAN MOD's HTTP interface on a PS3 (port 80). */
interface Webman : PSXProtocol, PSXNotifier {

    override val feature: Feature get() = Feature.WEBMAN

    override val TAG: String get() = Webman::class.simpleName ?: Webman::class.java.name

    private val baseUrl: String get() = "http://${service.targetIp}:80"

    private val gameUrl: String
        get() = "$baseUrl/dev_hdd0/xmlhost/game_plugin/mygames.xml"

    /** Loads the game list XML; overridable so tests can supply a fixture. */
    @Throws(IOException::class)
    suspend fun loadGamesXml(): String? = fetch(gameUrl)

    @Throws(IOException::class)
    suspend fun searchGames(): Map<GameType, List<Game>> {
        val xml = loadGamesXml() ?: return emptyMap()
        val document = Jsoup.parse(xml, "", Parser.xmlParser())
        return linkedMapOf(
            GameType.PS3 to getGames(document, GameID.PS3),
            GameType.PS2 to getGames(document, GameID.PS2),
            GameType.PSP to getGames(document, GameID.PSP),
            GameType.PSX to getGames(document, GameID.PSX)
        )
    }

    enum class GameID(private val id: String) {
        PSX("seg_wm_psx_items"),
        PSP("seg_wm_psp_items"),
        PS2("seg_wm_ps2_items"),
        PS3("seg_wm_ps3_items");

        override fun toString(): String {
            return id.lowercase()
        }
    }

    fun getGames(document: Document, game: GameID): List<Game> {
        val games = document.getElementById(game.toString()) ?: return emptyList()
        val tables = games.getElementsByTag("T").takeIf { it.isNotEmpty() } ?: games.getElementsByTag("Table")
        return tables.mapNotNull { table ->
            val tableKey = table.attr("key")
            if (tableKey == "ps2_classic_launcher" || tableKey == "inc") return@mapNotNull null
            var title = ""
            var icon = ""
            var link = ""
            var info = ""
            for (s in table.children()) {
                val text = s.text().replace("<>", "").replace("</>", "")
                when (s.attr("key")) {
                    "title" -> title = text
                    "icon" -> icon = text
                    "module_action" -> link = text.replace("127.0.0.1", service.targetIp.toString())
                    "info" -> info = text
                }
            }
            if (title.isBlank()) return@mapNotNull null
            // Icons are served by webMAN itself; the UI loads them straight from the console.
            val iconUrl = if (icon.isBlank()) "" else baseUrl + icon.replace(" ", "%20")
            Game(title, iconUrl, link, info)
        }
    }

    override suspend fun notify(message: String) {
        val encoded = URLEncoder.encode(message, "UTF-8").replace("+", "%20")
        fetch("$baseUrl/popup.ps3/$encoded")
    }

    override suspend fun buzzer(buzz: Buzzer) {
        val ordinal = when (buzz) {
            Buzzer.CONTINUOUS -> Buzzer.TRIPLE.ordinal
            else -> buzz.ordinal
        }
        fetch("$baseUrl/buzzer.ps3mapi?mode=$ordinal")
    }

    override suspend fun boot(ps3boot: Boot) {
        val url = when (ps3boot) {
            Boot.REBOOT, Boot.SOFTREBOOT, Boot.HARDREBOOT -> "$baseUrl/restart.ps3"
            Boot.SHUTDOWN -> "$baseUrl/shutdown.ps3"
        }
        fetch(url)
    }

    suspend fun refresh() {
        fetch("$baseUrl/refresh.ps3")
    }

    suspend fun insert() {
        fetch("$baseUrl/insert.ps3")
    }

    suspend fun eject() {
        fetch("$baseUrl/eject.ps3")
    }

    suspend fun unmount() {
        fetch("$baseUrl/mount.ps3/unmount")
    }

    suspend fun play(game: Game) {
        fetch("$baseUrl${game.link}")
    }
}
