package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TimeCommand : BaseCommand() {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!hasPermission(sender, "minecraft.command.time")) {
            sendNoPermissionMessage(sender)
            return true
        }
        
        if (args.isEmpty()) {
            sendUsageMessage(sender, "/time <set|add|query> [value]")
            return true
        }
        
        val world = if (sender is Player) {
            sender.world
        } else {
            // 控制台默认使用主世界
            org.bukkit.Bukkit.getWorlds().firstOrNull()
        }
        
        if (world == null) {
            sendErrorMessage(sender, "无法确定目标世界。")
            return true
        }
        
        when (args[0].lowercase()) {
            "set" -> {
                if (args.size < 2) {
                    sendUsageMessage(sender, "/time set <value>")
                    return true
                }
                
                val time = parseTime(args[1])
                if (time == null) {
                    sendErrorMessage(sender, "无效的时间值: ${args[1]}")
                    sendErrorMessage(sender, "可用值: day, night, noon, midnight 或数字 (0-24000)")
                    return true
                }
                
                world.time = time
                sendSuccessMessage(sender, "已将时间设置为 $time")
            }
            
            "add" -> {
                if (args.size < 2) {
                    sendUsageMessage(sender, "/time add <value>")
                    return true
                }
                
                try {
                    val addTime = args[1].toLong()
                    world.time += addTime
                    sendSuccessMessage(sender, "已增加时间 $addTime")
                } catch (e: NumberFormatException) {
                    sendErrorMessage(sender, "时间增量必须是数字。")
                }
            }
            
            "query" -> {
                val currentTime = world.time
                val gameTime = world.fullTime
                sendSuccessMessage(sender, "当前时间: $currentTime (游戏时间: $gameTime)")
                sendSuccessMessage(sender, "时间阶段: ${getTimePhase(currentTime)}")
            }
            
            else -> {
                sendUsageMessage(sender, "/time <set|add|query> [value]")
            }
        }
        
        return true
    }
    
    private fun parseTime(input: String): Long? {
        return when (input.lowercase()) {
            "day" -> 1000L
            "noon" -> 6000L
            "night" -> 13000L
            "midnight" -> 18000L
            else -> {
                try {
                    val time = input.toLong()
                    if (time in 0..24000) time else null
                } catch (e: NumberFormatException) {
                    null
                }
            }
        }
    }
    
    override fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "minecraft.command.time")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> filterCompletions(args, listOf("set", "add", "query"))
            2 -> {
                when (args[0].lowercase()) {
                    "set" -> filterCompletions(args, listOf("day", "noon", "night", "midnight", "0", "1000", "6000", "13000", "18000"))
                    "add" -> filterCompletions(args, listOf("1000", "6000", "12000"))
                    else -> emptyList()
                }
            }
            else -> emptyList()
        }
    }

    private fun getTimePhase(time: Long): String {
        val normalizedTime = time % 24000
        return when {
            normalizedTime < 6000 -> "白天"
            normalizedTime < 12000 -> "中午"
            normalizedTime < 18000 -> "傍晚"
            else -> "夜晚"
        }
    }
}