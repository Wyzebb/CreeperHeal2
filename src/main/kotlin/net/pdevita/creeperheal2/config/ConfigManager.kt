package net.pdevita.creeperheal2.config

import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Material
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.EntityType
import java.io.File


class ConfigManager(private val plugin: CreeperHeal2) {
    val general = General(plugin.config)
    val types = ExplosionTypes(plugin.config)
    private val worldListConfig = ConfigFile(plugin, "worlds.yml")
    val worldList = WorldList(worldListConfig)
    private val blockListConfig = ConfigFile(plugin, "blocks.yml")
    val blockList = BlockList(plugin, blockListConfig)
}

open class ConfigFile(private val plugin: CreeperHeal2, private val fileName: String) {
    private val configFile = File(plugin.dataFolder, fileName)
    var config: YamlConfiguration
    init {
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false)
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }
}

class General(config: FileConfiguration) {
    val initialDelay = config.getInt("initial-delay", 45)
    val betweenBlocksDelay = config.getInt("between-blocks-delay", 20)
    val bstats = config.getBoolean("bstats", true)
    val explodeTNT = config.getBoolean("explode-tnt", true)
    val turboThreshold = config.getInt("turbo-threshold", 5000)
    val turboType = config.getInt("turbo-type", 0).coerceIn(0..1)
    val turboAmount = config.getInt("turbo-amount", 3).coerceAtMost(1000)
    val turboPercentage = config.getInt("turbo-percentage", 1).coerceIn(1..100)
    val turboCap = config.getInt("turbo-cap", 10).coerceAtMost(1000)
    val entityType = config.getBoolean("entity-type", true)
    val disableContainers = config.getBoolean("disable-containers", false)
    val dropBlacklisted = config.getBoolean("always-drop-blacklisted-items", false)
}

class ExplosionTypes(config: FileConfiguration) {
    private val tnt = config.getBoolean("types.tnt", false)
    private val creeper = config.getBoolean("types.creeper", true)
    // Dragon fireballs do not do any impact damage so even though it is
    // considered an explosion, it's not really something we need to worry about
//    val endDragon = config.getBoolean("types.end_dragon")
    private val ghast = config.getBoolean("types.ghast", false)
    private val wither = config.getBoolean("types.wither", false)
    private val endCrystal = config.getBoolean("types.ender-crystal", false) or config.getBoolean("types.end-crystal", false)
    private val minecartTnt = config.getBoolean("types.minecart-tnt", false)
    private val bed = config.getBoolean("types.bed", false)
    private val customEntity = config.getBoolean("types.custom", false)

    fun allowExplosionEntity(entity: EntityType): Boolean {
        return when(entity) {
            EntityType.CREEPER -> creeper
            EntityType.PRIMED_TNT -> tnt
            EntityType.FIREBALL -> ghast
            EntityType.WITHER -> wither
            EntityType.WITHER_SKULL -> wither
            EntityType.ENDER_CRYSTAL -> endCrystal
            EntityType.MINECART_TNT -> minecartTnt
            else -> customEntity
        }
    }

    fun allowExplosionBlock(/*block: Material*/): Boolean {
        // Only beds cause this?
        return bed
    }

}

class WorldList(config: ConfigFile) {
    private val mode = config.config.getString("worldlist-type", "blacklist")!!.lowercase()
    private val isWhiteList = mode == "whitelist"
    private val worldList = config.config.getStringList("worlds")

    fun allowWorld(worldName: String): Boolean {
        return if (isWhiteList) {
            worldName in worldList
        } else {
            worldName !in worldList
        }
    }
}


class BlockList(plugin: CreeperHeal2, config: ConfigFile) {
    private val mode = config.config.getString("blocklist-type", "blacklist")!!.lowercase()
    private val isWhiteList = mode == "whitelist"
    private val blockListStrings = config.config.getStringList("blocks")
    private val blockList = blockListStrings.mapNotNull { materialOrNull(it) }.toSet()

    private fun materialOrNull(name: String): Material? {
        return try {
            Material.valueOf(name)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    init {
        plugin.debugLogger("Enabling blocklist in ${if (isWhiteList) "whitelist" else "blacklist"} mode with blocks ${blockList.map { it.name }.joinToString { ", " }}")
    }

    fun allowMaterial(material: Material): Boolean {
        return if (isWhiteList) {
            material in blockList
        } else {
            material !in blockList
        }
    }
}

