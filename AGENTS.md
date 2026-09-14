# Agent Instructions — KMP WinGet Package Manager

## General Guidelines
- Do not provide verbose summaries of changes or outputs at the end of every turn; keep responses concise, direct, and actionable unless explicitly requested.
- Adhere strictly to senior-level Kotlin, Compose Multiplatform, and Clean Architecture standards.
- Analyze the blast radius and system impact before modifying code; thoroughly validate and review implementations prior to finalizing.

---

## Project Overview & Tech Stack
- **Project Type**: Kotlin Multiplatform Desktop Application targeting Windows 10 & 11.
- **UI Framework**: Jetpack Compose Multiplatform (Compose Desktop 1.7.3 / Material 3).
- **Architecture**: Clean Architecture + MVI (Model-View-Intent) pattern with `StateFlow` and Coroutines.
- **Dependency Injection**: Koin 4.0.3 (`koin-compose-viewmodel`).
- **Native Windows Interop**: JNA 5.16.0 (`net.java.dev.jna:jna`, `jna-platform`) for Win32 API (`dwmapi.dll`, `Advapi32Util`).

---

## Architecture & Module Structure
```
composeApp/src/desktopMain/kotlin/com/velocity/kmpwinget/
├── data/
│   ├── datasource/
│   │   ├── WinGetExecutor.kt          # Direct WinGet CLI process runner (UTF-8, IO-streaming)
│   │   ├── WinGetParser.kt            # Positional column and fallback tokenizer for CLI output
│   │   ├── WindowsNativeBridge.kt     # JNA DWM titlebar dark/light theme integration
│   │   └── WindowsRegistryScanner.kt  # JNA Win32 Registry scanner for instant (<30ms) app discovery
│   └── repository/
│       ├── PackageRepositoryImpl.kt   # IPackageRepository implementation with caching & local update matching
│       └── SystemRepositoryImpl.kt    # ISystemRepository for disk space, cleanmgr, and diagnostics
├── domain/
│   ├── model/
│   │   ├── Package.kt                 # Core entity (supports local ARP & matched WinGet IDs)
│   │   ├── OperationResult.kt         # Sealed hierarchy: Idle, Loading, Success, Error
│   │   ├── PackageFilter.kt           # NavigationTab, PackageSortOption, SourceFilterOption
│   │   ├── SystemStats.kt             # Storage DriveInfo & WinGet environment stats
│   │   └── VersionComparator.kt       # Multi-part semantic version comparison engine
│   ├── repository/
│   │   ├── IPackageRepository.kt
│   │   └── ISystemRepository.kt
│   └── usecase/
│       ├── GetPackagesUseCase.kt
│       ├── UpgradePackageUseCase.kt
│       ├── UninstallPackageUseCase.kt
│       ├── BatchOperationUseCase.kt
│       └── SystemToolsUseCase.kt
├── di/
│   └── Koin.kt                        # Dependency injection bindings
├── theme/
│   ├── Color.kt                       # Material 3 & Islands color definitions
│   ├── ColorScheme.kt                 # Light and Dark Material 3 color schemes
│   ├── IslandsTheme.kt                # Modifier.islandContainer, tabUnderglow, subtleIslandControl
│   ├── Theme.kt                       # AppTheme and ThemeState.isDarkMode toggle
│   └── Typography.kt                  # AppTypography (Lato fonts)
├── ui/
│   ├── MainScreen.kt                  # Main Islands layout
│   └── components/
│       ├── AppHeaderIsland.kt         # Top branding, disk cleanup trigger, dark/light switch
│       ├── NavControlsIsland.kt       # Glowing underglow tabs + search, filters, sorting
│       ├── PackageTableRow.kt         # Sleek table row for package list (not individual cards)
│       ├── SelectionBarIsland.kt      # Floating / embedded batch action bar
│       ├── SystemToolsIsland.kt       # Drive storage cards & maintenance tools
│       ├── OperationDialog.kt         # Modal progress dialog with live terminal log drawer
│       ├── ConfirmUninstallDialog.kt  # Safe confirmation dialog for package removal
│       └── EmptyStateView.kt          # Clean empty state representations
├── utils/
│   └── util.kt                        # BuildConfig & constants
└── main.kt                            # Application entry point with native titlebar sync
```

---

## Core Invariants & Engineering Best Practices

### 1. Process Execution & WinGet
- **Direct CLI Execution**: Always invoke `winget.exe` directly via `ProcessBuilder` (never spawn PowerShell as an intermediary).
- **Stream I/O Concurrently**: Read stdout and stderr on separate background threads with UTF-8 encoding to prevent process deadlocks.
- **Safety Flags**: WinGet upgrade and uninstall commands must include `--disable-interactivity --accept-package-agreements --accept-source-agreements --silent`.

### 2. Version Verification & Non-WinGet Packages
- **Source of Truth**: Always use `VersionComparator.isNewer(currentVersion, availableVersion)` to verify that an available version is strictly newer before marking `hasUpdate = true`.
- **Local Application Resolution**: When an app is detected from the Windows Registry (ARP entry) without WinGet metadata:
  - Query WinGet repository with a sanitized search name.
  - Check version validity with `VersionComparator`.
  - Attach `matchedWingetId` and `availableVersion`.
  - When upgrading, execute `winget upgrade --id <matchedId>` with automatic fallback to `winget install --id <matchedId>` for in-place vendor installer upgrades.

### 3. Window & Native Theming Rules
- **Window Configuration**: Use standard decorated `Window(resizable = true, state = state)` in `main.kt`.
- **Avoid Frame Errors**: Never call `window.background = Color(0, 0, 0, 0)` on decorated AWT `Frame` / `ComposeWindow`, as it throws `IllegalComponentStateException`.
- **Title Bar Synchronization**: Use `WindowsNativeBridge.applyTheme(window, isDarkMode)` to synchronize `DWMWA_USE_IMMERSIVE_DARK_MODE`, `DWMWA_CAPTION_COLOR`, and `DWMWA_TEXT_COLOR`.

### 4. UI/UX Design System
- **Islands Theme**: Follow the unified Islands layout where UI sections are grouped into distinct elevated containers (`islandContainer`), rather than wrapping each table row in an individual card.
- **Tab Underglow**: Navigation tabs must maintain the luminous underglow beam (`Modifier.tabUnderglow`) when active.
- **Defensive Dialogs**: Always wrap dialog composables in explicit boolean/state checks in `MainScreen.kt` to prevent accidental uninitialized renders.
