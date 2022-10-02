package tech.grimm.midgard.data

import dev.kord.common.entity.Snowflake
import me.jakejmattson.discordkt.dsl.Data

@kotlinx.serialization.Serializable
data class Configuration(
    val owner: Snowflake = Snowflake(0),
    val database: String = "data/database.db",
    val apis: APIs = APIs(),
    val prefix: String = "!",
    val arma3ServerIP: String = "",
    val arma3ServerPort: Int = 2303
) : Data()


@kotlinx.serialization.Serializable
data class APIs(val youtube: String = "", val wolframAlpha: String = "")