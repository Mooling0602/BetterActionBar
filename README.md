# BetterActionBar

Fabric client mod that adds newline support for actionbar messages containing `\n`.

为 actionbar 消息内容添加换行支持的Fabric客户端模组。

## Version Compatibility / 版本兼容
- Minecraft `26.2` (snapshot versions): Fabric Loader `>=0.19.1`, Java 25
- Minecraft `26.1.x` (latest stable): Fabric Loader `>=0.19.1`, Java 25
- Minecraft `1.21.6`: Fabric Loader `>=0.16.14`, Java 21
- Minecraft `1.21.1`: Fabric Loader `>=0.16.14`, Java 21
- Minecraft `1.20.6`: Fabric Loader `>=0.15.11`, Java 21

If your game version is not listed above, choose the closest lower target version (i.e., the latest version that is still lower than or equal to your game version).

For example: if your client version is `1.21.11`, use `1.21.6` (but not `1.21.1`).

如果你的游戏版本不在上述列表中，请选择离你游戏版本最近且比它更低的目标版本（即不高于你游戏版本的最新版本）

例如：若你的客户端版本为 `1.21.11`，请使用 `1.21.6`（而非 `1.21.1`）。

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

> 经过测试，在 NeoVim（nvim-jdtls）和 Zed 编辑器（同样使用 jdtls 作为 LSP ）中，自动补全将正常工作，但 IntelliJ IDEA 中仍会报错且无法使用。

For different game versions, the mod needs to provide actionbar rendering hooks via Mixin for the core module to call.

To obtain the upstream Minecraft interfaces for the target game version in an IDE/LSP to develop Mixin implementations, you need to temporarily modify `gradle.properties` to set the target version to your desired one.

Then run `./gradlew cleanEclipse eclipse`, and restart your LSP or IDE. You should then have correct module and interface auto-completion.

> As I tested, auto-completion will work fine in NeoVim (nvim-jdtls) and Zed (also use jdtls as LSP), but not in IntelliJ IDEA.

## Thanks to / 感谢
- GitHub Copilot: Helps me to create this cool mod.

## License

GPL-3.0
