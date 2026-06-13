<div align="center">
  <!-- English / 中文 toggle -->
  <sup>
    <a href="#creator-os-lite">English</a> • <a href="#creator-os-lite-1">中文</a>
  </sup>

  <h1>🛡️ Creator OS Lite</h1>
  <p><strong>Risk Control Warning Radar for Freelance Professionals</strong><br>
  <em>自由职业者风险控制预警雷达</em></p>

  <p>
    <a href="https://github.com/Marnie0415/creator-os-lite/releases">
      <img src="https://img.shields.io/github/v/release/Marnie0415/creator-os-lite?label=Release&color=34C759" />
    </a>
    <a href="https://github.com/Marnie0415/creator-os-lite/actions">
      <img src="https://img.shields.io/github/actions/workflow/status/Marnie0415/creator-os-lite/build.yml?label=Build&color=34C759" />
    </a>
    <a href="LICENSE">
      <img src="https://img.shields.io/badge/License-MIT-green" />
    </a>
    <a href="https://github.com/Marnie0415/creator-os-lite/issues">
      <img src="https://img.shields.io/github/issues/Marnie0415/creator-os-lite?color=FF9500" />
    </a>
  </p>

  <p>
    <a href="#features">Features</a> •
    <a href="#supported-ai-providers">AI Providers</a> •
    <a href="#quick-start">Quick Start</a> •
    <a href="#build">Build</a> •
    <a href="#tech-stack">Tech Stack</a> •
    <a href="#license">License</a>
  </p>
</div>

---

<a id="creator-os-lite"></a>
## 📖 English

### What is Creator OS Lite?

Creator OS Lite is a **privacy-first, open-source Android app** built for independent professionals — freelancers, contractors, solo creators — who need to track clients, projects, and invoices without getting lost in spreadsheets.

Think of it as a **financial risk radar for your freelance business**. It watches your incoming payments, flags overdue invoices, alerts you when clients go silent, and helps you write professional follow-up messages — all powered by the AI model of your choice.

### Who is this for?

- **Freelancers** juggling multiple clients and projects
- **Solo developers** who need payment reminders
- **Creative professionals** (designers, writers, artists) managing client work
- **Independent contractors** wanting a simple, private CRM
- **Anyone tired of chasing late payments**

### How it Works

```
   Create Client → Link Projects → Track Invoices
                                          ↓
   Risk Engine monitors: Ghosting · Overdue · Expiring Deadlines
                                          ↓
   Dashboard alerts you → AI writes follow-up → Get Paid 💰
```

### Features

| Feature | Description |
|---------|-------------|
| 📊 **Risk Radar Dashboard** | Real-time overview with color-coded warnings (Critical/High/Medium) |
| 👥 **Client Management** | Contact channels (Discord, Telegram, Email, WhatsApp) + timeline logging |
| 📋 **Project & Invoice Tracking** | Linked project-invoice pairs with status flow: Unpaid → Deposit → Balance → Paid |
| 🤖 **AI Payment Follow-up Writer** | Generate professional reminders in Friendly / Professional / Firm tones |
| 🤖 **AI Quote Assistant** | Structure raw client requirements into polished markdown proposals |
| 🔒 **Privacy-First** | All data stays on-device. No accounts. No cloud. No telemetry. |
| 🌐 **Multi-Provider AI** | Bring your own API key from Gemini, OpenAI, DeepSeek, Claude, etc. |
| 🌏 **Bilingual UI** | English + Chinese (auto-switches based on system language) |

### Supported AI Providers

