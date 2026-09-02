# Slyvos Launcher — Lawnchair Engineering Study & Architecture Modernization Roadmap

> **Author**: Antigravity Engineering Team  
> **Target Repository**: `ryo-builds/slyvos-launcher`  
> **Reference Repository**: `https://github.com/LawnchairLauncher/lawnchair.git` (`origin/16-dev`)  
> **Date**: September 2, 2026  
> **Build Identity**: Slyvos Pre-Alpha Build #009  

---

## Executive Summary

This engineering study evaluates **Lawnchair Launcher** (a mature, open-source Android launcher based on AOSP `Launcher3` and Quickstep) as a technical reference to guide the long-term architecture, stability, performance, and hardware robustness of **Slyvos Launcher**.

---

## 1. Subsystem Engineering Audits & Comparisons

### 1.1 Launcher Architecture
Separating UI rendering from background data loading (`MODEL_EXECUTOR`) prevents UI thread jank when processing 100+ installed applications or heavy widget updates.

### 1.2 Home Screen & Workspace
`Workspace.java` manages a paged `CellLayout` grid where items are assigned exact grid coordinates `(cellX, cellY, spanX, spanY, screenId)`.

### 1.3 App Drawer
`AllAppsContainerView.java` uses fast A-Z indexed section headers with `FastBitmapDrawable` caching.

### 1.4 Native Widget Experience & Lifecycle Safety
`LauncherWidgetHolder` and `WidgetAddFlowHandler.java` handle `ACTION_APPWIDGET_BIND` permissions dialog flow by storing `PendingRequestArgs` in `Activity.onSaveInstanceState(Bundle outState)`.

---

## 2. Decision Matrix: ADOPT / ADAPT / STUDY ONLY / REJECT

| Engineering Pattern | Lawnchair Source | Decision | Rationale for Slyvos |
| :--- | :--- | :--- | :--- |
| **Drawable / Icon LruCache** | `IconCache.java` | **ADOPT** | High visual performance gain for app drawer scrolling with zero architectural risk. |
| **Pending Widget State Persistence** | `WidgetAddFlowHandler.java` | **ADOPT** | Prevents transient widget loss if process death occurs during widget configuration. |
| **Responsive Grid Scaling** | `DeviceProfile.java` | **ADAPT** | Adapt concept using Compose `BoxWithConstraints` for tablet/landscape support instead of complex Java `DeviceProfile`. |
| **Package Broadcast Receiver** | `PackageUpdatedTask.java` | **ADAPT** | Listen to `BroadcastReceiver(ACTION_PACKAGE_ADDED/REMOVED)` to update `StateFlow` automatically when apps are installed/uninstalled. |
| **StateFlow UI Architecture** | N/A (Slyvos Native) | **PRESERVE** | Slyvos' ViewModel + StateFlow is superior and cleaner than Lawnchair's legacy AOSP `StateManager`. |
| **AOSP Quickstep System Taskbar**| `quickstep/` | **STUDY ONLY** | Useful system knowledge, but unnecessary complexity for Slyvos Pre-Alpha. |

---

## 3. Phase 7.1 Implementation Log — Hardening

1. **`LruCache<String, ImageBitmap>` Icon Caching**: Bounded to 128 items in `PackageManagerAppRepository`.
2. **`LauncherApps.Callback` Real-Time Package Sync**: Streams package additions, removals, and updates into `StateFlow<List<AppInfo>>` and prunes uninstalled dock entries automatically.
3. **Pending Widget Allocation Persistence**: Persists `pending_widget_id` and `pending_widget_provider` in `PreferencesWidgetRepository` (`slyvos_widgets_prefs.xml`) to survive process death during widget creation.

---

## 4. Phase 7.2 Implementation Log — Responsive Workspace Engine

### 4.1 Responsive Architecture Adopted
1. **Single Authoritative Calculation (`WorkspaceGeometry.kt`)**:
   - *Adapted from*: Lawnchair `DeviceProfile.java`.
   - *Slyvos Implementation*: Computes `columns` (4 portrait, 6 landscape), `rows` (5 portrait, 3 landscape), `cellWidthDp`, `cellHeightDp`, `iconSizeDp`, `gridSpacingDp`, `sidePaddingDp`, `topPaddingDp`, and `bottomPaddingDp` in a single Compose `remember(maxWidth, maxHeight, orientation, density, iconSize)` block inside `HomeScreen.kt`.
2. **`BoxWithConstraints` Root Integration**:
   - Replaces hardcoded pixel dimensions with declarative Compose density-independent units (`Dp`), ensuring exact portrait rendering on Samsung Galaxy A07 while expanding seamlessly to landscape mode and larger screens.
3. **Dynamic Bar & Floating Dock Safe Areas**:
   - Prevents overlapping between widget canvas, desktop app shortcuts, Dynamic Bar, and floating dock across density settings (`COMPACT`, `BALANCED`, `SPACIOUS`).

---

## Conclusion & Next Steps

Phase 7.1 and Phase 7.2 establish a highly hardened, robust, and responsive launcher foundation. Official published release identity remains **Slyvos Pre-Alpha Build #009**.
