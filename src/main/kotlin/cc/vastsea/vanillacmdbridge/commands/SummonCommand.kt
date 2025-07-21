package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType

class SummonCommand : BaseCommand() {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!hasPermission(sender, "minecraft.command.summon")) {
            sendNoPermissionMessage(sender)
            return true
        }

        val player = requirePlayer(sender) ?: return true

        if (args.isEmpty()) {
            sendUsageMessage(sender, "/summon <entity> [x] [y] [z]")
            return true
        }

        val entityType = try {
            org.bukkit.entity.EntityType.valueOf(args[0].uppercase())
        } catch (e: IllegalArgumentException) {
            sendErrorMessage(sender, "未知的实体类型: ${args[0]}")
            return true
        }

        val location = if (args.size >= 4) {
            val coordinates = parseCoordinates(args, 1)
            if (coordinates == null) {
                sendErrorMessage(sender, "坐标必须是有效数字。")
                return true
            }
            createLocationFromCoordinates(player.location, coordinates)
        } else {
            player.location
        }

        try {
            player.world.spawnEntity(location, entityType)
            sendSuccessMessage(sender, "已在 (${location.x.toInt()}, ${location.y.toInt()}, ${location.z.toInt()}) 生成 ${entityType.name}")
        } catch (e: Exception) {
            sendErrorMessage(sender, "无法生成实体 ${entityType.name}")
        }

        return true
    }

    override fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "minecraft.command.summon")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> {
                val entities = EntityType.entries
                    .filter { it.isSpawnable && it != org.bukkit.entity.EntityType.UNKNOWN }
                    .map { it.name.lowercase() }
                filterCompletions(args, entities)
            }
            2, 3, 4 -> filterCompletions(args, listOf("~", "~1", "~-1", "0"))
            else -> emptyList()
        }
    }
}
