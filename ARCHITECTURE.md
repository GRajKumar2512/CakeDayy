# BirthdayKeeper — Architecture

> Multi-module **Clean Architecture** with **MVVM** as the presentation pattern.
> Offline-first, single source of truth, designed for a future Go/AWS sync backend.

---

## 1. Architectural style

Three logical layers, with a strict dependency rule pointing inward:

```
        presentation  ──▶  domain  ◀──  data
        (Compose +UI)     (pure Kotlin)  (Room, DataStore, future remote)
```

- **Domain** is pure Kotlin (no Android, no framework). It owns models, use cases, and repository *interfaces*. It depends on nothing.
- **Data** implements the repository interfaces using Room / DataStore (and, later, a remote source). It depends on domain.
- **Presentation** (ViewModels + Compose) depends on domain only. It never touches Room or DataStore directly.

The **dependency rule**: source dependencies only point inward toward domain. Domain knows nothing about the layers that use it. This is what we enforce with module boundaries.

## 2. Why multi-module

Beyond the architecture flex, modules give us:
- **Enforced boundaries** — a `:feature:*` module physically *cannot* import another feature's internals or reach into Room. The compiler enforces the architecture, not a code-review convention.
- **Build performance** — independent modules compile in parallel and benefit from incremental builds.
- **Clear ownership** — each module has one job.

Trade-off (documented honestly in the README): more Gradle boilerplate and more upfront ceremony. Justified here because demonstrating scalable structure is an explicit goal.

## 3. Module graph

```
:app
 ├─ depends on every :feature, plus :core:designsystem, :core:notifications, :core:domain
 │   (hosts navigation, Application class, Hilt root, widget registration;
 │    depends on :core:domain directly to observe SettingsRepository for live theme switching)
 │
:feature:people        ─┐
:feature:editperson     ├─▶ :core:domain ─▶ :core:model
:feature:groups         │   :core:designsystem, :core:ui, :core:common
:feature:settings      ─┘
:widget                ──▶ :core:domain, :core:designsystem
 │
:core:data ─▶ :core:domain, :core:database, :core:datastore, :core:common, :core:model
:core:database ─▶ :core:model, :core:common
:core:datastore ─▶ :core:model, :core:common
:core:notifications ─▶ :core:domain, :core:common
:core:domain ─▶ :core:model, :core:common
:core:designsystem ─▶ (Compose/Material3 only)
:core:ui ─▶ :core:designsystem, :core:model
:core:common ─▶ (kotlin/coroutines only)
:core:testing ─▶ :core:domain, :core:data   (test-only fakes & rules)
```

Key rule: **features never depend on each other**, and **features never depend on `:core:data`/`:core:database` directly** — only on `:core:domain`. The `:app` module is the only place wiring happens (it provides repository implementations from `:core:data` via Hilt).

### Module responsibilities

| Module | Responsibility |
|---|---|
| `:app` | DI root, navigation host, `Application`, widget + worker registration. The composition root. |
| `:core:model` | Pure Kotlin domain models (`Person`, `Group`, `ReminderLead`, `ThemeMode`). No framework deps. |
| `:core:domain` | Repository *interfaces* (including `BackupRepository`) + use cases (`GetUpcomingBirthdaysUseCase`, `ExportDataUseCase`/`ImportDataUseCase`, date math). Pure Kotlin. |
| `:core:data` | Repository *implementations*, mappers, single-source-of-truth logic. |
| `:core:database` | Room — entities, DAOs, `RoomDatabase`, type converters, migrations. |
| `:core:datastore` | Preferences DataStore for settings (theme, reminder lead). |
| `:core:notifications` | WorkManager workers, notification channels/builders, scheduling. |
| `:core:designsystem` | Material 3 theme, color, type, reusable composables (buttons, cards). |
| `:core:ui` | Shared composables that know about domain models (e.g. a `PersonRow`). |
| `:core:common` | Dispatcher providers, `Result` wrappers, date utilities, constants. |
| `:core:testing` | Test fakes (`FakePersonRepository`), JUnit rules, test data builders. |
| `:feature:*` | One screen/flow each: Compose UI + ViewModel + UI state. |
| `:widget` | Glance widget + its data binding. |

## 4. Package convention (within a feature)

```
com.example.birthdaykeeper.feature.people
 ├─ PeopleScreen.kt        // @Composable, stateless, takes state + callbacks
 ├─ PeopleViewModel.kt     // exposes StateFlow<PeopleUiState>, handles events
 ├─ PeopleUiState.kt       // sealed/data classes for the screen state
 └─ navigation/            // type-safe nav destination + entry point
```

ViewModels expose a single immutable `UiState` via `StateFlow`; screens are stateless and driven by state-in / events-out. This keeps Compose previewable and the ViewModel unit-testable.

## 5. Presentation pattern (MVVM details)

