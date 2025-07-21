package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class WeatherCommand : BaseCommand() {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!hasPermission(sender, "minecraft.command.weather")) {
            sendNoPermissionMessage(sender)
            return true
        }
        
        if (args.isEmpty()) {
            sendUsageMessage(sender, "/weather <clear|rain|thunder> [duration]")
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
        
        val duration = if (args.size >= 2) {
            try {
                val dur = args[1].toInt()
                if (dur <= 0) {
                    sendErrorMessage(sender, "持续时间必须大于 0")
                    return true
                }
                dur * 20 // 转换为tick
            } catch (e: NumberFormatException) {
                sendErrorMessage(sender, "持续时间必须是数字（秒）。")
                return true
            }
        } else {
            6000 // 默认5分钟
        }
        
        when (args[0].lowercase()) {
            "clear", "sun" -> {
                world.setStorm(false)
                world.isThundering = false
                world.weatherDuration = duration
                sendSuccessMessage(sender, "已将天气设置为晴天")
            }
            
            "rain" -> {
                world.setStorm(true)
                world.isThundering = false
                world.weatherDuration = duration
                sendSuccessMessage(sender, "已将天气设置为雨天")
            }
            
            "thunder", "storm" -> {
                world.setStorm(true)
                world.isThundering = true
                world.weatherDuration = duration
                world.thunderDuration = duration
                sendSuccessMessage(sender, "已将天气设置为雷雨")
            }
            
            else -> {
                sendErrorMessage(sender, "无效的天气类型: ${args[0]}")
                sendErrorMessage(sender, "可用类型: clear, rain, thunder")
            }
        }
        
        return true
    }

    override fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "minecraft.command.weather")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> filterCompletions(args, listOf("clear", "rain", "thunder", "sun", "storm"))
            2 -> filterCompletions(args, listOf("60", "300", "600", "1200"))
            else -> emptyList()
        }
    }
}