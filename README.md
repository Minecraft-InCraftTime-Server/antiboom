# AntiBoom

Folia 兼容的 Paper 1.21 防爆插件，提供灵活的爆炸保护以及可选的烟花视觉效果。

## 功能概览

- 基于配置的爆炸控制：`protect` 阻止爆炸破坏方块、`allow` 允许破坏、`firework` 展示烟花效果并阻止方块破坏。
- 支持针对不同爆炸源（如苦力怕、恶魂火球、末影龙）的独立设置，苦力怕与恶魂分别触发对应脸型烟花，粒子效果经过精简。
- 可选的自定义保护：可为地图画、盔甲架、展示框（含荧光）、船（支持 `oak_boat` 等别名）、拴绳等实体逐一开关爆炸免疫，并自动保护这些挂饰背后的支撑方块。
- 新增风弹（Wind Charge）防护，防止 Breeze 风弹对指定实体造成伤害或破坏。
- 默认阻止末影龙破坏方块（爆炸及直接破坏），可通过配置调整。
- 通过 Folia 区域调度器触发烟花，确保多线程服务器环境安全。

## 构建

```powershell
mvn clean package
```

构建完成后在 `target` 目录下获取插件 JAR。

## 部署

1. 将生成的 JAR 放入服务器的 `plugins` 目录。
2. 启动或重载服务端，插件会在首次启动时生成默认配置。
3. 根据需要编辑 `plugins/AntiBoom/config.yml`，修改后可使用 `/antiboom reload`（需 `antiboom.reload` 权限）或重启以应用新配置。

## 配置说明

```yaml
# 三种模式：'protect'（阻止破坏）、'allow'（允许）、'firework'（烟花效果 + 阻止破坏）

explosions:
	creeper: 'firework'
	ghast-fireball: 'firework'
	ender-dragon: 'protect'

explosion-protection:
	enabled: true
	entities:
		painting: true
		armor_stand: true
		item_frame: true
		glow_item_frame: true
		# 船类支持 `oak_boat`、`oak_chest_boat`、`bamboo_raft` 等别名
		boat: true
		leash_knot: true

wind-charge-protection:
	enabled: true
	entities:
		painting: true
		armor_stand: true
		item_frame: true
		glow_item_frame: true
		boat: true
		leash_knot: true
```

> 未列出的实体默认遵循 Bukkit 行为；若 `entities` 留空且 `enabled: true`，插件将使用内置默认保护清单。

## Folia 兼容性

- 插件通过 `folia-supported: true` 声明兼容性。
- 烟花效果通过 Folia 的 Region Scheduler 执行，避免跨线程访问世界对象。
