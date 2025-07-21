package cc.vastsea.vanillacmdbridge.commands

import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

abstract class BaseCommand : CommandExecutor, TabCompleter {

    protected fun hasPermission(sender: CommandSender, permission: String): Boolean {
        return sender.hasPermission(permission)
    }
    
    protected fun sendNoPermissionMessage(sender: CommandSender) {
        sender.sendMessage("§c你没有权限使用此命令。")
    }
    
    protected fun sendUsageMessage(sender: CommandSender, usage: String) {
        sender.sendMessage("§c用法: $usage")
    }
    
    protected fun sendErrorMessage(sender: CommandSender, message: String) {
        sender.sendMessage("§c$message")
    }
    
    protected fun sendSuccessMessage(sender: CommandSender, message: String) {
        sender.sendMessage("§a$message")
    }
    
    protected fun requirePlayer(sender: CommandSender): Player? {
        if (sender !is Player) {
            sender.sendMessage("§c此命令只能由玩家执行。")
            return null
        }
        return sender
    }
    
    protected fun getOnlinePlayer(name: String): Player? {
        return org.bukkit.Bukkit.getPlayer(name)
    }

    /**
     * 解析三个坐标参数
     * @param args 参数数组
     * @param startIndex 开始解析的索引位置
     * @return 解析成功返回坐标数组[x, y, z]，失败返回null
     */
    protected fun parseCoordinates(args: Array<out String>, startIndex: Int = 0): DoubleArray? {
        if (args.size < startIndex + 3) return null

        val x = args[startIndex].toDoubleOrNull()
        val y = args[startIndex + 1].toDoubleOrNull()
        val z = args[startIndex + 2].toDoubleOrNull()

        return if (x != null && y != null && z != null) {
            doubleArrayOf(x, y, z)
        } else {
            null
        }
    }

    /**
     * 创建新的Location对象
     * @param baseLocation 基础位置(用于获取世界信息)
     * @param coordinates 坐标数组[x, y, z]
     * @return 新的Location对象
     */
    protected fun createLocationFromCoordinates(baseLocation: Location, coordinates: DoubleArray): Location {
        val location = baseLocation.clone()
        location.x = coordinates[0]
        location.y = coordinates[1]
        location.z = coordinates[2]
        return location
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        return getTabCompletions(sender, command, alias, args)
    }

    abstract fun getTabCompletions(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>

    protected fun getOnlinePlayerNames(): List<String> {
        return org.bukkit.Bukkit.getOnlinePlayers().map { it.name }
    }

    protected fun filterCompletions(args: Array<out String>, completions: List<String>): List<String> {
        if (args.isEmpty()) return completions
        val partial = args.last().lowercase()
        return completions.filter { it.lowercase().startsWith(partial) }
    }
}