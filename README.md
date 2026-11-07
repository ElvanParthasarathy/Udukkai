# Udukkai (உடுக்கை)

Billing and ledger apps for two kinds of small business in Tamil Nadu, with bilingual (Tamil / English) bills, GST,
receipts, reports and a Firebase-backed sync. **The only maintained app is the Kotlin Multiplatform project in
[`kmp/`](kmp/).** Everything else in this repository is an earlier generation kept for reference.

| App | Package | For |
| :--- | :--- | :--- |
| **Udukkai Textiles** (உடுக்கைச் சவுளி) | `com.elvan.udukkai.textiles` | Silk / textile sellers: GST tax invoices, proforma, receipts, GST sales report |
| **Udukkai Factory** (உடுக்கை ஆலை) | `com.elvan.udukkai.factory` | Coolie / twisting factories: weight-based bills without GST, receipts, sales report |

Both are built from one codebase as two Android flavors (`textiles`, `factory`). A desktop target exists in the code
but is not built or shipped at the moment.

## Repository map

| Directory | Status | What it is |
| :--- | :--- | :--- |
| [`kmp/`](kmp/) | **Maintained** | Kotlin Multiplatform + Compose Multiplatform app (Android, Desktop). All new work happens here. |
| [`UdukkaiTemplates/`](UdukkaiTemplates/) | **Maintained** | The printed bill papers (GST invoice, coolie invoice, receipt) as a small React project. Its built bundle is copied into `kmp/composeApp/src/androidMain/assets/print/`. |
| [`flutter/`](flutter/) | Legacy | First generation (Flutter). Not maintained. |
| [`react/`](react/) | Legacy | Second generation (React + Vite + Capacitor + Electron web app). Not maintained; the root `vercel.json` still deploys it as a website. |
| [`archive/`](archive/) | Legacy | Old documents, sample data and scratch tools. |
| `scripts/` | Maintained | APK publishing helpers. |
| `graphify-out/` | Tooling | Generated code knowledge graph (`graphify update .` refreshes it). |

## Working on the app (`kmp/`)

Requirements: JDK 21 (Android Studio's bundled JDK works), Android SDK.

```
cd kmp
./gradlew :composeApp:installTextilesDebug      # Textiles on a connected phone
./gradlew :composeApp:installFactoryDebug       # Factory on a connected phone
./gradlew :composeApp:testTextilesDebugUnitTest # unit tests (GST, numbering, financial year, Excel, app lock...)
```

Layout of `kmp/composeApp/src`:

- `commonMain/` shared code: `core/` (auth, sync, backup, finance rules, printing, Excel export), `data/` (models,
  database interfaces), `textiles/` and `factory/` (each app's screens and repositories), `ui/` (shared components),
  `localization/` (English and Tamil strings, `K.kt` holds the keys).
- `androidMain/` Android specifics (SQLite helper, Firebase sync, activities, file save dialog, voice typing).
- `desktopMain/` desktop specifics. `commonTest/` unit tests.
- `textiles/` and `factory/` (flavor folders) hold each app's `google-services.json`.

### Debug builds and the app lock

The app locks on a fresh start and whenever the screen turns off. Debug builds can be unlocked from the computer so
testing does not need a fingerprint (release builds ignore this):

```
adb shell am start -n com.elvan.udukkai.textiles/com.elvan.udukkai.MainActivity --ez dev_skip_lock true
```

## Data and security

- Business data lives in a local SQLite database on the phone and syncs to Firebase Realtime Database
  (`kmp/firebase/database.rules.json`). Only the listed Google accounts may read or write; deploy rule changes with
  `firebase deploy --only database` from `kmp/`.
- Firebase App Check (Play Integrity) is built into the app. It protects the data only once enforcement is switched
  on in the Firebase console.
- Keystores, `keystore.properties`, `local.properties` and database files are git-ignored. Never commit them.

## Bill papers

The printed papers come from `UdukkaiTemplates/`. After changing a template:

```
cd UdukkaiTemplates
npm install
npm run bundle     # writes dist-bundle/
```

then copy `dist-bundle/` (index.html, assets, version.txt) over `kmp/composeApp/src/androidMain/assets/print/` and
bump `src/bridge/version.ts` so saved PDFs are made again.

## Conventions

- Tamil text uses pure Tamil words (see [`.agents/AGENTS.md`](.agents/AGENTS.md)); the logo is called "ஓவுரு".
- Numbering: invoices `SHORT-001`, proformas `PF/SHORT-001`, receipts `RCP/SHORT/01`; each series restarts on 1 April.
  A finished financial year closes for edits on 1 December.
- Run `graphify update .` after changing code to keep the knowledge graph current.