| Provider | Models | Get API Key |
|----------|--------|-------------|
| **Google Gemini** | gemini-2.5-flash, gemini-2.5-pro | [ai.google.dev](https://ai.google.dev/) |
| **OpenAI** | gpt-4o, gpt-4o-mini, o3-mini | [platform.openai.com](https://platform.openai.com/) |
| **DeepSeek** | deepseek-chat, deepseek-reasoner | [platform.deepseek.com](https://platform.deepseek.com/) |
| **Groq** | Llama 3.3 (70B), DeepSeek R1 | [console.groq.com](https://console.groq.com/) |
| **Together AI** | Llama 3.3, Mixtral | [api.together.xyz](https://api.together.xyz/) |
| **OpenRouter** | 200+ models unified | [openrouter.ai](https://openrouter.ai/) |
| **Perplexity** | Sonar Pro, Sonar Reasoning | [perplexity.ai](https://perplexity.ai/) |
| **Anthropic Claude** | Sonnet 4, Haiku 3.5, Opus 4 | [console.anthropic.com](https://console.anthropic.com/) |
| **Any OpenAI-compatible** | Custom model | Set custom base URL in Settings |

### Quick Start

#### Option A: Download APK
Download the latest APK from the [Releases page](https://github.com/Marnie0415/creator-os-lite/releases).

#### Option B: Build from Source

**Prerequisites:** Android Studio Ladybug+ · JDK 17 · Android SDK 36

```bash
git clone https://github.com/Marnie0415/creator-os-lite.git
cd creator-os-lite

# Debug build
./gradlew assembleDebug

# APK output: app/build/outputs/apk/debug/app-debug.apk
```

Open in Android Studio → File → Open → select `creator-os-lite` folder → Sync → Run.

#### Configuring AI

1. Open the app → go to **Settings** (gear icon)
2. Tap **AI Provider** card
3. Select your provider (Gemini, OpenAI-compatible, or Claude)
4. Enter your API key and select a model
5. Tap **Save Configuration**

### Build

```bash
# Debug APK (no signing)
./gradlew assembleDebug

# Release APK (requires your keystore)
./gradlew assembleRelease

# Run tests
./gradlew test
```

### Project Structure

```
app/src/main/java/com/example/
├── MainActivity.kt           # Entry point + 5-tab navigation
├── CreatorOSApplication.kt   # App initialization
├── ai/                       # Multi-provider AI system
│   ├── AiProvider.kt         # Provider types & service interface
│   ├── GeminiService.kt      # Google Gemini implementation
│   ├── OpenAiService.kt      # OpenAI-compatible (DeepSeek, Groq, etc.)
│   ├── AnthropicService.kt   # Claude implementation
│   ├── AiServiceManager.kt   # Factory & caching
│   ├── AiViewModel.kt        # AI feature state management
│   └── AiScreen.kt           # AI feature UI
├── client/                   # Client CRUD + timeline
├── project/                  # Project management
├── invoice/                  # Invoice management
├── dashboard/                # Risk radar dashboard
│   ├── RiskEngine.kt         # Core risk calculation logic
│   ├── DashboardViewModel.kt
│   └── DashboardScreen.kt
├── data/                     # Room database
├── settings/                 # Settings screen
└── ui/                       # Theme, utilities, i18n
```

### Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | **Kotlin** 2.2 |
| UI Framework | **Jetpack Compose** + Material 3 |
| Architecture | **MVVM** (ViewModel + StateFlow) |
| Database | **Room** (local SQLite) |
| Networking | **Retrofit** + OkHttp + Moshi |
| AI Integration | **Multi-provider** — Gemini / OpenAI / Anthropic |
| Build System | Gradle KTS, compileSdk 36, minSdk 24 |
| Security | Network security config, ProGuard/R8 |
| Testing | JUnit, Robolectric, Roborazzi (screenshot tests) |
| CI | GitHub Actions (auto-build APK on push) |

### Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/awesome`)
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

### License

Distributed under the **MIT License**. See [LICENSE](LICENSE).

---

<a id="creator-os-lite-1"></a>
## 📖 中文说明

### Creator OS Lite 是什么？

**Creator OS Lite** 是一个**隐私优先、完全开源**的 Android 应用，专为自由职业者、独立开发者、创意工作者设计，帮助你轻松管理客户、项目和发票。

它就像一个**自由职业财务风险雷达**——实时监控你的收款状况，在客户逾期付款、失联、项目即将到期时自动发出预警。还内置 AI 助手，帮你自动生成催款文案和报价方案。

### 适合谁用？

- **自由职业者** — 同时管理多个客户和项目
- **独立开发者** — 需要收款提醒的工具
- **创意工作者**（设计师、作家、艺术家）— 管理客户委托
- **独立承包商** — 想要一个简单、私密的客户管理系统
- **任何厌倦了追款的人**

### 工作流程

```
   创建客户 → 关联项目 → 追踪发票
                                ↓
   风控引擎监控：失联 · 逾期 · 即将到期
                                ↓
   仪表盘预警 → AI 生成催款 → 收到报酬 💰
```

### 功能特性

| 功能 | 说明 |
|:-----|:------|
| 📊 **风险雷达仪表盘** | 实时概览，红/橙/绿三色预警 |
| 👥 **客户管理** | 多渠道联系（Discord/Telegram/微信/邮件）+ 沟通时间线 |
| 📋 **项目与发票追踪** | 状态流转：未付 → 已付定金 → 尾款 → 已结清 |
| 🤖 **AI 催款文案生成** | 友好 / 专业 / 坚定 三种语气 |
| 🤖 **AI 报价助手** | 将客户需求整理为专业报价方案 |
| 🔒 **隐私优先** | 所有数据存本地，无需注册，无云端同步，无追踪 |
| 🌐 **多 AI 供应商** | 支持 Gemini、OpenAI、DeepSeek、Claude 等 |
| 🌏 **中英双语** | 根据系统语言自动切换 |

### 支持的 AI 供应商

| 供应商 | 推荐模型 | 获取 API 密钥 |
|:-------|:---------|:-------------|
| **Google Gemini** | gemini-2.5-flash | [ai.google.dev](https://ai.google.dev/) |
| **OpenAI** | gpt-4o-mini | [platform.openai.com](https://platform.openai.com/) |
| **DeepSeek** | deepseek-chat | [platform.deepseek.com](https://platform.deepseek.com/) |
| **Groq** | llama-3.3-70b | [console.groq.com](https://console.groq.com/) |
| **Anthropic Claude** | claude-sonnet-4 | [console.anthropic.com](https://console.anthropic.com/) |
| **任何兼容 OpenAI 的 API** | 自定义 | 在设置中配置 Base URL |

在 **设置 → AI 供应商** 中选择并配置。

### 快速开始

#### 方式 A：下载 APK
从 [Releases 页面](https://github.com/Marnie0415/creator-os-lite/releases) 下载最新 APK。

#### 方式 B：源码构建

**前提条件：** Android Studio Ladybug+、JDK 17、Android SDK 36

```bash
git clone https://github.com/Marnie0415/creator-os-lite.git
cd creator-os-lite

# 构建调试版
./gradlew assembleDebug

# APK 位置: app/build/outputs/apk/debug/app-debug.apk
```

用 Android Studio 打开 → File → Open → 选择 `creator-os-lite` 文件夹 → 同步 → 运行。

#### 配置 AI

1. 打开应用 → 进入**设置**（齿轮图标）
2. 点击 **AI 供应商** 卡片
3. 选择供应商（Gemini、OpenAI 兼容 或 Claude）
4. 输入 API 密钥并选择模型
5. 点击**保存配置**

### 构建命令

```bash
# 调试 APK（无需签名）
./gradlew assembleDebug

# 发布 APK（需要你的签名文件）
./gradlew assembleRelease

# 运行测试
./gradlew test
```

### 项目结构

```
app/src/main/java/com/example/
├── MainActivity.kt           # 入口 + 5 标签导航
├── CreatorOSApplication.kt   # 应用初始化
├── ai/                       # 多供应商 AI 系统
│   ├── AiProvider.kt         # 供应商类型 & 服务接口
│   ├── GeminiService.kt      # Google Gemini 实现
│   ├── OpenAiService.kt      # OpenAI 兼容实现（DeepSeek, Groq 等）
│   ├── AnthropicService.kt   # Claude 实现
│   ├── AiServiceManager.kt   # 工厂 & 缓存
│   ├── AiViewModel.kt        # AI 功能状态管理
│   └── AiScreen.kt           # AI 功能界面
├── client/                   # 客户管理 + 时间线
├── project/                  # 项目管理
├── invoice/                  # 发票管理
├── dashboard/                # 风险雷达仪表盘
│   ├── RiskEngine.kt         # 核心风控算法
│   ├── DashboardViewModel.kt
│   └── DashboardScreen.kt
├── data/                     # Room 数据库
├── settings/                 # 设置界面
└── ui/                       # 主题、工具、国际化
```

### 技术栈

| 层级 | 技术 |
|:-----|:-----|
| 语言 | **Kotlin** 2.2 |
| UI 框架 | **Jetpack Compose** + Material 3 |
| 架构 | **MVVM**（ViewModel + StateFlow）|
| 数据库 | **Room**（本地 SQLite）|
| 网络 | **Retrofit** + OkHttp + Moshi |
| AI 集成 | **多供应商** — Gemini / OpenAI / Anthropic |
| 构建 | Gradle KTS, compileSdk 36, minSdk 24 |
| 安全 | 网络安全配置、ProGuard/R8 混淆 |
| 测试 | JUnit、Robolectric、Roborazzi（截图测试）|
| CI | GitHub Actions（每次推送自动构建 APK）|

### 贡献指南

详见 [CONTRIBUTING.md](CONTRIBUTING.md)。

1. Fork 本仓库
2. 创建功能分支 (`git checkout -b feature/awesome`)
3. 提交你的修改
4. 推送分支
5. 发起 Pull Request

### 开源许可证

基于 **MIT 许可证** 发布。详见 [LICENSE](LICENSE)。

---

<div align="center">
  <p>Built with ❤️ for the freelance community<br>
  <sub>为自由职业者社区倾心打造</sub></p>
  <p>
    <a href="https://github.com/Marnie0415/creator-os-lite">GitHub</a> •
    <a href="https://github.com/Marnie0415/creator-os-lite/releases">Downloads</a> •
    <a href="https://github.com/Marnie0415/creator-os-lite/issues">Issues</a>
  </p>
</div>
