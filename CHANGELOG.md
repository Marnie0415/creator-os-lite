# Changelog

## [1.2.0] - 2026-06-13

### Added
- **Encrypted API Key Storage** — API keys encrypted with Android Keystore AES-256-GCM, no external dependencies
- **Configurable Risk Thresholds** — Customize ghosted client (6-168h) and expiring project (6-168h) thresholds from Settings
- **CSV Data Import** — Restore data from previously exported CSV files via file picker and system share
- **Local Notifications** — Risk alerts (overdue/ghosted/expiring) posted as Android notifications, debounced to once per 24h
- **More Unit Tests** — 24 new tests: CryptoManager (6), DataImporter CSV parsing (7), RiskEngine configurable thresholds (4), existing tests expanded
- **Privacy Audit** — Full codebase scan: no hardcoded credentials, no personal data leaks, no insecure storage

### Changed
- `SettingsManager.kt` — API keys now encrypted via `CryptoManager` (Android Keystore) before writing to SharedPreferences
- `DashboardViewModel.kt` — Now accepts `SettingsManager` for configurable thresholds; extends `AndroidViewModel` for notifications
- `RiskEngine.kt` — `generateRisks()` now accepts `ghostHours` and `expiringHours` parameters (defaults: 48h, 72h)
- `SettingsScreen.kt` — Added Risk Thresholds sliders and Data Import card
- `CreatorOSApplication.kt` — Creates notification channel on startup
- `AndroidManifest.xml` — Added `POST_NOTIFICATIONS` permission (API 33+)

### New Files
| File | Purpose |
|:-----|:---------|
| `CryptoManager.kt` | AES-256-GCM encryption via Android Keystore |
| `DataImporter.kt` | CSV file parser with UTF-8 BOM and quoted-field support |
| `NotificationHelper.kt` | Android notification channel + debounced risk alerts |
| `DataImporterTest.kt` | 7 CSV parsing tests |
| `CryptoManagerTest.kt` | 6 encryption roundtrip tests |

### Technical Notes
- Encryption is transparent — existing API keys are re-encrypted on next save; legacy plaintext keys are still readable
- Notifications fire when the app opens; no background service required
- Risk thresholds are persisted in SharedPreferences; DashboardViewModel reads them reactively

## [1.1.0] - 2026-06-13

### Added
- **Client Search** — Filter clients by name with a search bar on the Clients page
- **Project Search** — Filter projects by title or client name with a search bar on the Projects page
- **Edit Client** — Edit client name and contact channel from the detail view
- **Edit Project** — Edit project title, description, deadline, and invoice amount
- **Snackbar Notifications** — Visual feedback for client/project create, update, and delete operations
- **Data Export** — Export all data as CSV files, shareable via any app
- **Persistent AI Key Warning** — Red banner on AI Tools when no API key configured
- **Light/Dark Theme Toggle** — Real-time switching from Settings
- **WeChat Contact Channel** — Added WeChat option for Chinese users
- **Dashboard Statistics Fix** — OUTSTANDING INVOICES now correctly excludes DepositPaid
- **Database Migration Safety** — Explicit MIGRATION_2_3 and MIGRATION_3_4; fallbackToDestructiveMigration retained as last resort
- **29 Unit Tests** — RiskEngine (14), TimeFormatter (10), CurrencyUtils (5)
- **CHANGELOG.md** — Version tracking

### Changed
- Settings page: added Data Export card, theme toggle, innerPadding fix for system bars
- MainActivity passes repositories to SettingsScreen
- UI strings: 22 new strings (EN + ZH)

### New Files
| File | Purpose |
|:-----|:---------|
| `DataExporter.kt` | CSV export with UTF-8 BOM |
| `file_paths.xml` | FileProvider paths config |
| `RiskEngineTest.kt` | 14 risk engine tests |
| `TimeFormatterTest.kt` | 10 time utility tests |
| `CurrencyUtilsTest.kt` | 5 currency format tests |
| `CHANGELOG.md` | This file |

## [1.0.0] - 2026-06-13

### Initial Release
- Core features: Client CRUD, Project + Invoice tracking, Risk Radar Dashboard
- AI Tools: Payment follow-up writer + Quote assistant
- Multi-provider AI: Gemini / OpenAI-compatible / Anthropic Claude (9+ providers)
- Bilingual UI: English + Chinese (auto-switch)
- Privacy-first: 100% local storage, no accounts, no cloud, no telemetry
- Technology: Kotlin, Jetpack Compose, Material 3, Room, Retrofit, Moshi
