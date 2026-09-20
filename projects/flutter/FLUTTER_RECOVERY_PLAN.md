# Flutter Recovery and React-Parity Plan

## Purpose

Finish the Flutter app without losing its native Flutter UI, navigation,
animations, or the strict separation between **Niril Pattu (Silk/GST)** and
**Niril Kooli (Coolie)**.

The original React app is the source of truth for **business behaviour, screen
actions, and document layouts**. Flutter is the source of truth for the new
native UI language, motion, and responsive Android/desktop flow.

This is a recovery plan, not a rewrite plan. Do not replace Flutter with React
or Kotlin. Do not recreate the React invoice design in Dart PDF widgets.

## Non-Negotiable Rules

1. Silk and Coolie are two separate applications selected from one launcher.
   They may share infrastructure and visual primitives, but must never share
   business records or query each other's database.
2. Every normal read and write uses exactly one database:
   - Silk: `elvan_niril_silk.db`
   - Coolie: `elvan_niril_coolie.db`
3. A Silk row and Coolie row may have the same numeric `id`. An `id` alone is
   never a globally unique reference.
4. Mode-specific screens must not import each other. Shared widgets move into
   `niril_podhu/`; they are never borrowed from the other mode.
5. The React renderer remains the one document renderer for invoice/receipt
   preview and PDF output. Flutter owns all normal application screens.
6. A mode switch never silently transfers, merges, or exposes records.
7. Never use destructive git commands or delete user/generated files without
   explicit approval. The workspace already contains important local changes.

## Repository Facts to Preserve

- `lib/src/adippadai/tharavuthalam/pattu_tharavuthalam.dart` defines the Silk
  Drift database.
- `lib/src/adippadai/tharavuthalam/kooli_tharavuthalam.dart` defines the
  Coolie Drift database.
- Separate providers open them from:
  - `lib/src/cheyalpaadugal/amaippugal/tharavu/pattu_niruvana_tharavugal_provider.dart`
  - `lib/src/cheyalpaadugal/amaippugal/tharavu/kooli_niruvana_tharavugal_provider.dart`
- Concrete Silk/Coolie repositories already exist under
  `lib/src/cheyalpaadugal/niril_podhu/kalanjiyam/`.
- The legacy combined `seyali_tharavuthalam.dart` is migration-only. It must
  never become the normal runtime database.

## Current Faults to Address First

1. `lib/src/adippadai/nilaimai/seyali_nilaimai.dart` merges profile streams and
   mutates `NiruvanaTharavugal.adaimozhi` to label the mode. Replace this hack
   with typed setup status; do not mutate a profile to carry session state.
2. `niril_kooli/.../kooli_urupadi_kooru.dart` imports a Silk widget. Move only
   the genuinely shared presentation widget to `niril_podhu/`.
3. `NirilDestination` declares Settings, Reports, and GST Returns, but the
   desktop custom-content branch currently wires only Settings. Do not leave a
   visible route that renders no body.
4. The stored Flutter analysis report has five compiler errors around missing
   editor input widgets. Trace and repair the intended code through history;
   do not create fake duplicate APIs to hide them.
5. Flutter has copied React renderer source and generated assets. Establish one
   React document source of truth and an automatic build/copy process.

## Target Architecture

```text
Launcher: onboarding / restore / explicit mode choice
  ├─ Silk session
  │    ├─ PattuDatabase + Pattu repositories + Pattu feature widgets
  │    └─ shared Flutter primitives
  └─ Coolie session
       ├─ KooliDatabase + Kooli repositories + Kooli feature widgets
       └─ shared Flutter primitives

Document request
  Flutter mode data → versioned DTO → React document bundle in WebView
  → Kotlin Android PDF/print service → URI → Flutter preview/share/download
```

### Layer ownership

| Area | May contain | Must not contain |
| --- | --- | --- |
| `adippadai/` | lifecycle, database connection, mode session, localization, theme, navigation primitives | Silk/Coolie feature calculations |
| `niril_podhu/` | reusable UI, repository interfaces, selection controls, document DTOs | hidden concrete mode queries |
| `niril_pattu/` | GST screens, GST tax logic, Silk data mapping | Coolie imports |
| `niril_kooli/` | Coolie screens/rules/data mapping | Silk imports |
| `chattagam/` | shell, responsive navigation, explicit whole-widget mode choice | business queries/calculations |
| `react_engine/` | document-only React entries and original document components | Flutter app screens/database code |
| Android Kotlin | WebView, PDF file creation, print/share/open | duplicate Flutter screens/business rules |

