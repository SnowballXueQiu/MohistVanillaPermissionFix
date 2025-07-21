# VanillaCommandBridge

一个用于 Mohist 1.20.1 + LuckPerms 的插件，让非 OP 玩家可以通过权限系统使用原版命令。

## 功能特性

✅ **完全接管原版命令** - 重写原版命令逻辑，支持权限控制  
✅ **LuckPerms 集成** - 使用标准的 `minecraft.command.*` 权限节点  
✅ **保持原版体验** - 命令格式和行为与原版完全一致  
✅ **支持多平台** - 兼容 Mohist、Paper、Purpur、Spigot  
✅ **无需 OP** - 通过权限系统精确控制命令使用权限  

## 支持的命令

| 命令 | 别名 | 权限节点 | 功能 |
|------|------|----------|------|
| `/gamemode` | `gm` | `minecraft.command.gamemode` | 切换游戏模式 |
| `/tp` | - | `minecraft.command.tp` | 传送玩家 |
| `/give` | - | `minecraft.command.give` | 给予物品 |
| `/time` | - | `minecraft.command.time` | 设置时间 |
| `/weather` | - | `minecraft.command.weather` | 控制天气 |
| `/effect` | - | `minecraft.command.effect` | 药水效果 |

## 权限配置

### 基础权限
```yaml
# 授予所有原版命令权限
minecraft.command.*

# 或者单独授予特定命令权限
minecraft.command.gamemode
minecraft.command.tp
minecraft.command.give
minecraft.command.time
minecraft.command.weather
minecraft.command.effect
```

### LuckPerms 示例
```bash
# 给玩家授予游戏模式权限
/lp user <玩家名> permission set minecraft.command.gamemode true

# 给组授予传送权限
/lp group <组名> permission set minecraft.command.tp true

# 授予所有原版命令权限
/lp user <玩家名> permission set minecraft.command.* true
```

## 安装方法

1. 确保服务器运行 Java 17+
2. 安装 LuckPerms 插件
3. 将 `VanillaCommandBridge-1.0.0.jar` 放入 `plugins` 文件夹
4. 重启服务器
5. 使用 LuckPerms 配置权限

## 构建方法

```bash
# 克隆项目
git clone <repository-url>
cd VanillaCommandBridge

# 构建插件
./gradlew shadowJar

# 输出文件位于 build/libs/VanillaCommandBridge-1.0.0.jar
```

## 技术细节

### 为什么需要重写？

原版命令注册在 Brigadier Dispatcher 中，仅对 OP 可用。Bukkit API 无法 Hook 原版命令的权限检查，导致 LuckPerms 等权限插件无法生效。

### 解决方案

本插件通过以下方式解决问题：

1. **重新注册命令** - 在 `plugin.yml` 中注册相同的命令名
2. **权限检查** - 使用 Bukkit 权限系统检查 `minecraft.command.*` 权限
3. **功能实现** - 使用 Bukkit API 实现原版命令的功能
4. **保持兼容** - 命令格式和行为与原版保持一致

## 作者信息

- **作者**: Snowball_233
- **版本**: 1.0.0
- **适用版本**: Minecraft 1.20.1
- **依赖**: Paper API, LuckPerms (可选)

## 许可证

本项目采用 MIT 许可证，详见 LICENSE 文件。