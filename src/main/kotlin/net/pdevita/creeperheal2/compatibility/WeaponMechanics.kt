package net.pdevita.creeperheal2.compatibility

import com.google.auto.service.AutoService
import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent


@AutoService(BaseCompatibility::class)
class WeaponMechanics : BaseCompatibility {
    override val pluginName = "WeaponMechanics"
    override val pluginPackage = "me.deecaad.weaponmechanics.WeaponMechanicsLoader"
    private lateinit var creeperHeal2: CreeperHeal2

    override fun setCreeperHealReference(creeperHeal2: CreeperHeal2) {
        this.creeperHeal2 = creeperHeal2
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onWeaponMechanicsBreakBlockEvent(event: BlockBreakEvent) {
        if (event.eventName == "WeaponMechanicsBlockDamage") {
            val loc = event.block.location.clone()
            val originalMaterial = event.block.type
            val originalData = event.block.blockData.clone()

            Bukkit.getScheduler().runTaskLater(creeperHeal2, Runnable {
                checkLocAndExplode(loc, originalMaterial, originalData)
            }, 1)
        }
    }

    fun checkLocAndExplode(loc: Location, originalMaterial: Material, originalData: BlockData) {
        val block = loc.block
        if (block.type == Material.AIR) {
            if (loc.world?.let { creeperHeal2.settings.worldList.allowWorld(it.name) } == true) {
                block.type = originalMaterial
                block.blockData = originalData

                creeperHeal2.createNewExplosion(listOf(block))
            }
        }
    }
}