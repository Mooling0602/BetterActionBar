# BetterActionBar

Fabric client mod that adds newline support for actionbar messages containing `\n`.

为 actionbar 消息内容添加换行支持的Fabric客户端模组。

## Version Compatibility / 版本兼容
- Minecraft `26.1.x` (default build target): Fabric Loader `>=0.19.1`, Java 25
- Minecraft `1.21.1`: Fabric Loader `>=0.16.14`, Java 21
- Minecraft `1.20.6`: Fabric Loader `>=0.15.11`, Java 21

Build target can be switched with Gradle property `mcTarget` (for example: `./gradlew build -PmcTarget=1.21.1`).

可通过 Gradle 属性 `mcTarget` 切换构建目标（例如：`./gradlew build -PmcTarget=1.21.1`）。

## Usage / 用法
Install for your game version, it just works.

根据你的游戏版本安装即可。

## Notes / 说明
- `BetterActionBarClient` intentionally keeps an empty `onInitializeClient()` implementation.
- The functional behavior is provided by client mixins; the entrypoint exists for explicit client-side wiring and future extensibility.
- `BetterActionBarClient` 有意保持空实现；实际功能由客户端 mixin 提供，entrypoint 用于明确客户端装配与后续扩展。

## Thanks to / 感谢
- GitHub Copilot: Helps me to make this cool mod.

## License

GPL-3.0
