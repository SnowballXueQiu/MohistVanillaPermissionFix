package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EffectCommand : BaseCommand() {
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!hasPermission(sender, "minecraft.command.effect")) {
            sendNoPermissionMessage(sender)
            return true
        }
        
        if (args.size < 2) {
            sendUsageMessage(sender, "/effect <give|clear> <player> [effect] [duration] [amplifier]")
            return true
        }
        
        val targetPlayer = getOnlinePlayer(args[1])
        if (targetPlayer == null) {
            sendErrorMessage(sender, "玩家 ${args[1]} 不在线。")
            return true
        }
        
        when (args[0].lowercase()) {
            "give" -> {
                if (args.size < 3) {
                    sendUsageMessage(sender, "/effect give <player> <effect> [duration] [amplifier]")
                    return true
                }
                
                val effectType = PotionEffectType.getByName(args[2].uppercase())
                if (effectType == null) {
                    sendErrorMessage(sender, "未知的药水效果: ${args[2]}")
                    return true
                }
                
                val duration = if (args.size >= 4) {
                    try {
                        val dur = args[3].toInt()
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
                    600 // 默认30秒
                }
                
                val amplifier = if (args.size >= 5) {
                    try {
                        val amp = args[4].toInt()
                        if (amp < 0 || amp > 255) {
                            sendErrorMessage(sender, "效果等级必须在 0-255 之间")
                            return true
                        }
                        amp
                    } catch (e: NumberFormatException) {
                        sendErrorMessage(sender, "效果等级必须是数字。")
                        return true
                    }
                } else {
                    0
                }
                
                val effect = PotionEffect(effectType, duration, amplifier)
                targetPlayer.addPotionEffect(effect)
                
                val effectName = effectType.name.lowercase().replace("_", " ")
                val durationSeconds = duration / 20
                sendSuccessMessage(sender, "已给予 ${targetPlayer.name} $effectName 效果 (等级 ${amplifier + 1}, ${durationSeconds}秒)")
                
                if (targetPlayer != sender) {
                    sendSuccessMessage(targetPlayer, "你获得了 $effectName 效果 (等级 ${amplifier + 1}, ${durationSeconds}秒)")
                }
            }
            
            "clear" -> {
                if (args.size >= 3) {
                    // 清除特定效果
                    val effectType = PotionEffectType.getByName(args[2].uppercase())
                    if (effectType == null) {
                        sendErrorMessage(sender, "未知的药水效果: ${args[2]}")
                        return true
                    }
                    
                    if (targetPlayer.hasPotionEffect(effectType)) {
                        targetPlayer.removePotionEffect(effectType)
                        val effectName = effectType.name.lowercase().replace("_", " ")
                        sendSuccessMessage(sender, "已清除 ${targetPlayer.name} 的 $effectName 效果")
                        
                        if (targetPlayer != sender) {
                            sendSuccessMessage(targetPlayer, "你的 $effectName 效果已被清除")
                        }
                    } else {
                        sendErrorMessage(sender, "${targetPlayer.name} 没有该效果")
                    }
                } else {
                    // 清除所有效果
                    val activeEffects = targetPlayer.activePotionEffects.size
                    if (activeEffects > 0) {
                        targetPlayer.activePotionEffects.forEach { effect ->
                            targetPlayer.removePotionEffect(effect.type)
                        }
                        sendSuccessMessage(sender, "已清除 ${targetPlayer.name} 的所有药水效果 (${activeEffects}个)")
                        
                        if (targetPlayer != sender) {
                            sendSuccessMessage(targetPlayer, "你的所有药水效果已被清除")
                        }
                    } else {
                        sendErrorMessage(sender, "${targetPlayer.name} 没有任何药水效果")
                    }
                }
            }
            
            else -> {
                sendErrorMessage(sender, "无效的操作: ${args[0]}")
                sendUsageMessage(sender, "/effect <give|clear> <player> [effect] [duration] [amplifier]")
            }
        }
        
        return true
    }

    override fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String> {
        if (!hasPermission(sender, "minecraft.command.effect")) {
            return emptyList()
        }

        return when (args.size) {
            1 -> filterCompletions(args, listOf("give", "clear"))
            2 -> filterCompletions(args, getOnlinePlayerNames())
            3 -> {
                when (args[0].lowercase()) {
                    "give" -> {
                        val effects = PotionEffectType.values()
                            .map { it.name.lowercase() }
                        filterCompletions(args, effects)
                    }
                    "clear" -> {
                        val effects = PotionEffectType.values()
                            .map { it.name.lowercase() }
                        filterCompletions(args, effects)
                    }
                    else -> emptyList()
                }
            }
            4 -> {
                if (args[0].lowercase() == "give") {
                    filterCompletions(args, listOf("30", "60", "300", "600"))
                } else emptyList()
            }
            5 -> {
                if (args[0].lowercase() == "give") {
                    filterCompletions(args, listOf("0", "1", "2", "3", "4"))
                } else emptyList()
            }
            else -> emptyList()
        }
    }
}