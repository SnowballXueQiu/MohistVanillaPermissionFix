package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class KillCommand : BaseCommand() {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!hasPermission(sender, "minecraft.command.kill")) {
            sendNoPermissionMessage(sender)
            return true
        }

        val targetPlayer = if (args.isEmpty()) {
            requirePlayer(sender) ?: return true
        } else {
            if (!hasPermission(sender, "minecraft.command.kill.other")) {
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

        targetPlayer.health = 0.0

        if (targetPlayer == sender) {
            sendSuccessMessage(sender, "你已死亡")
        } else {
            sendSuccessMessage(sender, "已杀死 ${targetPlayer.name}")
            sendErrorMessage(targetPlayer, "你被 ${sender.name} 杀死了")
        }

        return true
    }

    override fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "minecraft.command.kill")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> {
                if (hasPermission(sender, "minecraft.command.kill.other")) {
                    filterCompletions(args, getOnlinePlayerNames())
                } else emptyList()
            }
            else -> emptyList()
        }
    }
}
