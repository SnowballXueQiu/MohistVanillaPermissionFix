package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.inventory.ItemStack

class GiveCommand : BaseCommand() {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!hasPermission(sender, "minecraft.command.give")) {
            sendNoPermissionMessage(sender)
            return true
        }

        if (args.size < 2) {
            sendUsageMessage(sender, "/give <player> <item> [amount] [data]")
            return true
        }

        val targetPlayer = getOnlinePlayer(args[0])
        if (targetPlayer == null) {
            sendErrorMessage(sender, "玩家 ${args[0]} 不在线。")
            return true
        }

        val material = Material.matchMaterial(args[1])
        if (material == null) {
            sendErrorMessage(sender, "未知的物品: ${args[1]}")
            return true
        }

        val amount = if (args.size >= 3) {
            args[2].toIntOrNull() ?: 1
        } else 1

        if (amount <= 0 || amount > material.maxStackSize * 36) {
            sendErrorMessage(sender, "无效的数量: $amount")
            return true
        }

        val item = ItemStack(material, amount)
        val remainingItems = targetPlayer.inventory.addItem(item)

        if (remainingItems.isNotEmpty()) {
            sendErrorMessage(sender, "玩家背包已满，部分物品未给予。")
        }

        sendSuccessMessage(sender, "已给予 ${targetPlayer.name} ${amount}x ${material.name}")
        sendSuccessMessage(targetPlayer, "你获得了 ${amount}x ${material.name}")

        return true
    }

    override fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "minecraft.command.give")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> filterCompletions(args, getOnlinePlayerNames())
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
