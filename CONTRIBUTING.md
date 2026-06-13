# Contributing to Creator OS Lite

Thank you for your interest in contributing! This is an open-source project for the freelance community.

## Development Setup

1. **Prerequisites**: Android Studio Ladybug+, JDK 17+, Android SDK 36
2. **Clone**: `git clone https://github.com/YOUR_USERNAME/creator-os-lite.git`
3. **Open**: Open in Android Studio and let Gradle sync
4. **Run**: Select a device/emulator and press Run

## Code Style

- **Kotlin**: Follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- **Compose**: Use `stringResource(R.string.xxx)` for all user-facing strings
- **Architecture**: MVVM — screens in composables, logic in ViewModels, data in Repositories

## Pull Request Process

1. Fork the repo and create your branch from `main`
2. If you're adding a feature, make sure it respects the app's offline-first design
3. Test your changes on a physical device or emulator (API 24+)
4. Ensure all strings are externalized to `strings.xml` and translated in `values-zh/`
5. Update the README if needed
6. Submit a PR with a clear description of the changes

## Adding Translations

To add a new language:
1. Create `app/src/main/res/values-{lang}/strings.xml`
2. Translate all strings from `values/strings.xml`
3. Submit a PR

## Reporting Issues

Use the GitHub issue tracker. Include:
- Device model and Android version
- Steps to reproduce
- Expected vs actual behavior
- Screenshots if applicable

## License

By contributing, you agree that your contributions will be licensed under the MIT License.
