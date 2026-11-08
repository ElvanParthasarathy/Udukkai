# Elvan Navil UI: design reference

Elvan Navil UI is the look and feel of the Udukkai apps (Textiles and Factory). This document describes what the app **does today**, checked against the source. Where something is only a wish, it is under "Planned". Where a rule was kept from the earlier version of this document but could not be re-checked, it says so.

**Last audited against branch `feat/one-ui-interactions`** (selection, dates, pickers and menu morph included). On-device checks were made on one phone (Samsung SM-A536E, Android 16, One UI 8) in portrait: Textiles and Factory in dark theme, Textiles also in light. Landscape was not checked on the device.

## How to read the status marks

| Mark | Meaning |
| --- | --- |
| **Implemented** | In the code and seen working (source file given). |
| **Carried over** | Rule kept from the earlier document; not re-measured in this audit. |
| **Planned** | Not built. Do not describe it to users as existing. |
| **Exception** | Chosen on purpose. Not to be "fixed" without asking. |

Where the rules live: `theme/` (`Dimens.kt`, `Spacing.kt`, `Shape.kt`, `ShellColors.kt`, `Type.kt`, `MotionTokens.kt`, `Transitions.kt`, `UdukkaiTheme.kt`) and `ui/components/` (`shell/` holds the shell, bars, sheets and menus).

---

## 1. Identity

Elvan Navil UI blends four things:

| From | What it takes |
| --- | --- |
| **Samsung One UI** | The large collapsing title with content starting low; big rounded cards; the round checkbox that slides in on selection; floating pill bars; drag-to-select like My Files and Photos. |
| **iOS (Apple HIG)** | Calm colour, pill controls, frosted glass on floating bars and menus, springy open and close, confirmation before anything destructive. |
| **Material You** | Ripples, bottom sheets, system back and predictive back, edge-to-edge with safe areas. |
| **Elvan's touch** | Tamil first, English second (two lines, never mixed on one line); a monochrome base with colour only where it carries meaning; bills that look like printed paper. |

Principles (**Carried over**):
1. Quiet beats loud: one bold element per screen.
2. Tamil is the main language. A name shows in Tamil on the first line and English on the second, in a lighter grey.
3. Colour carries meaning (see section 2.3); it is never decoration.

## 2. Colour

Use the named colours from `ShellColors` (`LocalShellColors` / `rememberShellColors()`). Do not write a raw hex colour in a screen. The exceptions are listed in section 2.4.

### 2.1 Surfaces and text. **Implemented** (`theme/ShellColors.kt`)

| Role | Dark | Light |
| --- | --- | --- |
| `background` (screen) | `#000000` | `#F5F5F7` |
| `surface` (cards, sections) | `#111111` | `#FFFFFF` |
| `floatingBg`, `pillBackground` (bars, pills, menus) | `#1E1E1E` | `#FFFFFF` |
| `floatingBorder`, `pillBorder` | `#333333` | `#FFFFFF` / `#E5E5E7` |
| `iconBg` (icon chip) | white 8% | black 6% |
| `divider` | white 4% | black 4% |
| `ripple` | white 16% | black 8% |
| `accent` / `modeAccent` | white | `#1D1D1F` / black |
| `accentContainer` | white 12% | black 8% |
| `textPrimary` | white | black |
| `textSecondary` | white 60% | black 64% |
| `textTertiary` | white 50% | black 56% |
| `textQuaternary` | white 24% | black 26% |

- Quaternary is for decorative icons only, never for text. Do not go lighter than tertiary for any text.
- Contrast figures (4.5:1 for text, 3:1 for large text and icons) are **Carried over**; they were not re-measured.

### 2.2 Status

| Token | Dark | Light |
| --- | --- | --- |
| `error` | `#FFB4AB` | `#BA1A1A` |
| `danger` | `#FF5252` | `#E53935` |
| `success` | `#4CAF50` | `#2E7D32` |

