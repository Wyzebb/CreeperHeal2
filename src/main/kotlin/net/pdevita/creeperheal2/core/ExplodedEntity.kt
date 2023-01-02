package net.pdevita.creeperheal2.core

import org.bukkit.Art
import org.bukkit.Location
import org.bukkit.Rotation
import org.bukkit.block.BlockFace
import org.bukkit.entity.*
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

open class ExplodedEntity() {
    open var entityType: EntityType? = null
    lateinit var location: Location

    companion object {
        fun from(entity: Entity): ExplodedEntity {
            return when (entity) {
                is org.bukkit.entity.Painting -> ExplodedPainting(entity)
                is org.bukkit.entity.ItemFrame -> ExplodedItemFrame(entity)
                else -> throw java.lang.Exception("$entity is not a supported exploded entity")
            }
        }
    }

    constructor(entity: Entity): this() {
        saveData(entity)
        entity.remove()
    }

    open fun saveData(entity: Entity) {
        location = entity.location
    }

    fun spawnEntity(location: Location): Entity? {
        val entityClass = entityType!!.entityClass!!
        return location.world?.spawn(location, entityClass) { loadData(it) }
    }

    fun placeEntity(): Boolean {
        try {
            spawnEntity(location)
        } catch (e: IllegalArgumentException) {
            println("IllegalArgumentException, could not place painting $location ${location.block}")
            return false
        }
        return true
    }

    open fun loadData(entity: Entity) {

    }

}

open class ExplodedHanging(entity: Entity): ExplodedEntity(entity) {
    lateinit var facingDirection: BlockFace

    override fun saveData(entity: Entity) {
        super.saveData(entity)
        val hanging = entity as Hanging
        facingDirection = hanging.facing
    }

    override fun loadData(entity: Entity) {
        super.loadData(entity)
        val hanging = entity as Hanging
        hanging.setFacingDirection(facingDirection)
    }
}

class ExplodedPainting(entity: Entity) : ExplodedHanging(entity) {
    override var entityType: EntityType? = EntityType.PAINTING
    private lateinit var art: Art

    companion object {
        val data = HashMap<BlockFace, HashMap<String, Vector>>()

        fun addPaintingData(direction: BlockFace, size: String, offset: Vector) {
            if (!data.contains(direction)) {
                data[direction] = HashMap()
            }
            val directionMap = data[direction]!!
            if (!directionMap.contains(size)) {
                directionMap[size] = offset
            } else {
                if (directionMap[size] != offset) {
                    println("Different offsets!!! $direction $size ${directionMap[size]} != ${offset}")
                }
            }
        }

        fun printPaintingData() {
            println("Painting Stats:")
            for (direction in data.keys) {
                val directionMap = data[direction]!!
                println("$direction:")
                for (size in directionMap.keys) {
                    println("$size - ${directionMap[size]}")
                }
            }
        }

    }

    override fun saveData(entity: Entity) {
        super.saveData(entity)
        val painting = entity as org.bukkit.entity.Painting
        art = painting.art
    }

    override fun loadData(entity: Entity) {
        // Use the normally spawned painting to figure out how far
        // it will move after we set the art
        super.loadData(entity)
        val painting = entity as org.bukkit.entity.Painting
        painting.setArt(art, true)
        val movedLocation = painting.location
        val moveX = movedLocation.x - location.x
        val moveY = movedLocation.y - location.y
        val moveZ = movedLocation.z - location.z
        entity.remove()
        // Correct the location and make a new painting
        val moveVector = Vector(moveX, moveY, moveZ)
        addPaintingData(facingDirection, "${art.blockWidth}x${art.blockHeight}", moveVector)
        val fixedLocation = location.clone()
        fixedLocation.subtract(moveVector)
        val entityClass = entityType!!.entityClass!!
        location.world?.spawn(location, entityClass) {
            super.loadData(it)
            val newPainting = it as Painting
            newPainting.setArt(art, true)

        }
        println("Painting of size ${art.blockWidth}x${art.blockHeight} was originally at $location. " +
                "After placing and setting art, it moved by $moveVector. It was then deleted and placed at " +
                "$fixedLocation.")
        printPaintingData()
    }
}

class ExplodedItemFrame(entity: Entity) : ExplodedHanging(entity) {
    override var entityType: EntityType? = EntityType.ITEM_FRAME
    private lateinit var itemStack: ItemStack
    private lateinit var rotation: Rotation

    override fun saveData(entity: Entity) {
        super.saveData(entity)
        val itemFrame = entity as ItemFrame
        itemStack = itemFrame.item
        rotation = itemFrame.rotation
    }

    override fun loadData(entity: Entity) {
        super.loadData(entity)
        val itemFrame = entity as ItemFrame
        itemFrame.setItem(itemStack)
        itemFrame.rotation = rotation
    }
}

