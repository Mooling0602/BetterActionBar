# Release 规范

**注意**：各分支独立运行这套规范。

1. 发行提交信息使用 `release: v<version>+<branch>`。
> e.g. `release: v1.0.0+26.1`
>
> 该次提交，应仅修改插件元数据等处的版本号，不应该有其他内容。代码变更等工作应在此提交前完成并推送。

2. 发行提交完成后，记录该提交编号前七位（short SHA）。

3. 使用 `git tag -a <version>+<branch> -m "commit <short SHA>"` 创建版本 tag。
> e.g. `git tag -a 1.0.0+26.1 -m "commit 1234567"`

4. 使用 `git push origin <branch> <version>+<branch>` 推送新版本代码与标签，在此处触发CI。正常的代码变更不触发CI。
> e.g. `git push origin 26.1 1.0.0+26.1`

5. CI 根据 `CHANGELOG.md` 的二级标题 `## <version>` 提取对应版本内容并生成 release，release 标题使用 `v<version>+<branch>`；若未找到内容则发布空正文（不失败）。
