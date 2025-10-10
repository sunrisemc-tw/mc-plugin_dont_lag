# DontLag - Minecraft 伺服器優化插件

[![Build Plugin](https://github.com/sunrisemc-tw/mc-plugin_dont_lag/actions/workflows/build.yml/badge.svg)](https://github.com/sunrisemc-tw/mc-plugin_dont_lag/actions/workflows/build.yml)

## 📖 簡介

DontLag 是一個專為 Minecraft 伺服器設計的優化插件，透過關閉生物的 AI 功能來減輕伺服器負擔，有效緩解卡頓問題。

## ✨ 主要功能

- **AI 控制工具**：使用木棒作為控制工具，輕鬆管理生物的 AI 狀態
- **保留基本功能**：關閉 AI 後，生物仍能執行基本互動（如村民的交易和補貨）
- **即時切換**：右鍵點擊生物即可切換其 AI 狀態
- **效能優化**：減少伺服器 AI 運算負擔，提升整體效能

## 🎮 指令

| 指令 | 描述 | 權限 |
|------|------|------|
| `/delag set` | 切換 AI 控制工具模式 | `dontlag.admin` |
| `/delag info` | 查看插件資訊和統計 | `dontlag.use` |
| `/delag reload` | 重新載入配置文件 | `dontlag.admin` |

**別名**: `/dl`, `/antilag`

## 🔧 使用方法

1. 使用指令 `/delag set` 啟用 AI 控制工具模式
2. 系統會自動給予你一支特殊的木棒（AI 控制工具）
3. 使用木棒右鍵點擊任何生物來切換其 AI 狀態
4. 左鍵點擊生物可查看其當前 AI 狀態

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
```

## 🎯 功能範例

### 村民優化
- **關閉 AI 前**：村民會到處走動、尋找工作站、社交等
- **關閉 AI 後**：村民保持在原地，但仍可正常交易和補貨

### 其他生物
- **動物（牛、羊、雞等）**：關閉 AI 後不會移動，但可正常繁殖
- **敵對生物**：關閉 AI 後不會主動攻擊或移動
- **鐵魔像**：關閉 AI 後保持原地，但外觀和基本特性不變

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