- One `UiState` per screen, surfaced as `StateFlow`, collected with `collectAsStateWithLifecycle()`.
- User actions are plain function callbacks (or a sealed `Event` type for richer flows).
- One-off effects (snackbars, navigation) via a `Channel`/`SharedFlow`, never stored in `UiState`.
- Use cases are injected into ViewModels; ViewModels never see Room/DataStore types.

## 6. Data layer & offline-first (sync-ready) design

**Single source of truth:** the UI always observes Room. Writes go to Room; reads are `Flow`s from Room. Nothing in the UI ever waits on a network.

**Schema carries future-sync fields now** (inert this phase, no migration churn later):

```kotlin
@Entity(tableName = "person")
data class PersonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val remoteId: String? = null,          // set after first sync
    val name: String,
    val birthMonth: Int,
    val birthDay: Int,
    val birthYear: Int? = null,            // null when year unknown
    val note: String? = null,
    val groupId: Long? = null,
    val reminderLeadDaysOverride: Int? = null,
    val createdAt: Long,
    val updatedAt: Long,                   // epoch millis — drives future sync
    val isDeleted: Boolean = false,        // soft-delete tombstone for sync
)
```

**Repository contract (in `:core:domain`)** is source-agnostic:

```kotlin
interface PersonRepository {
    fun observeUpcoming(withinDays: Int): Flow<List<Person>>
    fun observeAll(): Flow<List<Person>>
    suspend fun upsert(person: Person)
    suspend fun delete(id: Long)
}
```

When the Go backend arrives, we add a `RemoteDataSource` and a `SyncWorker` that reconciles by `updatedAt`; the domain and presentation layers don't change. That additive path is the senior-level signal this project is meant to send.

## 7. Key technical decisions

- **Scheduling:** a daily `PeriodicWorkRequest` (unique work) queries upcoming birthdays and posts notifications. Chosen over exact `AlarmManager` because a once-daily check doesn't need exact timing, and WorkManager survives reboots and respects Doze automatically — fewer permissions, more robustness. The trade-off (no minute-precise control) is acceptable for a birthday reminder.
- **Notifications:** dedicated channel; request `POST_NOTIFICATIONS` at a natural moment (when the user first enables reminders), with a rationale, not at cold start.
- **Permissions:** contacts import requests `READ_CONTACTS` lazily at the point of use, with a graceful denied state.
- **Navigation:** type-safe Navigation Compose; each feature owns its destination and exposes an entry function.
- **Widget refresh:** the Glance widget is refreshed from `:app` only, keeping `:feature:*`/`:core:data` free of any `:widget` dependency. Two triggers, merged: Room's `Flow` from `GetUpcomingBirthdaysUseCase` already re-emits on any write to the person table (add/edit/delete/contacts-import/backup-import all covered for free, since Room's invalidation tracker is table-based, not tied to a specific write's call site), and the existing M3 daily `PeriodicWorkRequest`'s `RUNNING → not-RUNNING` transition (a periodic work's `WorkInfo` never reaches `SUCCEEDED`, so this falling edge is the correct "a run just completed" signal) catches the case where the day rolls over with no data change. No second worker is added.

## 8. Tech stack

Managed via a Gradle **version catalog** (`gradle/libs.versions.toml`) — itself a best-practice signal. Pull the latest stable versions when scaffolding rather than pinning here (versions go stale fast).

- Language: **Kotlin**, Coroutines + Flow
- UI: **Jetpack Compose**, **Material 3**
- DI: **Hilt**
- Persistence: **Room** (KSP), **DataStore (Preferences)**
- Background: **WorkManager**
- Widget: **Jetpack Glance**
- Serialization: **kotlinx.serialization**
- Build: Gradle Kotlin DSL + convention plugins (`build-logic/`) to share module config

## 9. Testing strategy

- **Domain:** plain JUnit unit tests for use cases and date math (no Android needed — fast).
- **Data:** repository tests against an in-memory Room DB; DAO tests for queries.
- **Presentation:** ViewModel tests using `:core:testing` fakes + **Turbine** to assert `StateFlow` emissions; a `MainDispatcherRule` for coroutines.
- **UI:** at least one Compose UI test per feature (smoke level), Hilt test runner for wiring.
- Aim for meaningful coverage of business logic over a vanity percentage.

## 10. CI/CD & quality gates

GitHub Actions on every PR:
1. `./gradlew lint detekt`
2. `./gradlew testDebugUnitTest`
3. (optional) assemble debug APK as an artifact

Add status badges to the README. A green CI badge on a portfolio repo does real work for you.

## 11. Repository hygiene (the "decorate GitHub" layer)

- `README.md` with: one-liner, screenshots/GIF, architecture diagram, tech-stack list, and a short **"decisions & trade-offs"** section (reviewers read this first).
- This `ARCHITECTURE.md` and `FEATURES.md` committed at the root.
- Conventional, readable commit history (small, themed commits beat one giant dump).
- A `LICENSE`.