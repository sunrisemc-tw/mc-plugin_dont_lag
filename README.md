# DontLag - Minecraft 伺服器優化插件

[![Build Plugin](https://github.com/sunrisemc-tw/mc-plugin_dont_lag/actions/workflows/build.yml/badge.svg)](https://github.com/sunrisemc-tw/mc-plugin_dont_lag/actions/workflows/build.yml)

## 📖 簡介

DontLag 是一個專為 Minecraft 伺服器設計的優化插件，透過關閉生物的 AI 功能來減輕伺服器負擔，有效緩解卡頓問題。

## ✨ 主要功能

- **AI 控制工具**：使用木棒作為控制工具，輕鬆管理生物的 AI 狀態
- **村民優化工具**：專門針對村民的優化功能，移除代理（gossip）和尋路等負擔功能
- **🔥 自動村民優化**：當某區域村民超過閾值時，自動永久優化（預設5隻）
- **🛡️ OP 管理員棒**：管理員專用工具，可解除永久優化的村民（避免誤鎖）
- **⚡ TPS 自動保護**：當伺服器卡頓時自動停止計算並廣播訊息
- **保留基本功能**：關閉 AI 後，生物仍能執行基本互動（如村民的交易和補貨）
- **即時切換**：右鍵點擊生物即可切換其 AI/優化狀態
- **智能追蹤**：基於區塊的高效追蹤系統，避免大量計算
- **效能優化**：減少伺服器 AI 運算負擔，提升整體效能

## 🎮 指令

| 指令 | 描述 | 權限 |
|------|------|------|
| `/delag ai set` | 切換 AI 控制工具模式 | `dontlag.admin` |
| `/delag villager set` | 切換村民優化工具模式 | `dontlag.admin` |
| `/delag op set` | 切換 OP 管理員棒模式（解除永久優化） | `dontlag.admin` |
| `/delag info` | 查看插件資訊和統計 | `dontlag.use` |
| `/delag reload` | 重新載入配置文件 | `dontlag.admin` |

**別名**: `/dl`, `/antilag`

## 🔧 使用方法

### AI 控制工具
1. 使用指令 `/delag ai set` 啟用 AI 控制工具模式
2. 系統會自動給予你一支特殊的木棒（AI 控制工具）
3. 使用木棒右鍵點擊任何生物來切換其 AI 狀態
4. 左鍵點擊可查看使用說明和統計資訊

### 村民優化工具
1. 使用指令 `/delag villager set` 啟用村民優化工具模式
2. 系統會自動給予你一支特殊的木棒（村民優化工具）
3. 使用木棒右鍵點擊村民來優化其功能
4. 左鍵點擊可查看使用說明和統計資訊

### OP 管理員棒（解除永久優化）
1. 使用指令 `/delag op set` 啟用 OP 管理員棒模式
2. 系統會自動給予你一支特殊的木棒（OP 管理員棒）
3. 使用木棒右鍵點擊被自動優化的村民來解除其永久優化
4. 適用於誤鎖定或需要恢復村民正常行為的情況

## 🔑 權限

- `dontlag.admin` - 管理員權限（設定工具、重載配置）
- `dontlag.use` - 使用基本功能（查看資訊、使用工具）

預設情況下，這些權限只給予 OP 玩家。

## 📦 安裝

1. 從 [Releases](https://github.com/sunrisemc-tw/mc-plugin_dont_lag/releases) 下載最新版本
2. 將 `DontLag-x.x.x.jar` 放入伺服器的 `plugins` 資料夾
3. 重啟伺服器
4. 使用 `/delag` 指令開始使用

## 🛠️ 編譯

此專案使用 Maven 進行建置，並透過 GitHub Actions 自動編譯。

### 手動編譯

```bash
# 克隆專案
git clone https://github.com/sunrisemc-tw/mc-plugin_dont_lag.git
cd mc-plugin_dont_lag

# 使用 Maven 編譯
mvn clean package

# 編譯完成的 JAR 檔案位於 target/DontLag-*.jar
```

### 系統需求

- Java 17 或更高版本
- Maven 3.6 或更高版本
- Spigot/Paper 1.20.1 或更高版本

## 📝 配置文件

插件首次啟動時會自動生成 `config.yml`：

```yaml
settings:
  # 是否在生物 AI 被禁用時顯示標記
  show-ai-disabled-indicator: true
  
  # 伺服器重啟後是否保留 AI 禁用狀態
  persist-ai-state: false

# 自動村民優化設定
auto-optimize:
  # 是否啟用自動村民優化
  enabled: true
  
  # 村民數量閾值（當某個區塊的村民超過此數量時自動優化）
  threshold: 5
  
  # 檢查間隔（秒）- 建議 30-60 秒
  check-interval: 30

# TPS 監控設定
tps-monitor:
  # 是否啟用 TPS 監控自動停止機制
  enabled: true
  
  # TPS 閾值（當平均 TPS 低於此值時停止村民計算）
  threshold: 15.0
```

### 🤖 自動村民優化說明

- **工作原理**：系統會追蹤每個區塊（16x16）的村民數量
- **觸發條件**：當某個區塊的村民數量超過閾值時自動優化
- **優化效果**：移除代理(gossip)、尋路、記憶系統，只保留交易和補貨
- **永久性**：自動優化的村民不會在重啟後恢復（減輕伺服器負擔）
- **解除方式**：管理員可使用 `/delag op set` 獲得 OP 管理員棒來解除永久優化
- **性能影響**：使用異步檢查和事件驅動，對伺服器性能影響極小

### ⚡ TPS 自動保護機制

- **監控機制**：每秒檢查一次伺服器 TPS
- **自動停止**：當 TPS 低於閾值（預設15.0）時自動停止村民計算
- **廣播通知**：系統會向所有玩家廣播卡頓訊息
- **手動恢復**：管理員可使用 `/delag reload` 重新啟動
- **智能保護**：避免插件本身成為卡頓源

## 🎯 功能範例

### AI 控制工具
適用於所有生物（村民、動物、敵對生物等）
- **關閉 AI 前**：生物會正常移動、尋路、執行各種行為
- **關閉 AI 後**：生物保持在原地，但保留基本互動功能

### 村民優化工具（專用）
專門針對村民的深度優化
- **優化前**：村民會到處走動、尋找工作站、社交（gossip）、尋路等
- **優化後**：村民只保留交易和補貨功能，移除：
  - 代理系統（gossip）
  - 尋路行為
  - 工作站尋找
  - 社交互動
  - 記憶系統

### 效能對比
- **一般生物**：使用 AI 控制工具，減少約 50-70% 的運算負擔
- **村民（手動）**：使用村民優化工具，減少約 80-90% 的運算負擔
- **村民（自動）**：自動優化大型村民交易所，減少約 80-90% 的運算負擔（推薦！）

### 🎯 推薦配置

| 場景 | 建議閾值 | 檢查間隔 |
|------|---------|---------|
| 小型伺服器（< 10 玩家） | 8-10 隻 | 60 秒 |
| 中型伺服器（10-50 玩家） | 5-8 隻 | 30-45 秒 |
| 大型伺服器（> 50 玩家） | 3-5 隻 | 30 秒 |
| 超大型交易所 | 5 隻 | 20-30 秒 |

## 🤝 貢獻

歡迎提交 Issue 和 Pull Request！

## 📜 授權

此專案採用 MIT 授權條款 - 詳見 [LICENSE](LICENSE) 文件

## 👥 作者

**SunriseMC Team**

## 🐛 問題回報

如果您發現任何問題，請在 [GitHub Issues](https://github.com/sunrisemc-tw/mc-plugin_dont_lag/issues) 提出。

## 📊 版本資訊

- **當前版本**: 1.0.0
- **支援版本**: Minecraft 1.20.1+
- **API 版本**: 1.20