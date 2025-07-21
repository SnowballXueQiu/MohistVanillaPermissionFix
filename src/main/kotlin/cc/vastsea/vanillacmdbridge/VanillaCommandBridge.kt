package cc.vastsea.vanillacmdbridge

import cc.vastsea.vanillacmdbridge.commands.*
import org.bukkit.plugin.java.JavaPlugin

class VanillaCommandBridge : JavaPlugin() {
    
    override fun onEnable() {
        logger.info("VanillaCommandBridge 正在启用...")
        
        // 注册命令
        registerCommands()
        
        logger.info("VanillaCommandBridge 已成功启用!")
        logger.info("作者: Snowball_233")
        @Suppress("DEPRECATION")
        logger.info("版本: ${description.version}")
    }
    
    override fun onDisable() {
        logger.info("VanillaCommandBridge 已禁用!")
    }
    
    private fun registerCommands() {
        // 定义所有命令的映射关系
        val commands = mapOf(
            "gamemode" to GamemodeCommand(),
            "tp" to TeleportCommand(),
            "give" to GiveCommand(),
            "time" to TimeCommand(),
            "weather" to WeatherCommand(),
            "effect" to EffectCommand(),
            "clear" to ClearCommand(),
            "experience" to ExperienceCommand(),
            "fill" to FillCommand(),
            "kill" to KillCommand(),
            "setblock" to SetBlockCommand(),
            "summon" to SummonCommand()
        )

        // 批量注册命令
        var registeredCount = 0
        commands.forEach { (commandName, executor) ->
            if (registerCommand(commandName, executor)) {
                registeredCount++
            }
        }

        logger.info("已注册 $registeredCount 个原版命令的权限桥接")
    }

    /**
     * 注册单个命令
     * @param commandName 命令名称
     * @param executor 命令执行器
     * @return 注册是否成功
     */
    private fun registerCommand(commandName: String, executor: BaseCommand): Boolean {
        return getCommand(commandName)?.let { command ->
            command.setExecutor(executor)
            command.tabCompleter = executor
            true
        } ?: run {
            logger.warning("无法注册命令: $commandName (可能在 plugin.yml 中未定义)")
            false
        }
    }
}