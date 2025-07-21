package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class FillCommand : BaseCommand() {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!hasPermission(sender, "minecraft.command.fill")) {
            sendNoPermissionMessage(sender)
            return true
        }

        val player = requirePlayer(sender) ?: return true

        if (args.size < 7) {
            sendUsageMessage(sender, "/fill <x1> <y1> <z1> <x2> <y2> <z2> <block> [destroy|hollow|keep|outline|replace]")
            return true
        }

        val x1 = args[0].toIntOrNull()
        val y1 = args[1].toIntOrNull()
        val z1 = args[2].toIntOrNull()
        val x2 = args[3].toIntOrNull()
        val y2 = args[4].toIntOrNull()
        val z2 = args[5].toIntOrNull()

        if (x1 == null || y1 == null || z1 == null || x2 == null || y2 == null || z2 == null) {
            sendErrorMessage(sender, "坐标必须是有效的整数。")
            return true
        }

        val material = Material.matchMaterial(args[6])
        if (material == null || !material.isBlock) {
            sendErrorMessage(sender, "无效的方块类型: ${args[6]}")
            return true
        }

        val mode = if (args.size >= 8) args[7].lowercase() else "replace"

        val minX = minOf(x1, x2)
        val maxX = maxOf(x1, x2)
        val minY = minOf(y1, y2)
        val maxY = maxOf(y1, y2)
        val minZ = minOf(z1, z2)
        val maxZ = maxOf(z1, z2)

        val totalBlocks = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)
        if (totalBlocks > 32768) {
            sendErrorMessage(sender, "填充区域过大（最大 32768 个方块）。")
            return true
        }

        var blocksChanged = 0

        for (x in minX..maxX) {
            for (y in minY..maxY) {
                for (z in minZ..maxZ) {
                    val location = org.bukkit.Location(player.world, x.toDouble(), y.toDouble(), z.toDouble())
                    val block = location.block

                    val shouldPlace = when (mode) {
                        "keep" -> block.type.isAir
                        "destroy" -> {
                            if (!block.type.isAir) block.breakNaturally()
                            true
                        }
                        "hollow" -> {
                            x == minX || x == maxX || y == minY || y == maxY || z == minZ || z == maxZ
                        }
                        "outline" -> {
                            (x == minX || x == maxX) && (y == minY || y == maxY) ||
                            (x == minX || x == maxX) && (z == minZ || z == maxZ) ||
                            (y == minY || y == maxY) && (z == minZ || z == maxZ)
                        }
                        else -> true // replace
                    }

                    if (shouldPlace) {
                        if (mode == "hollow" && x != minX && x != maxX && y != minY && y != maxY && z != minZ && z != maxZ) {
                            block.type = Material.AIR
                        } else {
                            block.type = material
                        }
                        blocksChanged++
                    }
                }
            }
        }

        sendSuccessMessage(sender, "已填充 $blocksChanged 个方块")

        return true
    }

    override fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "minecraft.command.fill")) {
            return emptyList()
        }

        return when (args.size) {
            1, 2, 3, 4, 5, 6 -> filterCompletions(args, listOf("~", "~1", "~-1", "0"))
            7 -> {
                val blocks = Material.entries
                    .filter { it.isBlock }
                    .map { it.name.lowercase() }
                filterCompletions(args, blocks)
            }
            8 -> filterCompletions(args, listOf("replace", "destroy", "keep", "hollow", "outline"))
            else -> emptyList()
        }
    }
}
