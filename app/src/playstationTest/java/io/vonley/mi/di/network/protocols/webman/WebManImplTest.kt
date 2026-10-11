package io.vonley.mi.di.network.protocols.webman

import android.content.res.AssetManager
import io.vonley.mi.di.network.PSXService

/** Serves the game list from a bundled fixture instead of a real PS3. */
class WebManImplTest(override val service: PSXService, private val assets: AssetManager) : Webman {

    override suspend fun loadGamesXml(): String =
        assets.open("webman/games.xml").use { it.readBytes().decodeToString() }
}
