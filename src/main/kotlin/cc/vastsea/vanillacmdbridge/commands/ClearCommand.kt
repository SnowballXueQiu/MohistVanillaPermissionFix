package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class ClearCommand : BaseCommand() {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!hasPermission(sender, "minecraft.command.clear")) {
            sendNoPermissionMessage(sender)
            return true
        }

        val targetPlayer = if (args.isEmpty()) {
            requirePlayer(sender) ?: return true
        } else {
            if (!hasPermission(sender, "minecraft.command.clear.other")) {
                sendNoPermissionMessage(sender)
                return true
            }
            val target = getOnlinePlayer(args[0])
            if (target == null) {
                sendErrorMessage(sender, "玩家 ${args[0]} 不在线。")
                return true
            }
            target
        }

        val material = if (args.size >= 2) {
            val mat = Material.matchMaterial(args[1])
            if (mat == null) {
                sendErrorMessage(sender, "未知的物品: ${args[1]}")
                return true
            }
            mat
        } else null

        val maxCount = if (args.size >= 3) {
            val count = args[2].toIntOrNull()
            if (count == null || count < 0) {
                sendErrorMessage(sender, "数量必须是非负整数。")
                return true
            }
            count
        } else -1

        var removedCount = 0

        if (material == null) {
            // 清空整个背包
            removedCount = targetPlayer.inventory.contents.filterNotNull().sumOf { it.amount }
            targetPlayer.inventory.clear()
        } else {
            // 清除指定物品
            val items = targetPlayer.inventory.contents.filterNotNull()
                .filter { it.type == material }

            for (item in items) {
                if (maxCount != -1 && removedCount >= maxCount) break

                val toRemove = if (maxCount == -1) item.amount else minOf(item.amount, maxCount - removedCount)
                removedCount += toRemove

                if (toRemove >= item.amount) {
                    targetPlayer.inventory.remove(item)
                } else {
                    item.amount -= toRemove
                }
            }
        }

        if (removedCount > 0) {
            if (material == null) {
                sendSuccessMessage(sender, "已清空 ${targetPlayer.name} 的背包，移除了 $removedCount 个物品")
            } else {
                sendSuccessMessage(sender, "已从 ${targetPlayer.name} 的背包中移除了 $removedCount 个 ${material.name}")
            }

            if (targetPlayer != sender) {
                if (material == null) {
                    sendSuccessMessage(targetPlayer, "你的背包已被清空")
                } else {
                    sendSuccessMessage(targetPlayer, "你的 ${material.name} 已被移除")
                }
            }
        } else {
            sendErrorMessage(sender, "没有找到要清除的物品")
        }

        return true
    }

    override fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "minecraft.command.clear")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> {
                if (hasPermission(sender, "minecraft.command.clear.other")) {
                    filterCompletions(args, getOnlinePlayerNames())
                } else emptyList()
            }
            2 -> {
                val items = Material.entries
                    .filter { it.isItem }
                    .map { it.name.lowercase() }
                filterCompletions(args, items)
            }
            3 -> filterCompletions(args, listOf("1", "16", "32", "64"))
            else -> emptyList()
        }
    }
}
