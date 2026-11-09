# Google Play: everything to upload (Udukkai Textiles and Udukkai Factory)

Nothing here has been sent to Google. Each app is its own listing:

| | Textiles | Factory |
|---|---|---|
| Package | `com.elvan.udukkai.textiles` | `com.elvan.udukkai.factory` |
| App name (30 characters at most) | Udukkai Textiles | Udukkai Factory |
| App icon, 512 x 512 | `textiles/icon-512.png` | `factory/icon-512.png` |
| Feature graphic, 1024 x 500 | `textiles/feature-graphic-1024x500.png` | `factory/feature-graphic-1024x500.png` |
| Phone screenshots (1080 x 2160, 9:18) | `textiles/screenshots/phone/` (8) | `factory/screenshots/phone/` (7) |
| Title, short and full description | `LISTING.md` | `LISTING.md` |
| What's new | `RELEASE_NOTES.md` | `RELEASE_NOTES.md` |

Shared: `PRIVACY_POLICY.md` / `PRIVACY_POLICY.html` (Play needs a public web address: host the .html and paste its link),
`CONSOLE_FORMS.md` (answers for the Data safety, Content rating, App access, Ads, Target audience and permission forms).

## The images

- **Icon**: the launcher icon (dark charcoal gradient, white emblem), rendered from the app's own vector files, so it is the
  same black-and-white logo as the installed app. Each app has its own emblem.
- **Screenshots**: English, one language only (not bilingual), made-up businesses "Elvan Silks" (Textiles) and "Navil Silks"
  (Factory). No real customer or bank data. Taken on a phone (1080 x 2400) and cropped to 9:18, which Play accepts.
  Play wants at least 2 and at most 8 per device type. Tablet (7" and 10") screenshots are optional and were not made.
- Rebuild all images: `python -I kmp/tools/make_play_assets.py <folder of raw screenshots>`; the raw shots are named
  `t_<screen>.png` (Textiles) and `f_<screen>.png` (Factory).
- Take new screenshots: run a debug build, open the floating `< >` dev pill, choose **Seed Screenshot Demo** (loads the
  made-up business), **Toggle UI Lang** until the language is English, then **Hide pill (screenshots)**. Afterwards choose
  **Seed Textiles/Factory Data** (wipes the demo and loads your real imported data again). The pill only exists in debug builds.

## Building the release for Play (later)

`./gradlew :composeApp:bundleTextilesPlayRelease :composeApp:bundleFactoryPlayRelease` (the Play variant has no GitHub
self-updater). Version is in `composeApp/build.gradle.kts` (`versionCode`, `versionName`); every upload needs a higher
`versionCode`. Signing key: `udukkai-release.jks` + `keystore.properties` (ignored by git; keep a backup, losing the key
means no more updates). Turn on Play App Signing when you create the app.

## Before you press "Publish"

1. Put the privacy policy on a public page and enter its link (both apps).
2. Fill the forms in `CONSOLE_FORMS.md`; check every answer against what the app does at that time.
3. App access: only approved Google accounts can sign in (`AllowedAccounts`). Give Google a test account in App access, or the
   review fails at the sign-in screen. Decide open vs closed testing.
4. New personal developer accounts must run a closed test with 12 testers for 14 days before production.
5. Have the in-app privacy policy and terms checked and keep the policy link and the in-app text the same.