### Shell selection rule

Selecting whole widgets with `switch (mode)` in the shell is correct. Do not
put hundreds of `if (mode == ...)` branches inside one giant customer or
invoice screen merely to reduce files. When an editor opens, it captures the
active mode and must not change underneath a save operation.

### Data isolation contract

```text
Silk editor → PattuPattiyalKalanjiyam → PattuDatabase → Silk tables
Coolie editor → KooliPattiyalKalanjiyam → KooliDatabase → Coolie tables
```

Allowed cross-mode operations are only legacy migration, a backup containing
explicitly named Silk/Coolie sections, and launcher setup checks. Forbidden:

- combined business lists;
- queries or navigation using an integer `id` without mode context;
- a mode switch while an editor can save to its old repository;
- shared provider cache from the previous mode;
- mode labels injected into unrelated domain fields.

## Screen Map

Before implementing any Flutter screen, read the corresponding React source
and create its parity checklist (described below).

| Product area | React source | Flutter target | Required result |
| --- | --- | --- | --- |
| Mode choice | `moolam/pagudhigal/ModeSelector.tsx` | `ulnuzhaivu/kaatchi/muraimai_thaervu_thirai.dart` | Starts one isolated session; never transfers data. |
| Silk dashboard | `GstBill/Mugappu.tsx` | `niril_pattu/.../pattu_mugappu_thirai.dart` | Cards, totals, empty state, quick actions. |
| Coolie dashboard | `CoolieBill/CoolieDashboard.tsx` | `niril_kooli/.../kooli_mugappu_thirai.dart` | Coolie-only cards/totals/actions. |
| Silk invoices | `GstBill/InvoiceList.tsx`, `InvoiceEditorV2.tsx`, `InvoiceView.tsx` | `pattu_pattiyalgal_thirai.dart`, `thiruthi/pattiyal/`, `paarvai/` | List/filter/create/edit/duplicate/view/GST totals/document actions. |
| Coolie invoices | `CoolieInvoiceList.tsx`, `CoolieInvoiceEditor.tsx`, `CoolieInvoiceView.tsx` | `kooli_pattiyalgal_thirai.dart`, `thiruthi/pattiyal/`, `paarvai/` | Coolie fields/calculations, no hidden Silk fields. |
| Silk receipts | `GstBill/Receipts/Patru.tsx`, `ReceiptEditor.tsx`, `ReceiptView.tsx` | `pattu_patrucheettugal_thirai.dart`, `thiruthi/patrucheettu/` | Links/balances/edit/view/document actions. |
| Coolie receipts | `CoolieReceiptList.tsx`, `CoolieReceiptEditor.tsx`, `CoolieReceiptView.tsx` | `kooli_patrucheettugal_thirai.dart`, `thiruthi/patrucheettu/` | Links only to Coolie invoices/customers. |
| Silk merchants | `GstBill/Merchants/Vanigargal.tsx`, `VanigarThoguppu.tsx`, `VanigarThirai.tsx` | `pattu_vaangunargal_thirai.dart`, `thiruthi/vaangunar/` | CRUD/search/bulk actions/Silk fields. |
| Coolie merchants | `CoolieMerchants.tsx`, `CoolieClientEditor.tsx` | `kooli_vaangunargal_thirai.dart`, `thiruthi/vaangunar/` | CRUD/search/bulk actions/Coolie fields. |
| Silk items | `GstBill/Items/Porul.tsx`, `PorulThoguppu.tsx` | `pattu_porutkal_thirai.dart`, `thiruthi/porul/` | Tax/unit/item actions. |
| Coolie items | `CoolieItems.tsx`, `CoolieItemEditor.tsx` | `kooli_porutkal_thirai.dart`, `thiruthi/porul/` | Coolie item/weight actions. |
| Silk settings | `GstBill/GstSettings/` | `amaippugal/` plus `pattu_*` settings | Business, address, bank, branding, invoice, language. |
| Coolie settings | `CoolieBill/CoolieSettings/` | `amaippugal/` plus `kooli_*` settings | Business, address, contact/bank, branding, appearance. |
| Reports | `GstBill/Reports/Arikkaigal.tsx`, `VariArikkaigal.tsx` | body for `NirilDestination.reports` | Silk-only until a real Coolie report exists. |
| GST returns | React route/components reached from `moolam/Seyali.tsx` | body for `NirilDestination.gstReturns` | Silk-only, never a blank page. |
| Documents | React invoice/receipt view components | Flutter preview + `react_engine/` + Android bridge | Same React render for preview and PDF. |

