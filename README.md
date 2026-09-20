# Udukkai (உடுக்கை) / Elvan Niril (எல்வன் நிறிள்)

A unified multi-platform GST invoicing & ledger suite for Coolie (labor) and Silk (goods) businesses.

## 🏗️ Repository Architecture (`projects/`)

This repository is organized as a clean monorepo with dedicated project workspaces:

| Project Directory | Stack | Description | Build & Run |
| :--- | :--- | :--- | :--- |
| [`projects/kmp/`](projects/kmp/) | **Kotlin Multiplatform (Compose Multiplatform)** | **Primary implementation.** Modern native Android & Desktop app with Material 3 & SESL design system. | `cd projects/kmp`<br>`.\gradlew.bat assembleDebug` |
| [`projects/flutter/`](projects/flutter/) | **Flutter (Dart)** | Cross-platform mobile application following Senthamizh domain architecture. | `cd projects/flutter`<br>`flutter run` |
| [`projects/react/`](projects/react/) | **React + Vite + Capacitor + Electron** | Offline-first web application, Capacitor Android wrapper, and Electron desktop launcher. | `cd projects/react`<br>`npm install`<br>`npm run dev` |

---

## Architecture Map (English -> Tanglish)

This project has transitioned away from standard English Flutter conventions into a domain-specific Senthamizh (Pure Tamil) terminology, transliterated gracefully into English letters.

### 🏛️ The Three Pillars (`lib/src/`)
1. **`adippadai/` (Core)**: The fundamental building blocks (Database, Networking, Localization, Routing).
2. **`cheyalpaadugal/` (Features)**: Encapsulated business domains (Auth, Settings, Billing, Reports).
3. **`koorugal/` (Widgets/Components)**: Shared, reusable UI elements.

### 📂 Folder Name Mappings (Inside Features)
When creating new features, use these exact folder names:
- **Presentation / View Layer** ➔ `kaatchi/`
- **Pages / Screens** ➔ `thiraigal/`
- **Widgets / Components** ➔ `koorugal/`
- **Models / Entities** ➔ `tharavuru/`
- **State / Providers** ➔ `nilaimai/`
- **Repositories** ➔ `kalanjiyam/`
- **Data Source** ➔ `tharavu_moolam/`

### 📱 Platform & Context Mappings
- **Desktop** ➔ `kanini/`
- **Mobile** ➔ `kaipaesi/`
- **Reports** ➔ `arikkaigal/`
- **Onboarding** ➔ `varavaerpu_padigal/`

---

## File Naming Conventions

We do not use standard English suffixes (`_page.dart`, `_widget.dart`). Instead, use the appropriate Tanglish suffix to describe the file's purpose:

| English Suffix | Tanglish Suffix | Example |
| :--- | :--- | :--- |
| `*_page.dart` / `*_screen.dart` | `*_thirai.dart` | `mugappu_thirai.dart` (Home Page) |
| `*_widget.dart` / `*_component.dart` | `*_kooru.dart` | `elvan_pothan.dart` (Elvan Button) |
| `*_model.dart` / `*_entity.dart` | `*_tharavuru.dart` | `payanar_tharavuru.dart` (User Model) |
| `*_dialog.dart` / `*_modal.dart` | `*_meladukku.dart` | `urudhi_meladukku.dart` (Confirm Dialog) |
| `*_editor.dart` | `*_thiruthi.dart` | `pattiyal_thiruthi.dart` (Invoice Editor) |

### 🛠️ Working with Agents
This repository contains a specialized `.agents/AGENTS.md` ruleset. When invoking AI agents (like Cursor, Copilot, or Gemini), they will automatically read the `.agents/AGENTS.md` and follow these rules. 

**Rule of Thumb:**
If an AI agent accidentally generates a file with an English name (e.g. `settings_page.dart`), you must immediately instruct the AI to rename it according to the Tanglish rules (`amaippugal_thirai.dart`).

---
*Built with ❤️ using Flutter and Pure Tanglish Engineering.*
