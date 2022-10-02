package tech.grimm.midgard.services

import com.ibasco.agql.protocols.valve.source.query.SourceQueryClient
import com.ibasco.agql.protocols.valve.source.query.info.SourceQueryInfoResponse
import com.ibasco.agql.protocols.valve.source.query.players.SourceQueryPlayerResponse
import com.ibasco.agql.protocols.valve.source.query.rules.SourceQueryRulesResponse
import kotlinx.coroutines.runBlocking
import me.jakejmattson.discordkt.Discord
import me.jakejmattson.discordkt.annotations.Service
import org.jetbrains.exposed.sql.*
import tech.grimm.midgard.data.Configuration
import java.net.InetSocketAddress
import java.util.*
import kotlin.concurrent.timerTask

@Service
class ArmaService(private val discord: Discord, private val configuration: Configuration) {

    init {
        // initial call for first update, then we start timer for 10s updates. Have to wrap in a timer
        Timer().schedule(timerTask {
            runBlocking {
                this@ArmaService.updatePresence(discord)
            }
        }, 500)

        Timer().scheduleAtFixedRate(timerTask {
            runBlocking {
                this@ArmaService.updatePresence(discord)
            }
        }, 10000, 2000)
    }

    suspend fun updatePresence(discord: Discord) {
        val serverInfo = this@ArmaService.getArma3Data(configuration.arma3ServerIP, configuration.arma3ServerPort)
        val modpack = this@ArmaService.getCurrentModpack(serverInfo.second.result.values)
        // build string
        val currPlayers = serverInfo.first.result.numOfPlayers
        val maxPlayers = serverInfo.first.result.maxPlayers

        discord.kord.editPresence { watching("Digby: $currPlayers/$maxPlayers | $modpack") }
    }

    suspend fun getArma3Data(address: String, port: Int): Pair<SourceQueryInfoResponse, SourceQueryRulesResponse> {
        // query arma server for info and players with steam-query api
        SourceQueryClient().use { client ->
            val serverCon = InetSocketAddress(address, port)
            val info: SourceQueryInfoResponse = client.getInfo(serverCon).join()
            val rules: SourceQueryRulesResponse = client.getRules(serverCon).join()

            return Pair(info, rules)
        }
    }

    private fun getCurrentModpack(allMods: MutableCollection<String>): String {
        // loop through mods to check what mod
        allMods.forEach {
            if (it.contains("unsung", ignoreCase = true)) {
                return "Historical"
            }

            if (it.contains("RHS AFRF", ignoreCase = true)) {
                return "Modern"
            }

            if (it.contains("Operation: TREBUCHET", ignoreCase = true)) {
                return "Sci-fi"
            }
        }
        return "n/a"
    }
}