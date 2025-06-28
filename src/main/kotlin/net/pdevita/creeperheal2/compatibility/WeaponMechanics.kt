package net.pdevita.creeperheal2.compatibility

import com.google.auto.service.AutoService
import me.deecaad.weaponmechanics.WeaponMechanicsAPI
import me.deecaad.weaponmechanics.weapon.weaponevents.ProjectileExplodeEvent
import net.pdevita.creeperheal2.CreeperHeal2
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
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

//    @EventHandler(priority = EventPriority.HIGHEST)
//    fun onWeaponMechanicsExplodeEvent(event: ProjectileExplodeEvent) {
//        val allowedBlocks = ArrayList<Block>()
//        if (creeperHeal2.settings.types.allowExplosionBlock(/*event.block.blockData.material*/)) {
//            for (block in event.blocks) {
//                if (block.location.world?.let { creeperHeal2.settings.worldList.allowWorld(it.name) } == true) {
//                    allowedBlocks.add(block)
//                }
//            }
//        }
//
//        creeperHeal2.createNewExplosion(allowedBlocks)
//    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onWeaponMechanicsBreakBlockEvent(event: BlockBreakEvent) {
        if (event.eventName == "WeaponMechanicsBlockDamage") {
            println("1")
            if (WeaponMechanicsAPI.isBroken(event.block)) {
                println("TEST2")
                event.block.location.block.blockData
                if (event.block.location.world?.let { creeperHeal2.settings.worldList.allowWorld(it.name) } == true) {
                    creeperHeal2.createNewExplosion(listOf(event.block))
                }
            }
        }
    }
}
