# Play Console form answers (drafts: check each against the app when you submit)

Same answers for Textiles and Factory unless a line says otherwise.

## Store settings
- App or game: **App**. Free or paid: **Free**. Category: **Business** (tags: Business, Productivity, Finance tools).
- Contact email shown on the listing: `elvanpartha@gmail.com` (the address in the in-app privacy policy). Website and phone: optional.
- Default language: English (United Kingdom or India). Add **Tamil (ta)** as a second listing language with the Tamil texts in `LISTING.md`.

## App access (Play reviewers need to open the app)
Only approved Google accounts can sign in. Choose "All or some functionality is restricted" and give instructions plus a
**test Google account that is on the approved list** (create a dedicated account; never give your own). Text to paste:

> Sign in with the Google account given here. The app then asks for a business name; the rest works without internet.
> App lock is off by default.

## Ads
- Contains ads: **No**.

## Content rating (IARC questionnaire)
Category "Utility, Productivity, Communication or other". Violence, sexual content, language, controlled substances,
gambling: **No** to all. User-generated content shared between users: **No**. Location sharing: **No**. Expected rating: Everyone / PEGI 3.

## Target audience and content
- Target age: **18 and over** (business billing). Not designed for children; no "Families" programme.
- Appeals to children unintentionally: No.

## Data safety (what the app does today)
| Data | Collected | Shared | Why |
|---|---|---|---|
| Email address, name, profile picture (Google sign-in) | Yes | No | Account management, app functionality |
| Business records the user types (customers, items, bills, receipts, logo, signature, bank details) | Yes (synced to the user's own Firebase Realtime Database) | No | App functionality |
| App interactions, diagnostics (Firebase Analytics) | Yes | No | Analytics |
| Device or other IDs | Yes (Firebase installation id) | No | Analytics |
- Data is encrypted in transit: **Yes**. Users can request deletion: **Yes**: delete the business or data inside the app (the synced copy is
  deleted too) or email `elvanpartha@gmail.com`. No data sold. Not shared with third parties (Google / Firebase act as processors).
- Location, contacts, photos, microphone, SMS, financial info from Google Pay or cards: **not collected**. Bank details are
  text the user types into their own bills; say "financial info: user-provided, stored for app functionality" if the form asks.

## Permissions (merged manifest of the Play build)
| Permission | Why (for the declaration) |
|---|---|
| `INTERNET`, `ACCESS_NETWORK_STATE` | Sign-in and cloud sync; check whether the phone is online |
| `USE_BIOMETRIC` | App lock with the phone's fingerprint or face unlock |
| `VIBRATE` | Haptic feedback on buttons |
| `READ_EXTERNAL_STORAGE` (Android 12 and older only), `WRITE_EXTERNAL_STORAGE` (Android 9 and older only) | Save bill PDFs and backups to Documents on old phones |

`REQUEST_INSTALL_PACKAGES` and `UPDATE_PACKAGES_WITHOUT_USER_ACTION` (the GitHub self-updater) are removed in the Play build
(`src/androidPlayRelease/AndroidManifest.xml`); Play does not allow an app to update itself. No sensitive permission needing a
declaration form (SMS, call log, all-files access, accessibility, location, background location, exact alarms) is requested.

## Other declarations
- Government app: No. Financial features: the app makes bills; it does not move money, lend or trade, so answer "No" to
  the personal-loans, crypto and trading questions. Health: No. News: No. COVID-19: No.
- Advertising ID: **No** (the app does not use it). Target API level: 36 (meets Play's current requirement).
- Privacy policy: link to the hosted `PRIVACY_POLICY.html`.
- Account deletion link (required because accounts exist): the same page, section "Deleting your data".