### 2.3 Colour that carries meaning. **Implemented**

The earlier version said the app has no colour on screens. That is out of date. Two kinds of colour are used on purpose:

1. **Entity colours**, on the round icon of a card, with a soft gradient on the glyph. Each kind of record keeps one colour:

| Record | Dark | Light |
| --- | --- | --- |
| Invoice / bill (`invoiceColor`) | `#5C9DFF` | `#1E7BF2` |
| Receipt (`receiptColor`) | `#4FCB8A` | `#16A864` |
| Item (`itemColor`) | `#F4B94F` | `#E8930C` |
| Customer (`customerColor`) | `#B79CF7` | `#8A5CE6` |

   Each also has a two-stop gradient (`invoiceGradient`, and so on). `ShellColors.gradientFor(color)` picks it. Use `ElvanCardLeadingIcon` / `ElvanTintedIcon` (`ui/components/ElvanCommonCard.kt`) so the icon, circle wash and gradient stay consistent.

2. **Action blue** (`link`, `#0072DE`, in both modes): the Save button of an edit container, switches and text links. Primary actions in settings use this, not the accent.

The accent stays monochrome (white in dark, near-black in light). Sheets use the accent for their filled confirm button.

### 2.4 Allowed raw colours

- The login and welcome pages use their own palette (`NeramColors` in `ui/screens/onboarding/screens/NeramAuth.kt`).
- Printed bills use the bill's theme colour (section 11).
- The action sheet's body uses fixed near-black `#161616` and near-white `#FAFAFA` (`ElvanActionSheet.kt`), because it sits in its own window (section 8). The selection sheet uses `surface` instead.

### 2.5 Light and dark. **Implemented** (`theme/ThemeManager.kt`)

