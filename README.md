<div align="center">
  <h1>🛡️ Creator OS Lite</h1>
  <p><strong>Risk Control Warning Radar for Freelance Professionals</strong></p>
  <p>Prevent late payments, track ghosted clients, and never miss a deadline.</p>
  <p>
    <a href="#features">Features</a> •
    <a href="#screenshots">Screenshots</a> •
    <a href="#getting-started">Getting Started</a> •
    <a href="#build-from-source">Build from Source</a> •
    <a href="#license">License</a>
  </p>
</div>

## Overview

Creator OS Lite is an open-source Android application that helps freelance professionals track their clients, projects, and invoices — with an intelligent risk engine that alerts you to:

- 🚨 **Ghosted clients** — no contact for 48+ hours during active projects
- 💸 **Overdue invoices** — payments past their due date
- ⏰ **Expiring projects** — deadlines within 72 hours
- ⚡ **Critical clients** — ghosted + unpaid simultaneously

## Features

### 📊 Risk Radar Dashboard
Real-time overview of your freelance business health with color-coded warnings.

### 👥 Client Management
Track clients, contact channels (Discord, Telegram, Email, WhatsApp), and interaction history via timeline logging.

### 📋 Project & Invoice Tracking
Create linked project + invoice pairs with deadlines, amounts, and status tracking (Unpaid → Deposit Paid → Pending Balance → Fully Paid).

### 🤖 AI Assistant (Multi-Provider)
- **Follow-up Writer** — Generate professional payment reminders in Friendly/Professional/Firm tones
- **Quote Assistant** — Structure raw client requirements into polished markdown proposals

**Supported AI Providers:**

| Provider | Models | How to configure |
|----------|--------|-----------------|
| **Google Gemini** | gemini-2.5-flash, gemini-2.5-pro | API key from [ai.google.dev](https://ai.google.dev/) |
| **OpenAI** | gpt-4o, gpt-4o-mini, o3-mini | API key from [platform.openai.com](https://platform.openai.com/) |
| **DeepSeek** | deepseek-chat, deepseek-reasoner | API key from [platform.deepseek.com](https://platform.deepseek.com/) |
| **Groq** | Llama 3.3, DeepSeek R1 | API key from [console.groq.com](https://console.groq.com/) |
| **Together AI** | Llama 3.3, Mixtral | API key from [api.together.xyz](https://api.together.xyz/) |
| **OpenRouter** | 200+ models | API key from [openrouter.ai](https://openrouter.ai/) |
| **Perplexity** | sonar-pro, sonar-reasoning | API key from [perplexity.ai](https://perplexity.ai/) |
| **Anthropic Claude** | claude-sonnet-4, claude-haiku-3.5 | API key from [console.anthropic.com](https://console.anthropic.com/) |
| **Any OpenAI-compatible** | Custom | Set base URL + API key in Settings |

Go to **Settings → AI Provider** to select your provider and enter your API key.

### 🔒 Privacy-First
All data stays on your device. No accounts, no cloud sync, no telemetry. AI calls go directly to your chosen provider with your own key.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Database | Room (SQLite, local-only) |
| Networking | Retrofit + OkHttp + Moshi |
| AI | Google Gemini API |
| Build | Gradle KTS, compileSdk 36, minSdk 24 |
| Testing | JUnit, Robolectric, Roborazzi |

## Screenshots

*(Add screenshots here after building)*

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) Ladybug or newer
- JDK 17+
- Android SDK 36

### Quick Start

1. Clone the repository:
   ```bash
   git clone https://github.com/Marnie0415/creator-os-lite.git
   ```

2. Open the project in Android Studio

3. Let Gradle sync complete

4. (Optional) For AI features during development, create a `.env` file in the project root:
   ```
   GEMINI_API_KEY=your_api_key_here
   ```
   Get a key at [ai.google.dev](https://ai.google.dev/)

5. Run on emulator or physical device (Android 7.0+)

### Configuring AI Provider

**In-app (recommended):**
1. Go to **Settings → AI Provider**
2. Select your provider (Gemini, OpenAI-compatible, or Anthropic Claude)
3. Enter your API key
4. Select the model (or type a custom one)
5. For OpenAI-compatible providers, optionally set a custom base URL
6. Tap **Save Configuration**

**Build-time (for developers using Gemini):**
Set `GEMINI_API_KEY` in `.env` at the project root (uses the Secrets Gradle Plugin).

## Build from Source

```bash
# Debug build (no signing required)
./gradlew assembleDebug

# Release build (requires your own keystore)
# 1. Create a keystore
# 2. Configure signing in app/build.gradle.kts or use Android Studio's signing wizard
# 3. Run:
./gradlew assembleRelease
```

The APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

## Project Structure

```
app/
├── src/main/java/com/example/
│   ├── MainActivity.kt              # Main entry + bottom navigation
│   ├── CreatorOSApplication.kt       # Application class + DI
│   ├── ai/                          # Gemini AI integration
│   │   ├── GeminiApiClient.kt       # API client (Retrofit)
│   │   ├── AiScreen.kt             # AI UI (Follow-up + Quote)
│   │   └── AiViewModel.kt          # AI state management
│   ├── client/                      # Client CRUD
│   ├── project/                     # Project management
│   ├── invoice/                     # Invoice management
│   ├── dashboard/                   # Dashboard + risk engine
│   │   ├── DashboardScreen.kt
│   │   ├── DashboardViewModel.kt
│   │   └── RiskEngine.kt           # Core risk calculation
│   ├── data/                        # Room database
│   ├── settings/                    # Settings screen
│   └── ui/                          # Theme, utilities
└── src/test/                        # Unit tests
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.

---

*Built with ❤️ for the freelance community.*
