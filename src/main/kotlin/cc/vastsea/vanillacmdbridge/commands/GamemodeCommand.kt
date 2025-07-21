package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class GamemodeCommand : BaseCommand() {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!hasPermission(sender, "minecraft.command.gamemode")) {
            sendNoPermissionMessage(sender)
            return true
        }
        
        if (args.isEmpty()) {
            sendUsageMessage(sender, "/gamemode <mode> [player]")
            return true
        }
        
        val gameMode = parseGameMode(args[0])
        if (gameMode == null) {
            sendErrorMessage(sender, "无效的游戏模式: ${args[0]}")
            sendErrorMessage(sender, "可用模式: survival, creative, adventure, spectator (或 0, 1, 2, 3)")
            return true
        }
        
        val targetPlayer = if (args.size >= 2) {
            val target = getOnlinePlayer(args[1])
            if (target == null) {
                sendErrorMessage(sender, "玩家 ${args[1]} 不在线。")
                return true
            }
            target
        } else {
            requirePlayer(sender) ?: return true
        }
        
        // 检查是否有权限修改其他玩家的游戏模式
        if (targetPlayer != sender && !hasPermission(sender, "minecraft.command.gamemode.other")) {
            sendNoPermissionMessage(sender)
            return true
        }
        
        targetPlayer.gameMode = gameMode
        
        val modeName = gameMode.name.lowercase()
        if (targetPlayer == sender) {
            sendSuccessMessage(sender, "已将你的游戏模式设置为 $modeName")
        } else {
            sendSuccessMessage(sender, "已将 ${targetPlayer.name} 的游戏模式设置为 $modeName")
            sendSuccessMessage(targetPlayer, "你的游戏模式已被设置为 $modeName")
        }
        
        return true
    }
    
    override fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "minecraft.command.gamemode")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> {
                val gameModes = listOf("survival", "creative", "adventure", "spectator", "0", "1", "2", "3")
                filterCompletions(args, gameModes)
            }
            2 -> {
                if (hasPermission(sender, "minecraft.command.gamemode.other")) {
                    filterCompletions(args, getOnlinePlayerNames())
                } else emptyList()
            }
            else -> emptyList()
        }
    }

    private fun parseGameMode(input: String): GameMode? {
        return when (input.lowercase()) {
            "0", "survival", "s" -> GameMode.SURVIVAL
            "1", "creative", "c" -> GameMode.CREATIVE
            "2", "adventure", "a" -> GameMode.ADVENTURE
            "3", "spectator", "sp" -> GameMode.SPECTATOR
            else -> null
        }
    }
}