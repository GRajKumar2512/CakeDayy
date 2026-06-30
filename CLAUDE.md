# CLAUDE.md — CakeDayy

Repo-level instructions for Claude Code. Read this before generating or modifying any code.
Also read `ARCHITECTURE.md`, `FEATURES.md`, and `ROADMAP.md` at the repo root — they are the source of truth for structure and scope.

---

## Project

CakeDayy is an **offline-first** Android birthday reminder app, built as a portfolio piece to demonstrate clean, modern, scalable Android architecture. A Go/AWS sync backend is planned later; the data layer is designed to accommodate it without a rewrite.

- Base package: `com.pocketaps.cakeday`
- App name (label/strings): `CakeDayy`
- minSdk 26, target latest stable.

## Tech stack (non-negotiable defaults)

Kotlin · Coroutines + Flow · Jetpack Compose · Material 3 · Hilt · Room (KSP) · DataStore (Preferences) · WorkManager · Jetpack Glance · kotlinx.serialization · Gradle Kotlin DSL with a version catalog (`gradle/libs.versions.toml`) and `build-logic/` convention plugins.

Pull the **latest stable** versions when adding dependencies; do not pin from memory.

## Architecture (multi-module Clean Architecture + MVVM)

Three layers, strict inward dependency rule:

```
presentation (Compose + ViewModel)  ──▶  domain (pure Kotlin)  ◀──  data (Room, DataStore, future remote)
```

**Hard rules — enforce these, do not violate even if it seems convenient:**
- `domain` is pure Kotlin: no Android framework imports, no Room/DataStore/Compose. It owns models, use cases, and repository **interfaces** only.
- `data` implements repository interfaces. It depends on `domain`. Nothing else depends on `data` directly.
- `presentation` (`:feature:*`) depends on `:core:domain` and design-system/ui modules **only**. A feature must never import Room, DataStore, or `:core:data`.
- **Features never depend on other features.** Cross-feature needs go through `:core:domain`.
- `:app` is the only composition root: it wires `:core:data` implementations to interfaces via Hilt, and hosts navigation. Wiring happens nowhere else.

Module layout and per-module responsibilities are defined in `ARCHITECTURE.md §3`. Follow it exactly. Package everything under `com.pocketaps.cakeday.<layer>.<module>`.

## Presentation conventions (MVVM)

- One immutable `UiState` per screen, exposed as `StateFlow`, collected with `collectAsStateWithLifecycle()`.
- Composables are **stateless**: state in, events out (callbacks or a sealed `Event` type). No business logic in composables.
- One-off effects (snackbar, navigation) via `Channel`/`SharedFlow`, never stored in `UiState`.
- ViewModels depend on **use cases**, never on Room/DataStore types.
- Every screen-level composable has a `@Preview` with sample state.

## Data & offline-first rules

- Room is the **single source of truth**. UI observes Room via `Flow`. Nothing in the UI waits on a network.
- Entities carry `remoteId: String?`, `updatedAt: Long`, and `isDeleted: Boolean` (sync tombstone) now — inert this phase, present to avoid future migrations. See `ARCHITECTURE.md §6`.
- Map between Room entities, domain models, and serializable DTOs explicitly; never leak a Room entity past the data layer.

## Commands (use these to verify your own work)

Run these after changes and fix failures before considering a task done:
- Build: `./gradlew assembleDebug`
- All unit tests: `./gradlew testDebugUnitTest`
- Single module tests: `./gradlew :feature:people:testDebugUnitTest`
- Lint: `./gradlew lint`
- Detekt: `./gradlew detekt`

## Testing rules

- Domain use cases and date math: plain JUnit, fully covered (include the Feb 29 / leap-year case).
- Repositories: tested against in-memory Room.
- ViewModels: tested with fakes from `:core:testing` + Turbine, using a `MainDispatcherRule`.
- Every feature ships at least a smoke-level Compose UI test.
- Prefer meaningful coverage of logic over a coverage percentage.

## Workflow expectations

- **Plan before editing** non-trivial work. Propose the approach and wait for approval before changing files.
- Work **one ROADMAP milestone at a time**; do not jump ahead or scaffold future features early.
- Make **small, themed commits** with conventional messages (e.g. `feat(people): add upcoming-birthdays use case`).
- After edits, run the relevant tests/lint and report results.

## Do NOT

- Do not put business logic in composables or in `:app`.
- Do not let a feature reach into Room, DataStore, or another feature.
- Do not add LiveData (use Flow), or AsyncTask, or `runBlocking` in production code.
- Do not introduce new abstractions or libraries without a clear, stated reason — restraint is preferred over cleverness for this project.
- Do not pin dependency versions from memory; resolve latest stable.
- Do not implement the sync/backend logic in this phase — only keep the schema/contracts ready for it.