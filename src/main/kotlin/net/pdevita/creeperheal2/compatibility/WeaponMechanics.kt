package net.pdevita.creeperheal2.compatibility

import com.google.auto.service.AutoService
import me.deecaad.weaponmechanics.WeaponMechanics
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileExplodeEvent
import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.plugin.Plugin

@AutoService(BaseCompatibility::class)
class WeaponMechanics : BaseCompatibility {
    override val pluginName = "WeaponMechanics"
    override val pluginPackage = "com.cjcrafter.weaponmechanics.WeaponMechanics"
    private lateinit var wmPlugin: WeaponMechanics
    private lateinit var creeperHeal2: CreeperHeal2

    override fun setPluginReference(plugin: Plugin) {
        wmPlugin = plugin as WeaponMechanics
    }

    override fun setCreeperHealReference(creeperHeal2: CreeperHeal2) {
        this.creeperHeal2 = creeperHeal2
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onWeaponMechanicsExplodeEvent(event: ProjectileExplodeEvent) {
        val allowedBlocks = ArrayList<Block>()
        if (creeperHeal2.settings.types.allowExplosionBlock(/*event.block.blockData.material*/)) {
            for (block in event.blocks) {
                if (block.location.world?.let { creeperHeal2.settings.worldList.allowWorld(it.name) } == true) {
                    allowedBlocks.add(block)
                }
            }
        }

        creeperHeal2.createNewExplosion(allowedBlocks)
    }
}
