# Udukkai Templates

The four Udukkai bill papers as a standalone React project. No old app, no Firebase, no Capacitor, no MUI, no
app screens: only the paper templates, the CSS they need, and the helpers they call.

| Template | Component | Paper element | Props |
|---|---|---|---|
| GST invoice | `GstInvoice` | `#invoice-preview` | `profile, client, details, items, totals, invoiceType, options` |
| GST receipt | `GstReceipt` | `.print-area` | `receipt, profile` |
| Coolie invoice | `CoolieInvoice` | `.a4-paper` | `bill` (with `bill._companyProfile`) |
| Coolie receipt | `CoolieReceipt` | `.print-area` | `receipt` (with `receipt._companyProfile`) |

Wrap a template in `LanguageProvider` (it reads the UI language from `localStorage['elvanniril_language']`,
`ta` or `en`). Importing `src/index.ts` also loads the two stylesheets the papers need.

## Run and edit

```
npm install
npm run dev        # http://localhost:5180
```

The playground has a template picker, a live JSON editor for the props (a valid edit re-renders at once, an
invalid one keeps the last good paper and shows the error), the UI language, zoom, and Print / Save PDF.
Edit any file under `src/templates/` and the page reloads. Sample data is in `src/playground/samples/`
(fictional business and bills).

## The bundle an app loads

```
npm run bundle     # writes dist-bundle/: index.html, assets/, fonts/, version.txt
```

`src/bridge/` is the glue between an app and the templates, kept apart from `src/templates/`:

- `adapter.ts` turns the app's payload (as the Kotlin app builds it; examples in `verify/payload-*.json`) into the
  props above.
- `entry.tsx` registers `window.UdukkaiPrint` with the functions the Kotlin app calls today:
  `render(payload)` (then `UdukkaiNative.onReady()`, or `onError` if nothing drew), `printHtml(options)` (the
  paper-only A4 document the host prints to a vector PDF), `setTopInset` (kept, does nothing), and `buildPdf`
  (there is no image fallback in this bundle: it reports `onError` at once so the host shows its failure notice).
  `renderProps(props, lang, docType)` renders straight from template props.
- `version.ts` is the template version, written to `version.txt` in the bundle. Bump it only when the look changes,
  so an app can key its saved PDFs on it and keep them across a bundle that looks the same.

Compared with the old compiled bundle: 488 KB of JavaScript instead of 1.43 MB (1.25 MB + two lazy chunks), the CSS
file is the same size and the same content hash, the fonts (6.7 MB) are unchanged.

## Parity check (is the look the same as the old bundle?)

```
npm run dev   then open   http://localhost:5180/verify.html
```

The page renders 17 cases (4 samples, English UI, single language, changed options, 24 invoice lines, a wide-logo
header with another theme colour, a very long note) with the new code and, in the same page, with a frozen copy of
the old shipped bundle (`verify/reference/`). For each it builds the printed A4 document and compares every
element: position, size, font, size, weight, colour, background, padding, margin, border, line height, display.
Both run in the same browser state, so the display scaling cancels out. Result: 17/17 at 1.0x and at 1.25x.

The check can fail: a 1 mm change to the receipt padding failed exactly the 8 receipt cases and none of the invoices.

`verify/reference/` is the old app's code. It is only a baseline for this page; delete it when you no longer need to
compare with the old bundle.

## Layout

```
src/templates/   GstInvoice.tsx  CoolieInvoice.tsx  Receipt.tsx     the papers (the code is the old code, unchanged)
src/styles/      vadivam.css  print.css                              the old stylesheets, verbatim
src/lib/         Payanpadu.ts  mozhi/                                number words, currency, labels, translations
src/bridge/      adapter.ts  entry.tsx  version.ts  index.html       what an app loads (see above)
src/playground/  the dev pages only (playground, verify); delete them and the templates still stand alone
verify/          payload-*.json (app-style examples), reference/ (frozen old bundle)
public/fonts/    Elvan Sans (8 woff files)
```

## Known limits

- `vadivam.css` is the whole old app's global stylesheet, kept verbatim so nothing can drift. It can be trimmed
  later, one rule at a time, with the parity page.
- `en.ts` / `ta.ts` are the full translation files, not only the keys the papers use.
- The apps still use the old compiled bundle; nothing in the Kotlin app was changed.
- The parity page runs in a browser (Chromium). The apps print with Android's Chromium WebView, so the check
  matches, but it is not a measurement taken on the phone.
