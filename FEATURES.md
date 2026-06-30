# BirthdayKeeper — Feature Specification

> An offline-first Android app to track birthdays and get timely reminders.
> Built as a portfolio piece to demonstrate modern Android architecture, testing, and engineering practice.
> A Go backend (AWS) for cross-device sync is planned for a later phase — the data layer is designed to accommodate it without a rewrite.

---

## 1. Vision

Never forget a birthday. The app stores people and their birthdays locally, computes who's coming up, and reminds the user ahead of time — all fully offline. Sync is a future concern, but the architecture treats local storage as the single source of truth so that adding a remote source later is additive, not a rewrite.

## 2. Out of scope (this phase)

- Any network / backend / authentication
- Cloud sync (designed-for, not built)
- Social features, gift suggestions, calendar provider integration

## 3. Personas (brief)

- **Forgetful Friend** — wants a dead-simple list and a nudge the day before.
- **Organized Planner** — groups people (family/friends/work), wants a week's notice, uses the home-screen widget.

---

## 4. Feature tiers

Each feature notes the *user value* and the *technical capability it showcases* — the latter is the real point of the project.

### Tier 1 — MVP (offline core)

**F1. Person CRUD**
- Add / edit / delete a person: name, date of birth (year optional), free-text note.
- *Showcase:* Room database, repository pattern, form state handling, validation.

**F2. Upcoming birthdays list**
- Home screen lists people sorted by "days until next birthday," with age (or next age) shown when the year is known.
- *Showcase:* reactive data flow end-to-end (Room `Flow` → use case computes/sorts → ViewModel `StateFlow` → Compose). Derived state done correctly. Leap-year (Feb 29) edge handling.

**F3. Reminders & notifications**
- A daily background job checks for upcoming birthdays and posts a notification.
- *Showcase:* WorkManager periodic work, notification channels, `POST_NOTIFICATIONS` runtime permission (Android 13+). WorkManager persists across reboot, so no boot receiver is needed here — a deliberate, documented decision.

**F4. Configurable reminder lead time**
- Global setting: remind on the day / 1 day before / 3 days / 1 week before. Optional per-person override.
- *Showcase:* DataStore (Preferences), settings flowing reactively into the scheduling logic.

**F5. Age & next-birthday calculation**
- Correct age and countdown logic using `java.time`, with the year-unknown case handled gracefully.
- *Showcase:* clean date/time domain logic, unit-testable pure functions.

### Tier 2 — Depth

**F6. Import from device contacts**
- Pick contacts and pull in any birthdays stored on the device.
- *Showcase:* `ContentResolver`/`ContentProvider` access, `READ_CONTACTS` runtime permission, graceful permission-denied UX.

**F7. Groups / categories**
- Assign people to groups (Family, Friends, Work, custom) with a color. Filter the list by group.
- *Showcase:* one-to-many Room relations, filtering over a `Flow`.

**F8. Search**
- Debounced search across names/notes.
- *Showcase:* `Flow` operators (`debounce`, `flatMapLatest`), reactive query composition.

**F9. Backup / restore (JSON)**
- Export all data to a JSON file and re-import it. Useful offline, and a natural bridge to future sync.
- *Showcase:* `kotlinx.serialization`, Storage Access Framework file I/O, mapping domain ↔ serializable models.

**F10. Theming & dark mode**
- Material 3, dynamic color (Android 12+), light/dark/system theme setting.
- *Showcase:* design system module, Material 3 theming, polished screenshots for the README.

### Tier 3 — Stretch (differentiators)

**F11. Home-screen widget (Glance)**
- A widget showing the next few upcoming birthdays, tappable to open the app.
- *Showcase:* Jetpack Glance — modern, uncommon in portfolios, great screenshot.

**F12. Multi-module structure**
- Feature and core modules with enforced dependency boundaries (see ARCHITECTURE.md).
- *Showcase:* codebase scalability, build-time module isolation, parallelized builds.

---

## 5. Non-functional requirements

- **Offline-first:** every feature works with no network. Local DB is the single source of truth.
- **Min SDK 26 / Target latest stable.** (Min 26 keeps `java.time` available without desugaring noise; revisit if wider reach is wanted.)
- **Accessibility:** content descriptions, sufficient touch targets, dynamic type respected.
- **Testability:** business logic (use cases, repositories, date math) covered by unit tests; ViewModels tested with fakes + Turbine; at least a smoke-level Compose UI test per feature.
- **Quality gates:** lint + detekt + tests must pass in CI before merge.

---

## 6. Designed-for (future) — sync readiness

The schema and repository contracts are shaped now so the Go/AWS backend slots in later:

- Entities carry `remoteId` (nullable), `updatedAt` (epoch millis), and a `syncStatus` / soft-delete tombstone field.
- Repositories expose suspend/Flow contracts that a future `RemoteDataSource` can satisfy without changing the domain or presentation layers.
- A future `SyncWorker` (WorkManager) will reconcile local and remote using `updatedAt` timestamps.

These fields are inert in this phase but prevent painful migrations later — see ARCHITECTURE.md §6.