# BetterActionBar

Fabric client mod that adds newline support for actionbar messages containing `\n`.

为 actionbar 消息内容添加换行支持的Fabric客户端模组。

## Version Compatibility / 版本兼容
- Minecraft `26.2` (Snapshot versions): Fabric Loader `>=0.19.1`, Java 25
- Minecraft `26.1.x` (default build target): Fabric Loader `>=0.19.1`, Java 25
- Minecraft `1.21.1`: Fabric Loader `>=0.16.14`, Java 21
- Minecraft `1.20.6`: Fabric Loader `>=0.15.11`, Java 21

Build target can be switched with Gradle property `mcTarget` (for example: `./gradlew build -PmcTarget=1.21.1
> Default `mcTarget` value is `26.2` now.

可通过 Gradle 属性 `mcTarget` 切换构建目标（例如：`./gradlew build -PmcTarget=1.21.1`）。
> 现在默认为`26.2`快照版本编译插件。

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

## Develop / 开发
> This part is written by Chinese Simplified firstly.
>
> Translation may be generated from AI, or PRs for this are welcomed.
对于不同的游戏版本，插件需要通过 Mixin 提供 actionbar 的渲染接口，以便核心模块调用。

要想在 IDE/LSP 中针对不同游戏版本获取到来自 Minecraft 的上游接口以开发 Mixin 实现，需要临时性的修改 `gradle.properties`，使目标版本和你的预期一致。

然后运行 `./gradlew cleanEclipse eclipse`，并重启 LSP 或 IDE，你应该可以获取到正确的模块和接口补全。

## Thanks to / 感谢
- GitHub Copilot: Helps me to create this cool mod.

## License

GPL-3.0
