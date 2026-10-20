# Agent Customization Rules

## Language Policy for Udukkai
1. **Pure Tamil (Senthamizh) Only**: When creating Tamil translations or UI labels (e.g., in `ta.dart`), STRICTLY use Pure Tamil words. 
2. **NO Sanskrit**: Do NOT use Sanskrit-derived words.
   - *Bad*: விவரங்கள் (Vivarangal), வசூல் (Vasool), ஆயத்தம் (Aayatham), மகா/மாகாணம் (Maha/Maakaanam), சின்னம் (Chinnam), திட்டம் (Thittam), விகிதம் (Vigitham).
   - *Good*: தரவுகள் (Tharavugal), திரட்டல் (Thirattal), ஒருக்கம்/நிறைவு (Orukkam/Niraivu), ஆள்நிலம் (Aalnilam), ஓவுரு (Ovuru), எடுத்துக்காட்டு (Eduthukaattu), வீதம் (Veetham).
3. **NO Urdu/Arabic**: Avoid non-native administrative words like வசூல் (Vasool - use திரட்டிய/பெற்ற).
4. **No Direct English Transliterations**: Do not transliterate English words directly into Tamil script (e.g., do not use "கவுண்டி" for County, or "செஸ்" for Cess). Translate them into meaningful native Tamil words (e.g., மண்டலம் for County, மேல்வரி for Cess). 
   - *Exception*: Standard global acronyms like "GST" or "PDF" can be used as-is in English letters.
5. **Add Grammar Markers**: Always add the correct Tamil conjunction markers (e.g., 'ப்') when combining words, like நிறுவனப் பெயர் (not நிறுவன பெயர்), வங்கிப் பெயர், கிளைப் பெயர்.

## Tanglish Variable Naming Policy for UI/Keys
1. **Use Navil Engine (extended++ mode)**: When creating ANY new folder name, file name, variable name, or localization key:
   - **Step 1**: Write the name in **Pure Tamil** first (with correct புணர்ச்சி compound doubling).
   - **Step 2**: Run it through the Navil transliteration engine in `extended++` mode (located at `uruvakkunar_karuvigal/navil/transliterate.ts`).
   - **Step 3**: Use the engine output as the final Tanglish name. Do NOT manually guess transliterations.
2. **Geminate Doubling Rules** (from Navil engine `extended++` mode):
   - க்க → `kk` (explicit doubling)
   - ப்ப → `pp` (explicit doubling)
   - ச்ச → `ch` (NOT `cch` — implicit, already long enough)
   - த்த → `th` (NOT `tth` — implicit, already long enough)
   - ட்ட → `tt` (explicit doubling)
   - ற்ற → `tr` (special)
3. **Allophony / Voicing Rules**: Retain natural phonetic softening for Tamil Stop Consonants (e.g., க inside a word becomes g not k, த becomes dh, ப becomes b). Example: மாம்பழம் -> maambazham, காகம் -> kaagam.
4. **Compound Word Exceptions**: In compound words (புணர்ச்சி), the second word's starting consonant should remain unvoiced (hard). E.g., கை + பேசி = kaippaesi (NOT kaibaesi). மண் + பானை = manpaanai (NOT manbaanai). Use overrides when generating these.
5. **Vowel Preservation (Nedil)**: Always preserve extended vowels correctly: ஆ -> aa, ஈ -> ee, ஊ -> oo, ஓ -> oa, ஏ -> ae. E.g. ஓவியம் -> oaviyam, மேல் -> mael.
6. **Shrink Long Keys**: Do not use massive sentences as keys. Shrink them to 2-3 essential words (e.g., pattiyalGstElidhu).
7. **Button Suffix**: If the key represents a button, always append Ptn (Pothan) at the end, replacing any Btn.

## Standardized Database & Core Entity Names (Rulebook)
To ensure absolute consistency across databases, JSON payloads, and core models, **ALWAYS** use the following strict Tanglish spellings for these specific concepts. DO NOT generate alternative spellings (like minnanchal, ovuru, or tholaipesi):
- **Email**: `minnanjal` (மின்னஞ்சல்) [NOT minnanchal]
- **Phone**: `tholaippaesi` (தொலைப்பேசி) [NOT tholaipesi, tholaipaesi]
- **Logo / Image**: `oavuru` (ஓவுரு) [NOT ovuru]
- **Wide Logo**: `agalaOavuru` (அகல ஓவுரு)
- **Zip / PIN Code**: `anjalKuriyeedu` (அஞ்சல்குறியீடு) [NOT anchalkuriyeedu]
- **Theme / Appearance Color**: `thoatraNiram` (தோற்ற நிறம்) [NOT thottranNiram]
- **Header Format**: `thalaippuVadivu` (தலைப்பு வடிவு) [NOT thallaippuVadivu]
- **Data / Details**: `tharavugal` (தரவுகள்) [NOT vivarangal]
- **Settings**: `amaippugal` (அமைப்புகள்)
- **Password**: `kadavuchol` (கடவுச்சொல்)
- **Invoice**: `pattiyal` (பட்டியல்)
- **Receipt (Payments)**: `patrucheettu` (பற்றுச்சீட்டு) [NOT raseedhu]