## Parity Checklist Protocol

Create one checklist per target screen under `flutter/docs/parity/`, for
example `silk_invoice_editor_parity.md`. It must contain:

1. React source file and entry route.
2. Flutter target file(s).
3. Fixture data for normal, empty, loading, error, long Tamil, long English,
   and bilingual cases.
4. Every visible action: control, validation, confirmation, database effect,
   navigation result, and snackbar/toast.
5. Flutter equivalent status: `not-started`, `built`, or `verified`.
6. Phone and desktop screenshot/recording paths for verified states.
7. One assertion that proves the opposite mode's database did not change.

A screen is not complete because it looks similar. It is complete only when
its actions and data isolation are verified.

## Phase 0 — Preserve and Baseline

1. Record `git status --short`; preserve all local changes.
2. Add `flutter/docs/parity/README.md` explaining status labels and evidence.
3. Use the stored analysis report first; when ready, run fresh analysis and
   separate current errors from pre-existing warnings.
4. Repair the missing input-widget errors through source/history investigation.
5. After schema changes only, regenerate Drift code and inspect the diff.

**Exit:** zero analyzer errors, existing tests run, no user data/assets deleted.

## Phase 1 — Make Mode Isolation Safe

1. Replace the mutable profile-stream hack with typed setup status, for example:

   ```dart
   class ModeSetupStatus {
     const ModeSetupStatus({required this.mode, required this.hasProfile});
     final AppMode mode;
     final bool hasProfile;
   }
   ```

   Create one status provider per database; the launcher combines only these
   status values. Never attach a mode through `adaimozhi`.
2. Keep `appModeProvider` as session state. Decide separately whether mode
   selection persists across restart; preserve current chooser-on-launch until
   product direction explicitly changes.
3. Implement one mode-switch coordinator at the shell boundary. In order:
   - block/confirm when there are unsaved edits;
   - close editor routes and inline overlays;
   - clear outgoing selection/search/draft state;
   - reset navigation to home;
   - set the new mode and rebuild its body.
4. Ensure opened editors use their captured mode, never an arbitrary later
   global mode value.
5. Keep the legacy combined database in `MigrationUdhavi` only and test that
   normal repositories do not open it.
6. Move the cross-imported shared line-item UI into `niril_podhu/` while
   retaining mode-specific validation and total calculation.

**Exit:** writes are invisible in the other mode; switching cannot save an old
editor into the new database; no direct Pattu↔Kooli imports remain.

## Phase 2 — Stabilize Shell and Navigation

1. Keep `NirilDestination` as top-level navigation state.
2. Select mode-specific bodies only in shell/facade files; do not leak mode
   checks into generic cards and text fields.
3. Keep mobile's combined Uruvakku invoice/receipt tab state separate per mode.
4. Implement `reports`/`gstReturns` bodies or hide their entry until done.
5. Verify back, search, add, selection overlays, and desktop nested navigation
   on every primary destination.

**Exit:** every visible route renders; all shell commands affect active mode.

## Phase 3 — Repeatable React-to-Flutter Workflow

For every screen:

1. Open React with deterministic fixtures.
2. Complete its checklist before editing Flutter.
3. Implement missing behaviour in Flutter while preserving Flutter visual style
   and animations. React specifies behaviour; it does not require MUI cloning.
4. Compare normal/empty/error/long Tamil/long English/dark/narrow/wide states.
5. Exercise each write and run the opposite-mode isolation assertion.
6. Mark only tested rows `verified`.

Never mass-copy React screens to Dart. Deliver one vertical slice at a time.

## Phase 4 — Finish the Silk Vertical Slice

Complete in dependency order:

1. Silk business profile/settings/language.
2. Silk merchants.
3. Silk products.
4. Silk invoice list → editor/validation/totals → duplicate/view/delete.
5. Silk receipts → invoice allocation/balance → view/delete.
6. Silk dashboard sourced from finished repositories.
7. Silk reports and GST returns.

Use concrete `Pattu*` repository/database types inside Silk features. Shared
interfaces may express common operations but must not erase GST requirements.

**Exit:** offline flow works: profile → merchant → item → invoice → receipt →
React document PDF → backup/restore.

## Phase 5 — Finish the Coolie Vertical Slice

Repeat the same order using the existing Coolie feature folders, but do not
copy Silk screens and merely hide fields:

1. Coolie profile/settings.
2. Coolie merchants.
3. Coolie items.
4. Coolie invoices with Coolie quantities/weights/pricing/totals.
5. Coolie receipts linked only to Coolie invoices.
6. Coolie dashboard.
7. Only real Coolie reports; never show Silk GST reports as a placeholder.

**Exit:** every Coolie workflow is verified and cannot change Silk records.

## Phase 6 — React Document Renderer and Native PDF

1. Keep React invoice/receipt components as the only document layout source.
   Flutter does not create a visually similar replacement template.
2. `react_engine/` contains document entries/adapters only, reusing one
   authoritative React component and stylesheet source.
3. Define an explicit versioned DTO:

   ```text
   mode, documentType, documentId, profile, customer, lineItems, totals,
   settings, documentRevision
   ```

   Never send a raw database object with an implicit active mode.
4. Flutter builds the DTO from the concrete current-mode repository and sends
   it through one documented method channel.
5. Kotlin loads the local React bundle in WebView. React calls `documentReady`
   only after render and `document.fonts.ready`; do not print at the first page
   finished event.
6. Kotlin creates/prints a PDF from that same WebView, returns a URI, and
   Flutter handles preview/share/download.
7. Replace manual renderer copying with one explicit build task:
   React build → replace generated Android renderer assets → verify every HTML
   reference exists. Do not install npm dependencies on every Flutter build.
8. Defer Windows PDF work until Android is stable; reuse the same React
   document bundle and do not introduce a Tamil-incompatible Dart PDF fallback.

**Exit:** preview and PDF come from the same React render; Tamil is verified on
a physical Android device; wrong-mode document DTOs are rejected by tests.

## Phase 7 — Migration, Backup, Restore, and Release

1. Keep migration idempotent and versioned so it cannot duplicate records.
2. Make backup metadata explicitly identify both payloads, for example:

   ```json
   {"formatVersion":1,"silk":{"records":[]},"coolie":{"records":[]}}
   ```

3. Restore validates metadata before replacing files, restores databases
   independently, reopens affected providers, and invalidates related state.
4. Deleting data must name the affected mode; "clear all" may affect both only
   when the confirmation says so.
5. Validate a debug APK on Android and a Windows build before release.

## Required Tests

### Data isolation

- Silk writes never appear in Coolie queries and vice versa.
- Equal numeric IDs in separate databases remain distinct.
- Receipt-to-invoice links reject other-mode invoices.
- Mode switch clears old search, selection, and editor state.
- Migration assigns every legacy row to exactly one target database.
- Backup/restore preserves both databases without contamination.

### Navigation and screens

- Every destination selects the correct mode-specific body.
- Add opens the active-mode editor.
- Mobile Uruvakku segments remain independent per mode.
- GST routes remain Silk-only.
- Back from settings/reports returns to the active-mode primary tab.

### Documents

- DTO serialization includes `mode` and `documentType`.
- DTO builders read only their own mode repository.
- Generated asset manifest points to current React build output.
- Manually verify Tamil, English, bilingual, logo, long, and multipage Android
  documents.

## Validation Order

Run after the relevant phase is implemented:

```powershell
cd flutter
flutter analyze
flutter test
flutter build apk --debug
```

For document work, run the documented React renderer build, install the APK on
a physical Android device, and attach evidence to the parity checklist. An
emulator result alone is not sufficient for PDF/Tamil completion.

## Rules for the Next Agent

1. Read root and Flutter `AGENTS.md` instructions first.
2. Work one phase and one checklist at a time, never a broad rewrite.
3. Inspect every caller before altering a shared provider or repository.
4. Use existing Pattu/Kooli databases; do not create a third generic runtime
   database.
5. Do not use mode booleans on models to compensate for a wrong query. Fix the
   repository selection boundary.
6. Do not manually copy React source into Flutter. Only generated document
   renderer assets may be copied by the build task.
7. Follow the repository's Pure Tamil and Tanglish naming policy for new Dart
   names and localization keys.
8. Respect the file size rules; extract focused widgets/helpers as needed.
9. Never run `git checkout` or `git reset` without explicit user approval.
10. Stop and ask before moving data across mode boundaries.

## Definition of Done

The Flutter app is complete only when it builds with zero analyzer errors and
passing tests; Silk and Coolie work as isolated apps from one launcher; every
mapped React workflow is verified in Flutter; Flutter retains its native UI and
animations; documents use the original React layout with correct Tamil; and
backup/restore/migration preserve separate mode data on Android and Windows.