- Both modes must work. The choice is Light, Dark or Automatic (the phone's setting). The default is Automatic.
- Every colour has a light and a dark value, named by role, never by colour name.

## 3. Type

1. One typeface: **Elvan Sans**, Tamil and English (`FontManager.currentFontFamily` always returns it). Do not add another. **Implemented**
2. **Exception:** the app ignores the phone's "larger text" setting. `MAX_FONT_SCALE = 1.0` in `UdukkaiTheme.kt`; layouts are tuned for Tamil at this size.
3. Sizes seen in the code:

| Use | Size |
| --- | --- |
| Large title, expanded (`ElvanExpandedBar`) | 34 sp (it shrinks into the top bar on scroll) |
| Date picker headline | 22 sp semi-bold |
| Card name | 16.5 sp (`CustomerCards.kt`, `DocCardText.kt`) |
| Secondary line on a card | 13.5 sp |
| Detail lines on a card | 12 to 13 sp |
| Settings row title / description | 15 sp medium / 12 sp (`ElvanSettingsComponents.kt`) |
| Bottom navigation label | **9.5 sp. Exception** (four Tamil labels must fit; `BottomNavBar.kt`) |

   The Material scale in `Type.kt` (`createTypography`) exists and is used where a screen does not set its own size. The weights of card text (the earlier version said extra-bold for names and amounts) were not re-checked.
4. The rule "nothing below 11 sp" is **not fully true in code**: the banner tag in `ElvanExpandedBar.kt` uses 10.5 sp. Keep new text at 11 sp or more.
5. Long titles shrink so they show whole instead of ending in "...". Names on cards and rows may use an ellipsis.
6. Amounts use Indian grouping with the rupee sign (`₹1,14,765.00`); whole-rupee prices may drop the decimals. Always through `CurrencyUtils.formatInr`, never by hand.
7. Dates are `DD/MM/YYYY` through `DateUtils.formatDate`. See the known gap on time zones in section 12.

## 4. Space

1. A 2 dp grid. `theme/Spacing.kt`: 2, 4, 8, 12, 16, 24, 32, 48 dp.
2. `theme/Dimens.kt`:

| Token | Value |
| --- | --- |
| `ContentPadding` (side margin on card screens) | 12 dp |
| `SectionSpacing` | 16 dp |
| `ItemSpacing` | 12 dp |
| `CardRadius` | 24 dp |
| `CardPaddingHorizontal` / `CardPaddingVertical` | 20 dp / 16 dp |
| `ContentPaddingBottom` | 110 dp + navigation bar (90 dp + keyboard while typing) |
| `SubpageContentPaddingBottom` | 32 dp + navigation bar (24 dp while typing) |
| `FloatingBarBottomGap` | 11 dp |

3. Form screens use a 16 dp side margin (**Carried over**).
4. The expanded header is 280 dp tall (`ElvanExpandedBar`); the list's first spacer is `LocalElvanTopSpacerHeight`.
5. Odd values (1, 1.5, 3, 11, 13 dp) only for hairlines and alignment inside cards. Do not add new ones.

## 5. Shape

| Thing | Radius |
| --- | --- |
| Cards | 24 dp (`Dimens.CardRadius`) |
| Material shapes (`UdukkaiShapes`) | 4, 8, 12, 16, 24 dp |
| Stats card skeleton | 22 dp (`ElvanShimmer.kt`) |
| Pills, buttons, one-line fields, segmented controls | fully round |
| Menus (popup) | 24 dp |
| Action sheet | 32 dp (portrait), 24 dp (landscape side sheet) |
| Selection sheet | 28 dp top corners |
| Icon chips | circle, or 12 to 14 dp |

No sharp corners and no borders on cards. Floating bars and menus carry a 0.5 dp hairline border and a soft shadow.

## 6. Layout and navigation

All **Implemented** unless marked.

1. **Large collapsing title** (`ElvanShell`, `ElvanExpandedBar`, `ElvanCollapsedBar`): top-level screens show a big centred title that collapses on scroll. Sub-pages use `ElvanSubShell` with a back button; view pages can stay small (`collapsedOnly`).
2. **Floating bottom bar** (`ui/navigation/BottomNavBar.kt`): four tabs (Home, Create, Items, Customers), 56 dp tall, frosted glass, with a separate 56 dp round "+" button. The selected tab has a filled icon and a soft pill that stretches as it moves.
3. Search, filter and "more" sit at the right of the title row as round buttons (50 dp pills, `ElvanPill`).
4. **Landscape:** a side rail replaces the bottom bar (`LandscapeSideNavBar`) and the header is compact. Landscape is supported on the main screens; sub-page Activities are locked to portrait.
5. **Sub-pages are separate Android Activities** (`SettingsActivity`, `RecycleBinActivity`, editors, view screens), opened through `openPlatformSubpage`. They use the system's window transition and predictive back. On desktop they stay in one window with a horizontal slide (`Transitions.smoothHorizontal`).
6. A list keeps its scroll position when you open a record and come back (**Carried over**). Lists scroll continuously; no pagers.
7. Content respects safe areas and the keyboard. Screens that scroll add bottom space for the floating bar.
8. `ElvanFastScroller` (draggable, date-aware on the Create screens) and `ElvanSimpleScrollbar` (passive, on sub-pages) show position. `ExpressivePullToRefreshBox` refreshes lists without recreating their content.

## 7. Selection

Implemented in this audit's work; matches Samsung My Files and Google Photos (measured on the phone).

1. **Start:** long-press a card, or hold and slide. In selection mode a tap toggles a card; outside it a tap opens the record.
2. **Hold and slide** (`ui/components/DragSelect.kt`): hold a card, selected or not, and slide, as often as you like. Every card between where the hold began and the finger is selected, in reading order (row by row, left to right in landscape). Slide back and the cards the finger leaves are **unselected again**, including ones selected earlier. Cards outside the slide are never touched.
3. A hold that lands on a header, filter pills or a gap between cards does not start a drag. Plain scrolling never selects. A touch the phone takes for its Back gesture never counts as a hold: gesture code must test `!change.pressed`, because a cancelled touch arrives already consumed and `changedToUp()` stays false.
4. Near the top or bottom edge the list scrolls by itself, faster closer to the edge, and selection keeps following.
5. The selection bar stays visible during a drag.
6. Cards show a round checkbox that slides in at the left; a selected card gets only the tick, no tint (`ElvanCardSelector`, `ElvanCheckMark`). The header changes to "N selected" with Select all and Cancel.
7. The bar at the foot (`ElvanSelectionBar`) offers Copy (when exactly one card is selected and the tab allows it) and Delete. The tab bar and the selection bar swap with `elvanBarSwap()`: the incoming one swells in on a spring, the outgoing one shrinks and fades.
8. Back clears the selection first.
9. List keys must be stable record keys (`item_12`, `cust_3`, `invoice_...`, `receipt_...`; landscape rows `item_pair_12_13`). `recordIdsOfKey` reads ids from them. Use these key shapes in any new selectable list.

## 8. Components

### Cards (bill, receipt, item, customer). **Implemented** (`ElvanCommonCard`)
- Round entity icon at the left, Tamil name (bold), English name (grey), detail rows, and status over amount at the right.
- The whole card is one touch target. A card dips to 97.5% while pressed and springs back (it is calm in selection mode: no ripple or dip beyond that).
- The status says what is true: Paid or Unpaid. The short bill number has the financial-year tag beside it (`FY 26-27`) (**Carried over** for the wording rules).

### Menus. **Implemented** (`ElvanPopupMenu`, `MenuHost`)
- A menu's outline starts as its button and opens out to the full panel in 230 ms, corners easing to 24 dp, without overshoot; it closes back into the button in 170 ms. The panel is revealed by the outline, never scaled, so the rows do not squash. It waits for the panel to be composed before starting, or the opening would be spent before it is on screen. The panel reaches 4 dp past the button and stays 8 dp inside the screen. It is frosted glass over the page where haze is available.
- One UI 8's own menu fades with a slight scale near its button. The morph here is a deliberate choice, with the same timing family.
- **Menu to page:** choosing Settings or Recycle Bin hands the menu panel's bounds to the next Activity (`ElvanMenuState.clickedPanel`), and Android grows the new window out of that rectangle (`ActivityOptions.makeClipRevealAnimation`). The panel stays until it is covered. On Android 14 and later the page shrinks back toward the menu when closed (`res/anim/menu_reveal_close.xml`). Older versions keep the normal close. The system runs this, so it holds across the window change and with predictive back.
- A second tap while a page is opening is ignored.

### Floating bars. **Implemented** (`ElvanFloatingActionBar`)
- A pill as wide as its buttons (never edge to edge), 56 dp tall, up to five buttons; extra buttons go into a More menu above the bar.

### Sheets. **Implemented**
- **Action sheet** (`ElvanActionSheet`): a floating card from the bottom, closed by tapping outside, Back or its buttons (not by swiping). Title, optional custom content, Cancel and Confirm; `isConfirmFilled` makes Confirm an accent pill.
- **Selection sheet** (`ElvanSelectionBottomSheet`, hosted by `ElvanBottomSheetController`): list with search, drag handle, drag-down to close.
- Both are translucent where the phone can blur the screen behind them (about 86% to 88% opaque with window blur); where it cannot, they are **95% opaque** (`dialogBlurAvailable()`). They are in their own window, so Haze cannot reach them; floating bars and menus inside the page use Haze (`frostedGlass`).
- One sheet at a time, always with a way to dismiss it (except a sheet that must finish, such as a progress sheet).

### Frosted glass. **Implemented** (`shell/FrostedGlass.kt`)
`Modifier.frostedGlass(shape, colors, fallbackAlpha)`: Haze blur (24 dp radius) with a tint of the bar colour, where a shell provides a Haze source. Without one (no shell, or Android below 12) it falls back to the plain translucent bar colour (about 88% to 94%).

### Pickers. **Implemented** (this audit's work)
- **Date:** `ElvanDatePicker` (one day) and `ElvanDateRangePicker` (first tap starts, second ends in either order, a third starts again). In the action sheet, which fades in with a 32 dp rise: the chosen date large at the top, the month centred between its arrows, a weekday row that stays still, and six fixed rows of round 42 dp day cells (34 dp when the window is short) that page sideways, forward moving left, by arrow (about 130 ms, rapid taps retarget) or swipe. Days of the months either side show dimmed and can be chosen, which moves to that month. A tap on the month cross-fades the grid in place for day, month and year wheels (month and year only for a range); the arrows go at once and the caret flips. A ring on today, Sundays in the error colour, selected days in the accent, the range as a soft band. Always six weeks tall, so the sheet never resizes. A month is drawn in one pass, not as 42 composables: composing them held the main thread for about 250 ms per step. Optional `minMillis`/`maxMillis` grey days out; the opening day is pulled inside them. Month and weekday names are Tamil, or English for English and Tamil Latin. Each day has a spoken label and a selected state.
- Dates are UTC midnights, like every date in the app. Calendar rules (leap years, month lengths, weekday, month rollover, limits, range taps) are in `ElvanCalendar` and unit-tested.
- **Colour:** `ElvanColorPicker`: a Swatches | Spectrum shifter over a colour area of one fixed height (an 11 by 10 grid with the chosen swatch raised, or a hue and saturation square with a brightness bar), the old and new colour side by side, editable hex (`#rgb` or `#rrggbb`) and red, green and blue fields, and five recent-colour slots (the caller keeps them with `pushRecentColor` and `parseRecentColors`). Colour follows the finger from the moment it lands. A field keeps what is typed until focus leaves it; a value that does not parse shows in the error colour and changes nothing. The PDF theme uses it through `BillColorPickerDialog`, which darkens colours too pale to read on white paper.
- Do not use the stock Material date or colour pickers.

### Buttons. **Implemented** (sizes differ by place)
- Settings edit containers: a Cancel text button and a pill **Save** in action blue (`link`), 14 sp.
- Sheets: Cancel and Confirm pills (44 to 48 dp high).
- Onboarding and login: full-width 56 dp pills.
- Top-bar action buttons (`ElvanActionButton`): 44 dp.
- The earlier rule of one 56 dp accent primary on every screen was only true of onboarding and login.
- Press feedback is a bounded ripple on touch targets.

### Fields. **Implemented** (`ElvanSettingsTextField`, `ElvanAuthTextField`)
- Label above, pill-shaped (fully round) fill. Number fields show their prefix (`₹`) or suffix (`%`). Errors are plain sentences, not pop-ups.

### Settings. **Implemented** (`ElvanSettingsComponents.kt`)
- Rows inside one rounded section: icon chip, title, grey description, chevron or switch (`ElvanSettingsRow`, `ElvanSimpleSettingsRow`, `ElvanSettingsSwitch`). Editing swaps a row for an edit container (`ElvanSettingsAnimatedExpand`, `ElvanSettingsEditContainer`).

### Feedback
- Toast (`ElvanSnackbar`): a floating pill, shown for 2.5 seconds, above the bottom bar when one is visible.
- Skeleton loading (`ElvanShimmer`, card skeletons) and the expressive loaders (`ExpressiveLoadingIndicator`, `ExpressiveDotsLoader`).

### Confirmations. **Carried over**
- Anything destructive asks first, with a red confirm. Deleting a bill that has a receipt is refused with a message. Bills in a closed financial year ask "Edit anyway?".
- An editor with unsaved changes asks Save / Discard / Continue.

## 9. Words. **Carried over**

1. A button says what it does: "Save", "Restore". Not "Submit" or "OK".
2. An action keeps its name through the whole flow.
3. Errors say what went wrong and how to fix it.
4. Every label comes from the translation tables (`localization/`, keys in `K`). Screen-reader labels too. The languages are Tamil, English, Tamil Latin and System. Where a label cannot come from the tables (a spoken month name on a calendar), use `DateGroupUtils`.

## 10. Motion

1. Durations in `MotionTokens`: 250 ms short, 300 ms medium, 500 ms long, 700 ms extra long. Easings: Emphasized decelerate / accelerate and Standard decelerate / accelerate. Open with a decelerate, close with an accelerate and a shorter time.
2. Springs for things that grow from a control: menus (about 0.78 damping), the bar swap (0.72 to 0.82), the card press release.
3. Motion is interruptible: Back during an opening reverses it; a second tap is ignored; nothing waits for an animation to finish before responding.
4. **When the phone has animations turned off, onboarding stays still** (`systemAnimationsEnabled()`). Other screens were not audited for this.
5. Nothing important is carried by motion alone.

## 11. Printed bills

The bill is drawn by a bundled web print app (`androidMain/assets/print/`), not by Kotlin, so its layout could not be re-checked from source in this audit.

- **Implemented and checked:** the saved PDF name carries the financial year (`INV_SJPS-010_26-27.pdf`, from `BillNumbering.fyShort`), so two years never overwrite each other. The theme colour is chosen in Settings: Green `#388E3C`, Violet `#6A1B9A`, Blue `#1565C0`, Red `#C62828`, Pink `#AD1457`, or any colour from the colour picker.
- **Carried over:** Tamil first, English second in two lines; short bill numbers that restart each April; the financial-year line in the totals block; the contact band at the foot of every page; the table header repeating on long bills.

## 12. Exceptions and known gaps

### Exceptions chosen by Elvan
1. Text size is fixed (section 3).
2. Bottom navigation labels are 9.5 sp.
3. The app has its own Light / Dark / Automatic switch.

### Known gaps (true today)
1. Bills saved before this change with a raw clock time between midnight and 5:30 am still show the day before; their stored values were left alone.
2. The date wheels stop at their ends rather than wrapping round (December to January needs a scroll back through the year). Holding an arrow does not repeat.
3. Landscape has not been exercised on the device for the selection, picker or menu work.
4. The menu-to-page reveal depends on the phone's window manager honouring `makeClipRevealAnimation`. It works on the test phone; other makers' skins are untested.
5. Smaller text than 11 sp exists in one place (section 3).
6. The date picker has no limits wired in yet; invoices accept any date, as before.

### Planned (not built)
- A "jump to today" shortcut in the date picker.
- Reducing motion for users who turn animations off, beyond onboarding.

## 13. Accessibility floor

1. Every icon-only control has a translated screen-reader label. (Calendar days and month arrows speak their date.)
2. Choices that look like radio buttons or switches are real ones, with their state.
3. Tap targets are 44 dp or more where the layout allows. The bottom navigation tabs are tight; check new bars against it.
4. Nothing is shown by colour alone (status is also a word).
5. Fields offer password-manager hints (email, password). **Carried over**

## 14. Before you ship a screen

- [ ] Margins 12 dp (cards) or 16 dp (forms); everything on the 2 dp grid.
- [ ] Only named colours from `ShellColors`, checked in light and dark.
- [ ] Entity icons use `ElvanCardLeadingIcon`; action buttons use action blue; nothing else gets colour.
- [ ] Touch targets 44 dp or more, with a ripple.
- [ ] Every icon button has a translated label.
- [ ] Tamil first, English second; text from the translation tables.
- [ ] Selectable lists use stable record keys and `dragSelect`.
- [ ] Menus, sheets and floating bars use the shared components and `frostedGlass`, with the translucent fallback.
- [ ] Dates and colours use the Elvan pickers.
- [ ] The list keeps its place on return; Back and interrupted animations behave.
- [ ] Destructive actions ask first.
- [ ] Looks right at the fixed text size, with animations off, and in landscape where the screen supports it.
