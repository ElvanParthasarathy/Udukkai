# Elvan UI: design rules

Elvan UI is the look of the Udukkai apps (Textiles and Factory). It is a blend of three systems, with Elvan's own touch on top:

| From | What Elvan UI takes |
| --- | --- |
| **Samsung One UI** | The large collapsing title: a big centred title that shrinks into the top bar as you scroll, with the content starting low on the screen so the thumb reaches it. Big rounded cards. |
| **iOS (Apple HIG)** | Calm, quiet, mostly black-and-white colour. Pill controls. Confirmation before anything destructive. Text that says what happens. Checks for contrast, tap size and screen readers come from Apple's guidelines. |
| **Material You (Android)** | Ripples and pressed feedback, bottom sheets, switches, the floating bottom bar, system back, edge-to-edge with safe areas. |
| **Elvan's touch** | Tamil first, English second (two lines, never mixed on one line). One accent: the text colour itself. Cards and pills with big radii. Bills that look like printed paper. |

The code that holds these rules: `theme/Dimens.kt`, `theme/ShellColors.kt`, `theme/UdukkaiTheme.kt`, `ui/components/shell/`.

Rules marked **Exception** are chosen on purpose and are not to be "fixed".

---

## 1. Identity

1. The app is **monochrome**: white on black in dark mode, black on white in light mode. There is no brand colour on screens. Colour appears only for status (the error red `#BA1A1A` light / `#FFB4AB` dark) and inside the printed bill (the bill's theme colour).
2. The accent is the text colour (`accent`): white in dark mode, `#1D1D1F` in light mode.
3. Quiet beats loud. One bold element per screen (the large title, or one primary button). Everything else is calm.
4. Tamil is the main language. A name or label shows in Tamil on the first line and English on the second, in a lighter grey. The two languages never share a line.

## 2. Colour

Use the named colours from `ShellColors`. Never write a raw hex in a screen, except the ones listed in section 2.3.

### 2.1 Surfaces

| Role | Dark | Light |
| --- | --- | --- |
| Screen background | `#000000` | `#F5F5F7` |
| Card / section surface | `#111111` | `#FFFFFF` |
| Pill / floating bar | `#1E1E1E` | `#FFFFFF` |
| Icon chip background | white 8% | black 6% |
| Divider | white 4% | black 4% |

### 2.2 Text (all measured to pass 4.5:1 on their surface)

| Role | Dark | Light | Use |
| --- | --- | --- | --- |
| Primary | white | black | Names, amounts, titles |
| Secondary | white 60% | black 64% | Second language, dates, descriptions |
| Tertiary | white 50% | black 56% | Weekday labels, the "FY 26-27" tag |
| Quaternary | white 24% | black 26% | **Decorative icons only, never text** |

Do not go lighter than tertiary for any text. Faint grey that cannot be read is not allowed.

### 2.3 Allowed raw colours

The login and welcome pages use the Neram palette (`NeramColors` in `NeramAuth.kt`). Printed bills use their own theme colour. Nothing else.

### 2.4 Light and dark

- Both modes must work. The theme default is **Automatic** (follows the phone).
- Every colour has a light and a dark value, named by role, never by colour name.

## 3. Type

1. One typeface: **Elvan Sans** (Tamil and English). Do not add another.
2. Sizes in use:

| Use | Size / weight |
| --- | --- |
| Large title (expanded) | 34 sp bold; shrinks to 22 sp in the top bar |
| Login and welcome title | 23 sp bold |
| Card name | 16.5 sp extra-bold |
| Amount on a card | 15 sp extra-bold |
| Body, field text | 16 sp |
| Secondary line on a card | 13 to 13.5 sp |
| Hint text, small notes | 12 to 13 sp |
| Section title | 12 sp semi-bold, upper case |
| Bottom navigation label | **9.5 sp, Exception (see section 10)** |

3. Nothing below 11 sp, except the navigation labels.
4. Long titles shrink (down to 24 sp) so they show whole, instead of ending in "...". Names on cards and rows may use an ellipsis.
5. Amounts use Indian grouping with rupee sign: `₹1,14,765.00`. Whole-rupee prices may drop the decimals: `₹8,500`. Always through `CurrencyUtils.formatInr`, never by hand.
6. Dates are `DD/MM/YYYY`.

## 4. Space

1. Everything sits on a **2 dp grid** (4, 8, 12, 16, 20, 24, 32, 40, 48).
2. Side margins:
   - **12 dp** on screens made of cards (home, lists, settings). This is `Dimens.ContentPadding`.
   - **16 dp** on form screens (all editors) and on the login and onboarding pages.
3. Space between sections: 16 dp (`SectionSpacing`). Between items in a list: 12 dp (`ItemSpacing`).
4. Card padding: 16 dp all round (20 dp sideways on settings rows).
5. The top of a screen: a 280 dp expanded header, then content. The bottom: 110 dp clear above the floating navigation, and 32 dp on sub-pages.
6. Odd values (1, 1.5, 3, 11, 13 dp) are allowed only for hairlines and hand-tuned alignment inside cards. Do not add new ones.

## 5. Shape

| Thing | Radius |
| --- | --- |
| Cards | 24 dp (`CardRadius`) |
| Stats cards | 22 dp |
| Pills, buttons, text fields (one line), segmented controls | full round (999 dp) |
| Multi-line text field | 16 dp |
| Icon chips | circle, or 12 to 14 dp |
| Preview tiles (theme, setup icons) | 24 to 36 dp |

No sharp corners and no borders on cards. Separation comes from the surface colour and the 12 to 16 dp gaps.

## 6. Layout and navigation

1. **Large collapsing title** on every top-level screen (One UI): the title is centred and large, then moves left and shrinks as the list scrolls. Sub-pages show a back arrow and the title at the left.
2. **Floating bottom bar** (iOS and Material mix): four tabs (Home, Create, Items, Customers), a round "+" button beside it, never docked to the edge. Selected tab has a filled icon and a soft pill behind it.
3. Search, filter and "more" sit at the right of the title row, as round 44 dp buttons.
4. Sheets come from the bottom (Material). One sheet at a time, with a way to dismiss it.
5. The back gesture and system back always step back one level.
6. Content respects the safe areas (status bar, gesture bar, keyboard). Screens that scroll add bottom space for the floating bar.
7. A list **keeps its scroll position** when you open an item and come back. Never reload a list to the top.
8. Lists scroll continuously. No "load more" buttons and no pagers.

## 7. Components

### Cards (bill, receipt, item, customer)

- Icon at the left, Tamil name (bold), English name (grey), then a detail row.
- Right side: status over amount.
- The status says what is true: **Paid** (பெற்றது) or **Unpaid** (நிலுவை). Never "Draft" for an issued bill.
- The bill number is short (`SJPS-010`), with the small financial-year tag beside it (`FY 26-27`). The date sits on the line below with the place.
- The whole card is one touch target, and a screen reader reads it as one item.
- Long press starts selection mode.

### Buttons

- Primary: full width, **56 dp** tall, pill, accent fill, bold 16 sp text. One per screen.
- Secondary: text only, bold, secondary colour, at least **44 dp** tall.
- Round icon buttons: at least **44 dp**.
- Press feedback is a ripple (bounded) on every touch target. Cards also shrink a little (to 98.5%) while pressed.

### Fields

- A label above the field, the hint inside. Pill-shaped, soft chip background.
- Number fields show their prefix (`₹`) or suffix (`%`).
- Errors are plain sentences under the field or in a message at the foot, never a pop-up.

### Settings

- Rows inside one rounded section: icon chip, title, grey description, chevron.
- Switches and radio choices are the whole row (label and control together).

### Confirmations

- Anything destructive asks first, in a bottom sheet with a red confirm button.
- Deleting a bill that has a receipt is refused with a message.
- Bills in a closed financial year ask "Edit anyway?" and stay open until the app closes.

## 8. Words

1. A button says what it does: "Save", "Restore", "Allow permission". Not "Submit" or "OK".
2. An action keeps its name through the whole flow.
3. Errors say what went wrong and how to fix it, with no apology.
4. Every label comes from the translation tables (`localization/`), in Tamil and English. No English-only words in code.
5. Screen-reader labels are translated too.

## 9. Motion

1. Short and purposeful: 100 to 500 ms. The login pieces fade in and rise one after another.
2. **When the phone has animations turned off, onboarding stays still.** Use `systemAnimationsEnabled()`.
3. Nothing important is carried by motion alone.

## 10. Exceptions chosen by Elvan

These break an Apple rule on purpose. Do not change them without asking.

1. **Text size is fixed.** The app ignores the phone's "larger text" setting (`MAX_FONT_SCALE = 1.0` in `UdukkaiTheme.kt`). The layouts are tuned for Tamil at this size, and larger text looked bad on real screens.
2. **Bottom navigation labels are 9.5 sp**, below the 11 sp minimum, so four Tamil labels fit in the bar.
3. **The app has its own Light / Dark / Automatic switch** (Apple prefers following the phone only).

## 11. Accessibility floor (always)

1. Every icon-only control has a translated screen-reader label.
2. Choices that look like radio buttons or switches are real radio buttons and switches, with their state.
3. Tap targets are **44 dp** or more.
4. Text is **4.5:1** contrast or better against its surface, in both modes. Large or bold text, and icons, **3:1**.
5. Nothing is shown by colour alone (status is also a word).
6. Fields offer password-manager hints (email, password).

## 12. Printed bills

The paper follows the app's calm look: Elvan Sans, one theme colour (green or purple), rounded boxes.

1. Tamil first, English second, in two lines.
2. Number: short, e.g. `SJPS-010`. Numbers restart at 001 every April for each business.
3. The financial year (`நிதியாண்டு • Financial Year: 2026-27`) is a quiet grey line in the totals info block, top line, never bold and never beside the bill number.
4. The contact band sits at the foot of **every page**. Long bills continue onto further pages, and the table header repeats on each page.
5. Saved PDF file names carry the year (`INV_SJPS-010_26-27.pdf`) so two years never overwrite each other. The number printed on paper stays short.

## 13. Before you ship a screen

- [ ] Margins 12 dp (cards) or 16 dp (forms); everything on the 2 dp grid.
- [ ] Only named colours; checked in light and dark.
- [ ] Every touch target 44 dp or more, with a ripple.
- [ ] Every icon button has a translated label.
- [ ] Tamil first, English second; text from the translation tables.
- [ ] Long text shrinks or truncates safely; no clipped titles.
- [ ] The list keeps its place on return.
- [ ] Destructive actions ask first.
- [ ] Looks right at the normal text size and with animations off.
