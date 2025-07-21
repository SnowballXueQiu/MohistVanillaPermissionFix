package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class ExperienceCommand : BaseCommand() {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!hasPermission(sender, "minecraft.command.experience")) {
            sendNoPermissionMessage(sender)
            return true
        }

        if (args.isEmpty()) {
            sendUsageMessage(sender, "/experience <add|set|query> <player> [amount] [points|levels]")
            return true
        }

        val action = args[0].lowercase()

        if (args.size < 2) {
            sendUsageMessage(sender, "/experience $action <player> [amount] [points|levels]")
            return true
        }

        val targetPlayer = getOnlinePlayer(args[1])
        if (targetPlayer == null) {
            sendErrorMessage(sender, "玩家 ${args[1]} 不在线。")
            return true
        }

        when (action) {
            "query" -> {
                sendSuccessMessage(sender, "${targetPlayer.name} 拥有 ${targetPlayer.totalExperience} 经验点，等级 ${targetPlayer.level}")
            }
            "add", "set" -> {
                if (args.size < 3) {
                    sendUsageMessage(sender, "/experience $action <player> <amount> [points|levels]")
                    return true
                }

                val amount = args[2].toIntOrNull()
                if (amount == null) {
                    sendErrorMessage(sender, "数量必须是有效的整数。")
                    return true
                }

                val type = if (args.size >= 4) args[3].lowercase() else "points"

                when (type) {
                    "levels" -> {
                        if (action == "add") {
                            targetPlayer.level += amount
                        } else {
                            targetPlayer.level = amount
                        }
                        sendSuccessMessage(sender, "已${if (action == "add") "给予" else "设置"} ${targetPlayer.name} $amount 经验等级")
                    }
                    "points" -> {
                        if (action == "add") {
                            targetPlayer.giveExp(amount)
                        } else {
                            targetPlayer.totalExperience = amount
                            targetPlayer.exp = 0f
                            targetPlayer.level = 0
                            targetPlayer.giveExp(amount)
                        }
                        sendSuccessMessage(sender, "已${if (action == "add") "给予" else "设置"} ${targetPlayer.name} $amount 经验点")
                    }
                    else -> {
                        sendErrorMessage(sender, "类型必须是 points 或 levels")
                        return true
                    }
                }
            }
            else -> {
                sendErrorMessage(sender, "操作必须是 add、set 或 query")
                return true
            }
        }

        return true
    }

    override fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "minecraft.command.experience")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> filterCompletions(args, listOf("add", "set", "query"))
            2 -> filterCompletions(args, getOnlinePlayerNames())
            3 -> {
                if (args[0].lowercase() in listOf("add", "set")) {
                    filterCompletions(args, listOf("1", "10", "100", "1000"))
                } else emptyList()
            }
            4 -> {
                if (args[0].lowercase() in listOf("add", "set")) {
                    filterCompletions(args, listOf("points", "levels"))
                } else emptyList()
            }
            else -> emptyList()
        }
    }
}
