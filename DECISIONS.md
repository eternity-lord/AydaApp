# Decisions and implementation notes

## Architecture

- Kotlin, one activity, Jetpack Compose + Material 3, manual dependency wiring, Room, StateFlow, WorkManager, AlarmManager and Coil. Manual DI keeps this small offline-only app easy to maintain without adding a container.
- Room is the source of truth for clients, services, appointments, appointment-service price snapshots, design records, gallery metadata, transactions and expense categories. The current schema is version 2; migration `1 → 2` adds a client update timestamp. Destructive migrations are intentionally disabled, and KSP exports Room schema JSON under `app/schemas/`.
- Money is stored as `Long` Toman. Appointment completion and income transaction updates are performed in one Room transaction. Photo data is copied into app-private storage; database paths are relative so backups can be restored on another installation.

## Calendar, language and appearance

- The date converter follows the Jalali break-year algorithm and supports years within the published break table in `JalaliDate.kt`. Data remains epoch milliseconds; the UI converts at the edge. Calendar rows start on Saturday and Fridays use the coral weekend accent.
- Persian is the default interface and layout direction. Strings are in Android resources; number formatting can be switched to Latin digits. Vazirmatn regular/medium/bold and Lalezar are bundled with their OFL license texts.
- `ui/theme/Color.kt` is the central palette. `ChalkFrame`, `ChalkCard`, `ChalkButton`, `ChalkField`, chips and dividers provide the chalkboard/wood style.

## Notifications and privacy

- Appointment alarms use exact-and-idle scheduling when Android grants it, then fall back to an inexact idle alarm. Notification permission is requested only after onboarding or from Settings. A daily summary is scheduled with WorkManager; its execution time is best-effort under Android battery policies.
- There is no internet permission, server, login, ad SDK or analytics. Calls, SMS and shares are handed to Android's system intents.
- Backup archives contain the Room database, gallery images and the digit preference. Restore validates the ZIP paths and size, closes Room, replaces the local data, validates the database, then restarts the activity.

## Sensible defaults

- Seeded prices are editable examples, not an official price list. The default working window and appointment suggestion window are 09:00–19:00, and the default reminder is one hour before; the owner should confirm these before daily use.
- Release signing material is deliberately not committed. The downloadable GitHub artifact is a debug-signed installable APK; configure a private keystore outside the repository before distributing a production release.
- GitHub releases are published only by the manual `Release APK` workflow on `main`. That isolated workflow gets `contents: write`, validates a semantic-version tag, reruns tests/build, and uploads a version-named APK; the regular CI workflow remains read-only.

## Follow-up before a production launch

- Review the UI on representative Android devices and add screenshots to the README.
- Add UI automation for the complete add-client → appointment → payment → finance flow, plus an instrumented restore-on-clean-install test. Unit tests currently cover Jalali conversions, leap/Esfand/year boundaries, overlap/free-slot logic and price math.
- Consider configurable working hours, birthday notifications, a client PIN/biometric lock, appointment editing, and direct photo attachment to design records. The current build favors a reliable offline core over collecting additional permissions or private data.
