# Agent Instructions — KMP WinGet Package Manager

## General Guidelines
- Do not provide verbose summaries of changes or outputs at the end of every turn; keep responses concise, direct, and actionable unless explicitly requested.
- Adhere strictly to senior-level Kotlin, Compose Multiplatform, and Clean Architecture standards.
- Analyze the blast radius and system impact before modifying code; thoroughly validate and review implementations prior to finalizing.

---

## Git & GitHub Workflow Practices
- **Logical Staging & Commits**: Break down changes into granular, logical commit chunks scoped to individual functional and architectural layers (e.g., config, domain, data, presentation, UI).
- **Verified GPG Commits**: Always create signed commits (`git commit -S`) using the configured user GPG key so that commits show with the "Verified" badge on GitHub.
- **Commit Message Format**: Follow standard imperative style (e.g., `Add JNA dependencies and update build configuration`). Keep subject lines concise (≤50 characters), capitalized, without trailing punctuation, and omit bodies unless non-obvious context is required.
- **Pre-Commit Verification**: Always run full build checks (`./gradlew build`) and diagnostic scans before staging and committing.

---

## Project Overview & Tech Stack
- **Project Type**: Kotlin Multiplatform Desktop Application targeting Windows 10 & 11.
- **UI Framework**: Compose Multiplatform 1.12.2 (Material 3 / Compose Desktop).
- **Language & Build**: Kotlin 2.4.21, Gradle 9.3.1 (wrapper).
- **Architecture**: Clean Architecture + MVI (Model-View-Intent) pattern with `StateFlow` and Coroutines.
- **Dependency Injection**: Koin 4.2.2 (`koin-compose-viewmodel`).
- **Native Windows Interop**: JNA 5.19.1 (`net.java.dev.jna:jna`, `jna-platform`) for Win32 API (`dwmapi.dll`, `Advapi32Util`, `IPHlpAPI`).

---

## Architecture & Module Structure
```
composeApp/src/desktopMain/kotlin/com/velocity/kmpwinget/
├── core/
│   └── BuildConfig.kt                 # App version and build constants
├── data/
│   ├── datasource/
│   │   ├── WinGetExecutor.kt          # Direct WinGet CLI process runner (UTF-8, IO-streaming)
│   │   ├── WinGetParser.kt            # Positional column and fallback tokenizer for CLI output
│   │   ├── WindowsRegistryScanner.kt  # JNA Win32 Registry scanner for instant (<30ms) app discovery
│   │   ├── WindowsDriverStoreScanner.kt # pnputil /enum-drivers parser for installed drivers
│   │   ├── WindowsUpdateDriverScanner.kt # Windows Update Agent driver search
│   │   └── WindowsHardwareMonitor.kt  # Live CPU/RAM/network telemetry via Win32 + JNA
│   └── repository/
│       ├── PackageRepositoryImpl.kt   # IPackageRepository implementation with caching & local update matching
│       ├── DriverRepositoryImpl.kt    # IDriverRepository implementation (Driver Store + WUA)
│       └── SystemRepositoryImpl.kt    # ISystemRepository for disk space, cleanmgr, and diagnostics
├── domain/
│   ├── model/
│   │   ├── Package.kt                 # Core entity (supports local ARP & matched WinGet IDs)
│   │   ├── DriverPackage.kt           # Driver Store entity & DriverClass taxonomy
│   │   ├── PackageDetails.kt          # Rich details drawer payload
│   │   ├── UpdateTask.kt              # Background queue task and queue state
│   │   ├── OperationResult.kt         # Sealed hierarchy: Idle, Loading, Success, Error
│   │   ├── PackageFilter.kt           # NavigationTab, PackageSortOption, SourceFilterOption
│   │   └── SystemStats.kt             # Storage DriveInfo, telemetry & WinGet environment stats
│   ├── repository/
│   │   ├── IPackageRepository.kt
│   │   ├── IDriverRepository.kt
│   │   └── ISystemRepository.kt
│   ├── service/
│   │   ├── VersionComparator.kt       # Multi-part semantic version comparison engine
│   │   └── PackageDeduplicator.kt     # WinGet/ARP duplicate merging
│   └── usecase/
│       ├── GetPackagesUseCase.kt
│       ├── GetPackageDetailsUseCase.kt
│       ├── UpgradePackageUseCase.kt
│       ├── UninstallPackageUseCase.kt
│       ├── BatchOperationUseCase.kt
│       ├── GetDriversUseCase.kt
│       ├── UpdateDriverUseCase.kt
│       └── SystemToolsUseCase.kt
├── di/
│   └── Koin.kt                        # Dependency injection bindings
├── platform/
│   ├── WindowsNativeBridge.kt         # JNA DWM titlebar dark/light theme integration
│   ├── WindowsIconLoader.kt           # Extracts executable icons for package rows
│   └── WindowsUpdateLauncher.kt       # Opens Windows Update / triggers driver scans
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
│       ├── DriversIsland.kt           # Device driver list, filters & actions
│       ├── DriverTableRow.kt          # Driver class badges and row controls
│       ├── BackgroundQueueIsland.kt   # Queued update progress panel
│       ├── SelectionBarIsland.kt      # Floating / embedded batch action bar
│       ├── SystemToolsIsland.kt       # Drive storage, telemetry & maintenance tools
│       ├── OperationDialog.kt         # Modal progress dialog with live terminal log drawer
│       ├── ConfirmUninstallDialog.kt  # Safe confirmation dialog for package removal
│       └── EmptyStateView.kt          # Clean empty state representations
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

### 3. Driver Updates
- **Installed Drivers**: Enumerate the Windows Driver Store with `pnputil.exe /enum-drivers`; parse output with the system code page, not UTF-8.
- **Update Source of Truth**: Driver updates must come from the Windows Update Agent (`Microsoft.Update.Session`, `Type='Driver'`). Never infer driver updates from a WinGet search: WinGet is an application package manager and has no driver catalog.
- **Installation Surface**: Open Windows Update (`ms-settings:windowsupdate` / `UsoClient StartScan`) for installation. Never silently install driver packages; Windows must perform the OEM compatibility check.
- **Version Checks**: Use `VersionComparator.isNewer` before marking `DriverPackage.hasUpdate`.

### 4. Window & Native Theming Rules
- **Window Configuration**: Use standard decorated `Window(resizable = true, state = state)` in `main.kt`.
- **Avoid Frame Errors**: Never call `window.background = Color(0, 0, 0, 0)` on decorated AWT `Frame` / `ComposeWindow`, as it throws `IllegalComponentStateException`.
- **Title Bar Synchronization**: Use `WindowsNativeBridge.applyTheme(window, isDarkMode)` from `platform/` to synchronize `DWMWA_USE_IMMERSIVE_DARK_MODE`, `DWMWA_CAPTION_COLOR`, and `DWMWA_TEXT_COLOR`.

### 5. UI/UX Design System
- **Islands Theme**: Follow the unified Islands layout where UI sections are grouped into distinct elevated containers (`islandContainer`), rather than wrapping each table row in an individual card.
- **Tab Underglow**: Navigation tabs must maintain the luminous underglow beam (`Modifier.tabUnderglow`) when active.
- **Defensive Dialogs**: Always wrap dialog composables in explicit boolean/state checks in `MainScreen.kt` to prevent accidental uninitialized renders.
