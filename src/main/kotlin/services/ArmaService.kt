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
import java.lang.Exception
import java.net.InetSocketAddress
import java.util.*
import kotlin.concurrent.timerTask

@Service
class ArmaService(private val discord: Discord, private val configuration: Configuration) {

    init {
        // run update every 10s with an initial delay of 1s
        Timer().scheduleAtFixedRate(timerTask {
            runBlocking {
                this@ArmaService.updatePresence(discord)
            }
        }, 500, 30000)
    }

    suspend fun updatePresence(discord: Discord) {
        val serverInfo = this@ArmaService.getArma3Data(configuration.arma3ServerIP, configuration.arma3ServerPort)

        // if server is offline, we received an exception here and are just updating presence with offline server
        if (serverInfo.first == null) {
            discord.kord.editPresence { watching("Digby: Server Offline") }
            return
        }

        val modpack = this@ArmaService.getCurrentModpack(serverInfo.second!!.result.values)
        // build string
        val currPlayers = serverInfo.first!!.result.numOfPlayers
        val maxPlayers = serverInfo.first!!.result.maxPlayers

        discord.kord.editPresence { watching("Digby: $currPlayers/$maxPlayers | $modpack") }
    }

    suspend fun getArma3Data(address: String, port: Int): Pair<SourceQueryInfoResponse?, SourceQueryRulesResponse?> {
        // query arma server for info and players with steam-query api
        try {
            SourceQueryClient().use { client ->
                val serverCon = InetSocketAddress(address, port)
                val info: SourceQueryInfoResponse = client.getInfo(serverCon).join()
                val rules: SourceQueryRulesResponse = client.getRules(serverCon).join()

                return Pair(info, rules)
            }
        } catch (e: Exception) {
            return Pair(null,null)
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