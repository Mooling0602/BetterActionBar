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

## Configuration / 配置
The mod reads `config/betteractionbar.json` on the client.

Supported keys:
- `LineSpacingMultiplier`: extra spacing added to each text line. Default: `0.1`
- `NewLineBreak`: a list of strings as custom newline tokens that will be normalized to `\n`

Example:
```json
{
  "LineSpacingMultiplier": 0.1,
  "NewLineBreak": []
}
```

模组会读取客户端的 `config/betteractionbar.json`。

支持的字段：
- `LineSpacingMultiplier`：每行文字额外增加的行距倍数。默认值：`0.1`
- `NewLineBreak`：自定义换行符（字符串）列表，会被统一转换为 `\n`

示例：
```json
{
  "LineSpacingMultiplier": 0.1,
  "NewLineBreak": []
}
```

## Notes / 说明
- `BetterActionBarClient` reloads config in `onInitializeClient()`.
- Actionbar rendering behavior is provided by client mixins.
- `BetterActionBarClient` 会在 `onInitializeClient()` 中重载配置；actionbar 渲染行为由客户端 mixin 提供。

## Thanks to / 感谢
- GitHub Copilot: Helps me to make this cool mod.

## License

GPL-3.0
