# Landscape Mode Architecture & Continuation Notes

> **Status**: Landscape orientation is **temporarily locked to portrait** per user decision.  
> **Code Preservation**: All landscape components, layouts, adaptive side sheets, 2-column card grids, and touch gesture mechanics are **100% preserved** in the codebase for future development.

---

## 1. Quick Start: How to Re-Enable Landscape Mode

To re-enable landscape mode in the future, modify only two configuration points:

### Step 1: `kmp/composeApp/src/androidMain/AndroidManifest.xml`
Change `android:screenOrientation="portrait"` back to `"user"`:
```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:screenOrientation="user"
    android:configChanges="orientation|screenSize|screenLayout|smallestScreenSize|density|uiMode|keyboardHidden|keyboard"
    android:windowSoftInputMode="adjustResize">
```

### Step 2: `kmp/composeApp/src/androidMain/kotlin/com/elvan/udukkai/MainActivity.kt`
Change `SCREEN_ORIENTATION_PORTRAIT` back to `SCREEN_ORIENTATION_USER`:
```kotlin
// Change this:
requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

// Back to:
requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER
```

Rebuild the Android APK:
```powershell
cd kmp
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew --offline :composeApp:assembleTextilesDebug :composeApp:assembleFactoryDebug
```

---

## 2. Key Design Decisions & Principles

Based on user directives and Apple Human Interface Guidelines (HIG) for foldables and landscape devices:

### 1. Single Unified Screen (NO Split Screen / Master-Detail)
- **Rule**: Do **NOT** split the screen into a two-panel master-detail layout (e.g. left list + right preview).
- The screen remains a **single unified page** that adapts its content to landscape dimensions.

### 2. Two-Column Card Grid Adaptation
- In landscape mode (`maxWidth > maxHeight` or `isLandscape`), single-column card feeds stretch awkwardly wide.
- Cards are grouped into pairs using `.chunked(2)`:
  - **Textiles & Factory Dashboard**: Recent Bills feed displayed in 2 columns.
  - **Create Screen**: Invoices / Bills list displayed in 2 columns.
  - **Customer Screen**: Customer directory displayed in 2 columns.
  - **Item Screen**: Inventory items displayed in 2 columns.

### 3. Apple HIG Control Positioning
- **Leading Top Bar (Left Side)**:
  - In landscape, the **3-dot More Menu** is anchored to the **top-left** (`leadingActions` parameter on `ElvanShell` and `ElvanCollapsedBar`).
  - This keeps the right edge uncluttered and leaves room for primary actions and navigation.
- **Trailing Top Bar (Right Side)**:
  - Primary Action Button (`+` Plus button on Home screens, or `<` Chevron Back button on subpages) stays anchored at the **top-right** corner (`padding(end = 16.dp, top = 16.dp)`).

### 4. Bottom-Docked Vertical Side Navbar (`LandscapeSideNavBar.kt`)
- **Placement**: Docked to the **bottom right**, close to the edge (`padding(end = 14.dp, bottom = 8.dp)` with navigation bar bottom inset padding).
- **Stadium Pill Shape**: `RoundedCornerShape(percent = 50)` continuous pill.
- **Two-Phase Interaction Physics**:
  1. **Normal Drag / Glide**: Swiping or dragging between icons smoothly transitions selected tab with haptic feedback **without expanding or displaying text labels**.
  2. **Forced Long-Press (`>= 380ms`)**: Only an intentional press-and-hold triggers expansion. On expansion:
     - The capsule subtly widens (`50.dp -> 60.dp`) and slot height expands (`44.dp -> 46.dp`).
     - Text labels appear beneath or alongside icons.
     - Smooth spring-based animation (`spring(stiffness = Spring.StiffnessMediumLow)`).
     - Does **NOT** collide with or touch the top-right plus button (maintains >100.dp clearance).

