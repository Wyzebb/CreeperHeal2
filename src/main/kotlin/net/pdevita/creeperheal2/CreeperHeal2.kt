package net.pdevita.creeperheal2

import net.pdevita.creeperheal2.commands.Commands
import net.pdevita.creeperheal2.config.ConfigManager
import net.pdevita.creeperheal2.constants.ConstantsManager
import net.pdevita.creeperheal2.core.Explosion
import net.pdevita.creeperheal2.core.Gravity
import net.pdevita.creeperheal2.events.Explode
import net.pdevita.creeperheal2.utils.Stats
import org.bstats.bukkit.Metrics
import org.bukkit.block.Block
import org.bukkit.plugin.java.JavaPlugin

class CreeperHeal2 : JavaPlugin {
    private val explosions: ArrayList<Explosion> = ArrayList()
    val gravity = Gravity(this)
    private var debug = false
    val constants = ConstantsManager(this)
    lateinit var settings: ConfigManager
    var stats: Stats? = null

    constructor() : super()

    override fun onEnable() {
        super.onEnable()
        // Init config file
        saveDefaultConfig()
        reloadConfig()
        debug = config.getBoolean("debug")
        settings = ConfigManager(this, config)
        if (settings.general.bstats) {
            this.stats = Stats(this)
        }

        registerEvents()
        getCommand("ch")!!.setExecutor(Commands(this))
    }

    private fun registerEvents() {
        server.pluginManager.registerEvents(Explode(this), this)
    }

    fun createNewExplosion(blockList: List<Block>) {
        explosions.add(Explosion(this, blockList))
    }

    fun removeExplosion(explosion: Explosion) {
        explosions.remove(explosion)
    }

    fun debugLogger(message: String) {
        if (this.debug) {
            this.logger.info(message)
        }
    }

    fun warpExplosions() {
        this.debugLogger("Running warp")
        val itr = explosions.iterator()
        while (itr.hasNext()) {
            itr.next().warpReplaceBlocks()
            itr.remove()
        }
    }

    override fun onDisable() {
        super.onDisable()
        // Quickly replace all blocks before shutdown
        val itr = explosions.iterator()
        while (itr.hasNext()) {
            itr.next().warpReplaceBlocks()
            itr.remove()
        }
    }
}

