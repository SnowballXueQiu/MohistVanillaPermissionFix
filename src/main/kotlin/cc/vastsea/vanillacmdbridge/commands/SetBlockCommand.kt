package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class SetBlockCommand : BaseCommand() {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!hasPermission(sender, "minecraft.command.setblock")) {
            sendNoPermissionMessage(sender)
            return true
        }

        val player = requirePlayer(sender) ?: return true

        if (args.size < 4) {
            sendUsageMessage(sender, "/setblock <x> <y> <z> <block> [destroy|keep|replace]")
            return true
        }

        val x = args[0].toIntOrNull()
        val y = args[1].toIntOrNull()
        val z = args[2].toIntOrNull()

        if (x == null || y == null || z == null) {
            sendErrorMessage(sender, "坐标必须是有效的整数。")
            return true
        }

        val material = org.bukkit.Material.matchMaterial(args[3])
        if (material == null || !material.isBlock) {
            sendErrorMessage(sender, "无效的方块类型: ${args[3]}")
            return true
        }

        val location = org.bukkit.Location(player.world, x.toDouble(), y.toDouble(), z.toDouble())
        val block = location.block

        val mode = if (args.size >= 5) args[4].lowercase() else "replace"

        when (mode) {
            "keep" -> {
                if (!block.type.isAir) {
                    sendErrorMessage(sender, "此位置已有方块，使用keep模式无法替换。")
                    return true
                }
            }
            "destroy" -> {
                block.breakNaturally()
            }
        }

        block.type = material
        sendSuccessMessage(sender, "已在 ($x, $y, $z) 放置 ${material.name}")

        return true
    }

    override fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "minecraft.command.setblock")) {
            return emptyList()
        }

        return when (args.size) {
            1, 2, 3 -> filterCompletions(args, listOf("~", "~1", "~-1", "0"))
            4 -> {
                val blocks = Material.entries
                    .filter { it.isBlock }
                    .map { it.name.lowercase() }
                filterCompletions(args, blocks)
            }
            5 -> filterCompletions(args, listOf("replace", "destroy", "keep"))
            else -> emptyList()
        }
    }
}
