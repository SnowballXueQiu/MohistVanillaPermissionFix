package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TeleportCommand : BaseCommand() {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!hasPermission(sender, "minecraft.command.teleport")) {
            sendNoPermissionMessage(sender)
            return true
        }

        val player = requirePlayer(sender) ?: return true

        when (args.size) {
            1 -> {
                // /tp <target>
                val target = getOnlinePlayer(args[0])
                if (target == null) {
                    sendErrorMessage(sender, "玩家 ${args[0]} 不在线。")
                    return true
                }
                player.teleport(target)
                sendSuccessMessage(sender, "已传送到 ${target.name}")
            }
            3 -> {
                // /tp <x> <y> <z>
                handleTeleportToCoordinates(sender, player, args, 0)
            }
            4 -> {
                // /tp <player> <target> 或 /tp <player> <x> <y> <z>
                handleTeleportOther(sender, args)
            }
            5 -> {
                // /tp <player> <x> <y> <z>
                handleTeleportOther(sender, args)
            }
            else -> {
                sendUsageMessage(sender, "/tp <target> 或 /tp <x> <y> <z> 或 /tp <player> <target> 或 /tp <player> <x> <y> <z>")
            }
        }

        return true
    }

    /**
     * 处理传送到坐标的逻辑
     */
    private fun handleTeleportToCoordinates(sender: CommandSender, targetPlayer: Player, args: Array<out String>, startIndex: Int) {
        val coordinates = parseCoordinates(args, startIndex)
        if (coordinates == null) {
            sendErrorMessage(sender, "坐标必须是有效数字。")
            return
        }

        val location = createLocationFromCoordinates(targetPlayer.location, coordinates)
        targetPlayer.teleport(location)

        val coordText = "(${coordinates[0]}, ${coordinates[1]}, ${coordinates[2]})"
        if (sender == targetPlayer) {
            sendSuccessMessage(sender, "已传送到坐标 $coordText")
        } else {
            sendSuccessMessage(sender, "已将 ${targetPlayer.name} 传送到坐标 $coordText")
            sendSuccessMessage(targetPlayer, "你被传送到了坐标 $coordText")
        }
    }

    /**
     * 处理传送其他玩家的逻辑
     */
    private fun handleTeleportOther(sender: CommandSender, args: Array<out String>) {
        if (!hasPermission(sender, "minecraft.command.teleport.other")) {
            sendNoPermissionMessage(sender)
            return
        }

        val targetPlayer = getOnlinePlayer(args[0])
        if (targetPlayer == null) {
            sendErrorMessage(sender, "玩家 ${args[0]} 不在线。")
            return
        }

        when (args.size) {
            4 -> {
                // 尝试解析为坐标
                val coordinates = parseCoordinates(args, 1)
                if (coordinates != null) {
                    // /tp <player> <x> <y> <z>
                    handleTeleportToCoordinates(sender, targetPlayer, args, 1)
                } else {
                    // /tp <player> <target>
                    val target = getOnlinePlayer(args[1])
                    if (target == null) {
                        sendErrorMessage(sender, "玩家 ${args[1]} 不在线。")
                        return
                    }
                    targetPlayer.teleport(target)
                    sendSuccessMessage(sender, "已将 ${targetPlayer.name} 传送到 ${target.name}")
                    sendSuccessMessage(targetPlayer, "你被传送到了 ${target.name}")
                }
            }
            5 -> {
                // /tp <player> <x> <y> <z>
                handleTeleportToCoordinates(sender, targetPlayer, args, 1)
            }
        }
    }

    override fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "minecraft.command.teleport")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> {
                val completions = mutableListOf<String>()
                completions.addAll(getOnlinePlayerNames())
                completions.addAll(listOf("~", "~1", "~-1", "0"))
                filterCompletions(args, completions)
            }
            2 -> {
                val firstArg = args[0]
                if (getOnlinePlayer(firstArg) != null && hasPermission(sender, "minecraft.command.teleport.other")) {
                    // 第一个参数是玩家名，第二个可以是目标玩家或坐标
                    val completions = mutableListOf<String>()
                    completions.addAll(getOnlinePlayerNames())
                    completions.addAll(listOf("~", "~1", "~-1", "0"))
                    filterCompletions(args, completions)
                } else {
                    // 第一个参数是坐标，第二个也是坐标
                    filterCompletions(args, listOf("~", "~1", "~-1", "0", "64", "128"))
                }
            }
            3 -> {
                filterCompletions(args, listOf("~", "~1", "~-1", "0", "64", "128"))
            }
            4 -> {
                if (hasPermission(sender, "minecraft.command.teleport.other")) {
                    filterCompletions(args, listOf("~", "~1", "~-1", "0", "64", "128"))
                } else emptyList()
            }
            else -> emptyList()
        }
    }
}
