package net.pdevita.creeperheal2.utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.Directional
import org.bukkit.block.data.FaceAttachable
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.type.Bed
import org.bukkit.block.data.type.Piston
import org.bukkit.block.data.type.PointedDripstone
import org.bukkit.block.data.type.Scaffolding
import java.util.*

interface FindDependentBlock {
    open fun reorient(state: BlockState): Location?
    open fun getBlockFace(state: BlockState): BlockFace?
}

open class FindDependentBlockBase: FindDependentBlock {
    override fun reorient(state: BlockState): Location? {
        return this.getBlockFace(state)?.let { state.block.getRelative(it).location }
    }

    override fun getBlockFace(state: BlockState): BlockFace? {
        println("ERROR: FindDependentBlock object has not implemented getBlockFace!!!")
        return null
    }
}

object InFrontOf: FindDependentBlockBase() {
    override fun getBlockFace(state: BlockState): BlockFace? {
        if (state.blockData is Directional) {
            return (state.blockData as Directional).facing
        }
        return null
    }
}

object Behind: FindDependentBlockBase() {
    override fun getBlockFace(state: BlockState): BlockFace? {
        if (state.blockData is Directional) {
            return (state.blockData as Directional).facing.oppositeFace
        }
        return null
    }
}

object Below: FindDependentBlockBase() {
    override fun getBlockFace(state: BlockState): BlockFace? {
        return BlockFace.UP
    }
}

object OnTopOf: FindDependentBlockBase() {
    override fun getBlockFace(state: BlockState): BlockFace? {
        return BlockFace.DOWN
    }
}

object Vine: FindDependentBlockBase() {
    override fun getBlockFace(state: BlockState): BlockFace? {
        // Try to attach to a vine above first
        if (state.block.getRelative(BlockFace.UP).blockData.material == state.blockData.material) {
            return BlockFace.UP
        }
        // Otherwise try attaching to one of it's attached faces
        val multipleFacing = state.blockData as MultipleFacing
        for (face in multipleFacing.faces) {
            if (state.block.getRelative(face).blockData.material != Material.AIR) {
                return face
            }
        }
        // Otherwise attach to block above
        if (state.block.getRelative(BlockFace.UP).blockData.material != Material.AIR) {
            return BlockFace.UP
        }
        return null
    }
}

object FaceAttachable: FindDependentBlockBase() {
    override fun getBlockFace(state: BlockState): BlockFace? {
        val faceAttachable = state.blockData as FaceAttachable
        return when (faceAttachable.attachedFace) {
            FaceAttachable.AttachedFace.CEILING -> BlockFace.UP
            FaceAttachable.AttachedFace.FLOOR -> BlockFace.DOWN
            FaceAttachable.AttachedFace.WALL -> Behind.getBlockFace(state)
        }
    }
}

object TopOrBottom: FindDependentBlockBase() {
    override fun getBlockFace(state: BlockState): BlockFace? {
        if (state.block.getRelative(BlockFace.UP).blockData.material != Material.AIR) {
            return BlockFace.UP
        } else if (state.block.getRelative(BlockFace.DOWN).blockData.material != Material.AIR) {
            return BlockFace.DOWN
        }
        return null
    }
}

object Piston:FindDependentBlockBase() {
    override fun getBlockFace(state: BlockState): BlockFace? {
        if (state.blockData is Piston) {
            val piston = state.blockData as Piston
            if (piston.isExtended) {
                return piston.facing
            }
        }
        return null
    }
}

object Scaffolding:FindDependentBlockBase() {
    override fun getBlockFace(state: BlockState): BlockFace? {
        if (state.blockData is Scaffolding) {
            val scaffolding = state.blockData as Scaffolding
            // Bottom is kinda weird, you would think it would be true
            // if it was on the bottom or was dependent on the below block
            if (scaffolding.isBottom) {
                // Find a nearby scaffolding that is a lower distance than this one
                for (blockFace in listOf(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH)) {
                    val block = state.block.getRelative(blockFace)
                    if (block.blockData is Scaffolding) {
                        if ((block.blockData as Scaffolding).distance < scaffolding.distance) {
                            return blockFace
                        }
                    }
                }
                return null
            } else {
                // It's on top of a block, return that block
                return BlockFace.DOWN
            }
        }
        return null
    }
}

object Dripstone:FindDependentBlockBase() {
    override fun getBlockFace(state: BlockState): BlockFace? {
        if (state.blockData is PointedDripstone) {
            return (state.blockData as PointedDripstone).verticalDirection.oppositeFace
        }
        return null
    }
}

interface FindDependentBlocks {
    open fun getDependents(state: BlockState): List<Location>?
    open fun getDependentFaces(state: BlockState): LinkedList<BlockFace>?
    open fun getParents(state: BlockState): List<Location>?
    open fun getParentFaces(state: BlockState): LinkedList<BlockFace>?
}

open class FindDependentBlocksBase: FindDependentBlocks {
    override fun getDependents(state: BlockState): List<Location>? {
        return getDependentFaces(state)?.map { state.block.getRelative(it).location }
    }

    override fun getDependentFaces(state: BlockState): LinkedList<BlockFace>? {
        return null
    }

    override fun getParents(state: BlockState): List<Location>? {
        return getParentFaces(state)?.map { state.block.getRelative(it).location }
    }

    override fun getParentFaces(state: BlockState): LinkedList<BlockFace>? {
        return null
    }
}

object DoorMultiBlock: FindDependentBlocksBase() {
    override fun getDependentFaces(state: BlockState): LinkedList<BlockFace>? {
        val bisected = state.blockData as Bisected
        if (bisected.half == Bisected.Half.BOTTOM) {
            return LinkedList(listOf(BlockFace.UP, BlockFace.DOWN))
        } else if (bisected.half == Bisected.Half.TOP) {
            return LinkedList(listOf(BlockFace.DOWN))
        }
        return null
    }

    override fun getParentFaces(state: BlockState): LinkedList<BlockFace>? {
        return LinkedList(listOf(BlockFace.DOWN))
    }
}

object BedMultiBlock:FindDependentBlocksBase() {
    override fun getDependentFaces(state: BlockState): LinkedList<BlockFace>? {
        val bed = state.blockData as Bed
        if (bed.part == Bed.Part.FOOT) {
            return LinkedList(listOf(bed.facing))
        } else if (bed.part == Bed.Part.HEAD) {
            return LinkedList(listOf(bed.facing.oppositeFace))
        }
        return null
    }
    override fun getParentFaces(state: BlockState): LinkedList<BlockFace>? {
        val bed = state.blockData as Bed
        if (bed.part == Bed.Part.FOOT) {
            return LinkedList(listOf(bed.facing))
        }
        return null
    }
}

//object DripLeafMultiBlock:FindDependentBlocksBase() {
//    override fun getBlockFaces(state: BlockState): LinkedList<BlockFace>? {
//
//    }
//}