## Project Architecture & File Naming Policy (Pure Tanglish)
This project STRICTLY follows a Pure Tanglish (Navil Vili) folder and file architecture. Any new AI agent or Developer working on this codebase MUST abide by these translations when creating new files or folders.

### Top-Level Structure (`lib/src/`)
1. **`adippadai/` (Core)**: Fundamentals, base logic, databases, theme.
2. **`cheyalpaadugal/` (Features)**: Encapsulated business logic and feature modules.
3. **`koorugal/` (Widgets/Components)**: Shared/global UI elements.

### Internal Folder Mappings (Inside Features)
- **Presentation / View Layer**: `kaatchi/`
- **Pages / Screens**: `thiraigal/`
- **Widgets / Components**: `koorugal/`
- **Models**: `tharavuru/`
- **State**: `nilaimai/`
- **Repositories**: `kalanjiyam/`
- **Data Source**: `tharavu_moolam/`
- **Desktop**: `kanini/`
- **Mobile**: `kaippaesi/`
- **Reports**: `arikkaigal/`
- **Onboarding**: `varavaerpu_padigal/`

### File Naming Convention
When creating new files, use the Tanglish suffix:
- Do NOT use `_page.dart` or `_screen.dart`. USE `_thirai.dart`.
- Do NOT use `_widget.dart` or `_component.dart`. USE `_kooru.dart` or just a descriptive Tanglish name like `elvan_pothan.dart` (Elvan Button).
- Do NOT use `_model.dart`. USE `_tharavuru.dart`.
- Do NOT use `_dialog.dart` or `_modal.dart` or `_bottom_sheet.dart`. USE `_maeladukku.dart` (Overlay).

Example valid paths:
- `lib/src/cheyalpaadugal/ulnuzhaivu/kaatchi/thiraigal/ulnuzhaivu_thirai.dart` (Login Screen)
- `lib/src/koorugal/maeladukkugal/elvan_azhippu_urudhi_meladukku.dart` (Delete Confirm Modal)
- `lib/src/adippadai/tharavuthalam/app_database.dart` (Database - App layer)

### Editor Folder Structure (thiruthi/)
When a feature has **multiple editors** (e.g., invoice, customer, product), each editor MUST live in its own subfolder inside `thiruthi/`. **NEVER** place multiple editor files flat in the same `thiruthi/` folder.

**Pattern:**
```
kaatchi/
  thiruthi/
    pattiyal/                     ← Invoice editor + its components
      niril_xxx_pattiyal_thiruthi.dart
      koorugal/                   ← Components extracted from this editor
        koorugal.dart             ← Barrel file (re-exports all components)
        some_kooru.dart
    vanigar/                      ← Customer editor + its components
      niril_xxx_vanigar_thiruthi.dart
    porul/                        ← Product editor + its components
      niril_xxx_porul_thiruthi.dart
    patrucheettu/                 ← Receipt editor
      niril_xxx_patrucheettu_thiruthi.dart
```

**Rules:**
1. **One editor per subfolder** — never mix editors in the same directory.
2. **Subfolder name = entity name** — `pattiyal/` for invoices, `vanigar/` for customers, `porul/` for products, `patrucheettu/` for receipts.
3. **Components go inside the editor's folder** — `pattiyal/koorugal/`, NOT a shared `thiruthi/koorugal/`.
4. **Barrel file required** — if a `koorugal/` folder has 3+ files, create a `koorugal.dart` barrel that re-exports all components.
5. **Shared editor widgets** (used by ALL editors) stay at `thiruthi/` root level (e.g., `elvan_thiruthi_oadu.dart`).

### File Size Limits
- **Target**: 200–400 lines per file.
- **Acceptable**: Up to 600 lines for complex stateful editors.
- **Action required**: If a file exceeds 600 lines, extract sub-components into a `koorugal/` subfolder.
- **Extract candidates**: Build sections, row builders, modals, and data load/save logic (into a `_udhavi.dart` helper).

## Source Control Policy
1. **NO UNAPPROVED GIT CHECKOUTS**: Agents are STRICTLY FORBIDDEN from running `git checkout` or any command that reverts code to a previous state (e.g., `git reset --hard`) WITHOUT EXPLICIT PERMISSION from the user.
2. If you realize a mistake was made, you must either manually fix the mistake with code editing tools or ask the user for permission to run a git checkout/reset command to restore the previous state.

3. Always push to git whenever you have completed a major change or feature.
