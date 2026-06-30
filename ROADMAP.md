# BirthdayKeeper — Build Roadmap

An ordered milestone plan. Each milestone is a coherent, committable slice that keeps the app runnable. This is the sequence to feed into Claude Code, one milestone at a time.

---

## M0 — Project skeleton
- Gradle project, Kotlin DSL, version catalog (`libs.versions.toml`), `build-logic/` convention plugins.
- Empty modules created with correct dependency wiring (see ARCHITECTURE.md §3).
- Hilt set up in `:app`; a single empty Compose screen that launches.
- GitHub Actions workflow (lint + unit test) green on an empty project.
- *Deliverable:* app builds, runs, CI passes. First commit.

## M1 — Domain + data foundations
- `:core:model` models; `:core:domain` repository interfaces + `GetUpcomingBirthdaysUseCase` and date math (with tests, including Feb 29).
- `:core:database` Room entities/DAOs (with the sync-ready fields), `:core:data` repository impls.
- Unit tests for date math and repository (in-memory Room).
- *Deliverable:* fully tested domain/data layer, no UI yet.

## M2 — Person CRUD + upcoming list (F1, F2, F5)
- `:core:designsystem` theme; `:feature:people` list; `:feature:editperson` add/edit.
- Wire ViewModels to use cases; Turbine tests for ViewModels.
- *Deliverable:* you can add people and see them sorted by days-until. The app is now useful.

## M3 — Reminders (F3, F4)
- `:core:notifications` WorkManager daily worker + notification channel.
- `:feature:settings` + DataStore for global reminder lead; per-person override.
- `POST_NOTIFICATIONS` permission flow.
- *Deliverable:* the app actually reminds you. Core promise delivered.

## M4 — Depth (F6, F7, F8)
- Contacts import (F6, `READ_CONTACTS`).
- Groups + filtering (F7).
- Debounced search (F8).
- *Deliverable:* feature-complete on the "depth" tier.

## M5 — Backup + theming (F9, F10)
- JSON export/import via Storage Access Framework + kotlinx.serialization.
- Theme setting, dynamic color, dark mode polish.
- *Deliverable:* data portability + a polished look for screenshots.

## M6 — Stretch (F11)
- Glance home-screen widget showing upcoming birthdays.
- *Deliverable:* the differentiator + a great README screenshot.

## M7 — Polish & present
- README (screenshots/GIF, architecture diagram, decisions & trade-offs).
- Fill testing gaps; ensure CI is comprehensive.
- Tidy commit history; add LICENSE.
- *Deliverable:* repo is portfolio-ready.

---

### Suggested commit rhythm
Within each milestone, commit in small themed steps (e.g. "feat(people): add upcoming-birthdays use case", "test(people): cover leap-year case"). A clean, conventional history is itself a signal to reviewers.