### 5. Bottom Sheets Adapted as Side Sheets in Landscape
- Standard mobile bottom sheets look disproportionate when stretched across wide landscape screens.
- In landscape (`maxWidth > maxHeight`), sheets transform into **Right-Anchored Side Sheets**:
  - `contentAlignment = Alignment.CenterEnd`
  - Max width constrained to `420.dp`
  - Floating card appearance with `24.dp` rounded corners (`RoundedCornerShape(24.dp)`)
  - End and bottom window insets respected.
  - Implemented in:
    - `ElvanActionSheet.kt` (Action sheets & discard dialogs)
    - `ElvanSelectionBottomSheet.kt` (Item selection sheets & Date filter preset picker)
    - `CreateDateFilterSheet.kt` (inherits from `ElvanSelectionBottomSheet`)

---

## 3. Preserved Landscape Codebase Inventory

| File | Role in Landscape Mode |
|---|---|
| [`LandscapeSideNavBar.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/ui/navigation/LandscapeSideNavBar.kt) | Vertical floating pill navbar with 2-phase touch gesture physics (glide drag vs forced long-press expand). |
| [`ElvanShell.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/ui/components/shell/ElvanShell.kt) | Shell supporting `leadingActions` for left-docked 3-dot menu, landscape right-content padding (`76.dp`), and conditional vertical edge fades. |
| [`ElvanCollapsedBar.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/ui/components/shell/ElvanCollapsedBar.kt) | Top bar supporting `leadingActions` slot on the left edge. |
| [`ElvanActionSheet.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/ui/components/shell/ElvanActionSheet.kt) | Adapts to 420.dp right-side sheet when `maxWidth > maxHeight`. |
| [`ElvanSelectionBottomSheet.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/ui/components/shell/ElvanSelectionBottomSheet.kt) | Adapts to 420.dp right-side sheet when `maxWidth > maxHeight`. |
| [`TextilesHomeScreen.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/textiles/ui/TextilesHomeScreen.kt) | Unified single-screen home layout; routes 3-dot More menu to leading actions in landscape. |
| [`FactoryHomeScreen.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/factory/ui/FactoryHomeScreen.kt) | Unified single-screen home layout; routes 3-dot More menu to leading actions in landscape. |
| [`TextilesDashboardScreen.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/textiles/ui/screens/TextilesDashboardScreen.kt) | Recent bills chunked into 2-column grid in landscape. |
| [`FactoryDashboardScreen.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/factory/ui/screens/FactoryDashboardScreen.kt) | Recent bills chunked into 2-column grid in landscape. |
| [`TextilesCreateScreen.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/textiles/ui/screens/TextilesCreateScreen.kt) | Invoices/Bills chunked into 2-column grid in landscape. |
| [`FactoryCreateScreen.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/factory/ui/screens/FactoryCreateScreen.kt) | Invoices/Bills chunked into 2-column grid in landscape. |
| [`TextilesCustomerScreen.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/textiles/ui/screens/TextilesCustomerScreen.kt) | Customer cards chunked into 2-column grid in landscape. |
| [`FactoryCustomerScreen.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/factory/ui/screens/FactoryCustomerScreen.kt) | Customer cards chunked into 2-column grid in landscape. |
| [`TextilesItemScreen.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/textiles/ui/screens/TextilesItemScreen.kt) | Inventory cards chunked into 2-column grid in landscape. |
| [`FactoryItemScreen.kt`](file:///d:/Things/Padaippugal/Nadappil/Udukkai/kmp/composeApp/src/commonMain/kotlin/com/elvan/udukkai/factory/ui/screens/FactoryItemScreen.kt) | Inventory cards chunked into 2-column grid in landscape. |

---

## 4. Pending Roadmap When Resuming Landscape Development

1. **Subpage Toolbar Adaptation**:
   - In subpages (e.g., Detail views, Editor views), adapt the top bar so that the Chevron Back button `<` occupies the top-right position mirroring where the Plus button sits on the Home screen.
2. **Editor Form 2-Column Adaptation**:
   - Adapt `TextilesInvoiceEditorScreen` and `FactoryInvoiceEditorScreen` input sections (Customer header, line items, summary) into a balanced two-column form in landscape.
3. **Screen Rotation State & Animation Polish**:
   - Verify smooth transitions on device rotation so that list scroll positions and input focus are seamlessly preserved without screen rebuild visual flickers.
