# KMMTemplate

A production-ready Kotlin Multiplatform Mobile template for Android and iOS with a full MVI architecture stack.

## Tech Stack

| Layer           | Library               | Version |
|-----------------|-----------------------|---------|
| Language        | Kotlin                | 2.3.10  |
| UI              | Compose Multiplatform | 1.10.1  |
| MVI             | MVIKotlin             | 4.3.0   |
| DI              | Koin                  | 4.1.1   |
| Networking      | Ktor                  | 3.3.3   |
| Resources       | moko-resources        | 0.26.0  |
| Navigation      | Compose Navigation    | 2.9.2   |
| Android min SDK | —                     | 26      |

## Project Structure

```
├── android/app             # Android application module
├── iosApp/                 # iOS application (Xcode project)
├── shared/
│   ├── main/               # Shared entry point (Koin setup, App composable)
│   ├── common/             # Coroutines, extensions, base mappers
│   ├── common-ui/          # Compose theme, components, utilities
│   ├── common-resources/   # moko-resources wrappers
│   ├── core-mvikotlin/     # BaseExecutor, store factory helpers
│   ├── core-navigation/    # Screen interface, navigation utils
│   ├── core-network/       # Ktor client, NetworkEnvironment, DI
│   ├── core-presentation/  # BaseViewModel
│   ├── core-storage/       # DataStore setup
│   └── feature-demo-*/     # Demo features (api/impl/presentation/ui)
├── build-logic/            # Convention plugins
├── module-templates/       # KMP Module Generator templates
└── linters/                # SwiftLint and SwiftFormat configs
```

## Feature Architecture

Each feature is split into 4 modules following a strict dependency direction:

```
ui  →  presentation  →  impl  →  api
```

| Module         | Contains                                  |
|----------------|-------------------------------------------|
| `api`          | Store interface (Intent / State / Label)  |
| `impl`         | StoreFactory, Executor, Reducer, DI       |
| `presentation` | ViewModel, UI models, mappers, DI         |
| `ui`           | Composable screens, navigation API, Route |

## Getting Started

### 1. Use this template

Click **Use this template** on GitHub to create your repository.

### 2. Run the setup script

```bash
chmod +x setup.sh
./setup.sh --package com.example.myapp --app-name MyApp
```

This replaces all package names, namespaces, and the app name throughout the project.

### 3. Configure iOS

Open `iosApp/Configuration/Config.xcconfig` and set your `TEAM_ID`.

### 4. Open and sync

Open the project in Android Studio or IntelliJ IDEA, sync Gradle, then open `iosApp/` in Xcode.

## Creating a New Feature

Install the [KMP Module Generator](https://plugins.jetbrains.com/plugin/28838-kmp-module-generator) plugin, then:

1. Right-click the target folder → **New → Generate Module → From Template...**
2. Select **MVIKotlin Feature Module**
3. Fill in feature name and package
4. After generation, register `${featureName}Route` in your NavGraph

See `module-templates/GUIDE.md` for full template documentation.

## CI / CD

GitHub Actions workflows are pre-configured:

| Workflow        | Trigger | Job                     |
|-----------------|---------|-------------------------|
| Android Checks  | PR      | Detekt + Android Lint   |
| iOS Checks      | PR      | SwiftLint + SwiftFormat |
| Android Release | Manual  | APK or AAB (dev / prod) |

See [`.github/WORKFLOW_SETUP.md`](.github/WORKFLOW_SETUP.md) for signing setup.

## Build Commands

```bash
./gradlew :android:app:assembleDebug     # Android debug APK
./gradlew :android:app:assembleRelease   # Android release APK
./gradlew detekt                          # Static analysis
```

iOS: open `iosApp/` in Xcode.
