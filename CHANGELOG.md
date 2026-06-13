# Changelog

## [1.1.0] - 2026-06-13

### Added
- **Dashboard Statistics Fix** — "OUTSTANDING INVOICES" now correctly excludes DepositPaid status
- **Settings UI Fix** — Content no longer clipped by system bars (proper innerPadding usage)
- **WeChat Contact Channel** — Added WeChat option for Chinese user base
- **Database Safety** — Explicit MIGRATION_2_3 and MIGRATION_3_4 to prevent data loss on schema upgrades; fallbackToDestructiveMigration retained as last resort
- **Persistent AI Key Warning** — Red banner on AI Tools page when no API key is configured, with tap-to-navigate to Settings
- **Light Theme Support** — Full light/dark mode toggle in Settings; real-time switching without app restart
- **Client Search** — Filter clients by name with a search bar on the Clients page
- **Project Search** — Filter projects by title or client name with a search bar on the Projects page
- **Edit Client** — Edit client name and contact channel from the detail view
- **Edit Project** — Edit project title, description, deadline, and invoice amount
- **Snackbar Notifications** — Visual feedback for client/project create, update, and delete operations
- **Data Export** — Export all clients, projects, invoices, and timeline entries as CSV files, shareable via any app
- **RiskEngine Unit Tests** — 14 comprehensive tests covering ghosted clients, overdue invoices, critical escalations, expiring projects, boundary conditions, and severity ordering
- **TimeFormatter Unit Tests** — 10 tests for relative time, deadline countdown, and overdue formatting
- **CurrencyUtils Unit Tests** — 5 tests for all formatting cases (integer, decimal, large, negative, small)
- **CHANGELOG.md** — Version tracking

### Changed
- Settings page now includes a Data Export card (requires repository access)
- MainActivity passes repositories to SettingsScreen for export functionality

### Technical
- Added `DataExporter.kt` — CSV export utility with UTF-8 BOM for Chinese character support
- Added `file_paths.xml` — FileProvider configuration for sharing exported files
- Added FileProvider declaration to AndroidManifest.xml
