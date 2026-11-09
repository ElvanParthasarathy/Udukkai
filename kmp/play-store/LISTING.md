# Google Play listing: drafts to review before publishing

Everything here is a draft written from what the app does today. Read it, change it, and check each answer against the
Play Console forms before sending. Nothing here has been sent to Google.

## Apps

| | Package | Name |
|---|---|---|
| Textiles | `com.elvan.udukkai.textiles` | Udukkai Textiles |
| Factory | `com.elvan.udukkai.factory` | Udukkai Factory |

Build the Play bundles with `bundleTextilesPlayRelease` / `bundleFactoryPlayRelease` (see `README.md`).
The signing key (`udukkai-release.jks`) and `keystore.properties` are ignored by git: keep a backup of them elsewhere.
Losing the key means no more updates for the same listing.

## Textiles: short description (80 characters at most)

- English: `GST bills, receipts and customers for silk and textile shops, in Tamil and English.`
- Tamil: `பட்டு, ஜவுளிக் கடைகளுக்கான GST பட்டியல், பெறுகை, வாங்குநர் - தமிழ், ஆங்கிலத்தில்.`

## Textiles: full description

English:

> Udukkai Textiles makes billing simple for silk and textile shops.
>
> - Tax invoices with GST (CGST + SGST or IGST), in Tamil, English or both on the same bill
> - Payment receipts linked to the bills they pay
> - Customers (businesses with GSTIN, or individuals), items and prices
> - Your logo, signature, bank details and bill colour on every PDF
> - Print, share or save bills as PDF
> - Works fully without internet; syncs to your other devices when it is back
> - Backup and restore from a file, with a safety copy kept before every restore
> - App lock with your phone's screen lock or fingerprint

Tamil:

> உடுக்கை சவுளி, பட்டு மற்றும் ஜவுளிக் கடைகளுக்கான பட்டியல் செயலி.
>
> - GST (CGST + SGST அல்லது IGST) உடன் வரிப் பட்டியல்; தமிழ், ஆங்கிலம் அல்லது இரண்டும்
> - பட்டியலுடன் இணைந்த பெறுகைகள்
> - வாங்குநர்கள் (GSTIN உள்ள நிறுவனம் அல்லது தனிநபர்), பொருட்கள், விலைகள்
> - ஒவ்வொரு PDF-இலும் உங்கள் லோகோ, கையொப்பம், வங்கி விவரம், நிறம்
> - PDF ஆக அச்சிடலாம், பகிரலாம், சேமிக்கலாம்
> - இணையம் இல்லாமலும் முழுமையாக வேலை செய்யும்; இணையம் வந்ததும் மற்ற சாதனங்களுடன் ஒத்திசையும்
> - கோப்பிலிருந்து காப்பு மீட்பு; ஒவ்வொரு மீட்புக்கு முன்பும் பாதுகாப்பு நகல்
> - உங்கள் கைபேசியின் திரைப் பூட்டு அல்லது கைரேகையுடன் செயலி பூட்டு

## Factory: short description

- English: `Job-work bills and receipts for silk twisting and coolie work, in Tamil and English.`
- Tamil: `பட்டுநூல் பணிக்கட்டணப் பட்டியல், பெறுகை - தமிழ், ஆங்கிலத்தில்.`

## Factory: full description

English:

> Udukkai Factory is for job-work businesses that bill by weight and work done.
>
> - Job-work bills with items, weight, rate, wastage, courier and other charges
> - Payment receipts, customers and items
> - Tamil, English or both on every bill
> - Logo, signature, bank details and bill colour on every PDF
> - Works without internet and syncs when it is back
> - Backup and restore from a file; app lock with your phone's screen lock

Tamil:

> உடுக்கை ஆலை, எடை மற்றும் செய்த வேலையின் அடிப்படையில் கட்டணம் போடும் பணிக்கட்டணத் தொழில்களுக்கானது.
>
> - பொருள், எடை, விகிதம், கழிவு, தபால் மற்றும் பிற கட்டணங்களுடன் பட்டியல்
> - பெறுகைகள், வாங்குநர்கள், பொருட்கள்
> - ஒவ்வொரு பட்டியலிலும் தமிழ், ஆங்கிலம் அல்லது இரண்டும்
> - லோகோ, கையொப்பம், வங்கி விவரம், நிறம்
> - இணையம் இல்லாமலும் வேலை செய்யும்; இணையம் வந்ததும் ஒத்திசையும்
> - கோப்பிலிருந்து காப்பு மீட்பு; கைபேசித் திரைப் பூட்டுடன் செயலி பூட்டு

## Assets Play asks for

All of them are in this folder: see `README.md` (icons, feature graphics and screenshots per app, privacy policy, form answers).

## Data safety form: draft answers (check each one in the Console)

- Collected: **email address** and **name / profile picture** (Google sign-in), **app activity and diagnostics** (Firebase Analytics).
- Business data (bills, customers, items) is stored on the phone and synced to the signed-in user's own cloud storage (Firebase). Declare it as collected and linked to the user, for app functionality.
- Shared with third parties: none sold. Firebase / Google acts as the processor.
- Encrypted in transit: yes (Firebase).
- Users can ask for deletion: in the app (delete a business or data; sign out). Say how to request deletion of the cloud copy, and give a contact.
- Ads: none. Children: not directed at children.

## Things to settle before publishing

1. **Storage.** The manifest asks only for the old read/write storage permissions on Android 12 and older, so no "All files access" declaration is needed (checked against `AndroidManifest.xml`).
2. **`allowBackup="true"`.** Android may copy the app database to the user's Google Drive backup. Decide whether that is wanted; it is also how data can come back on a new phone.
3. **Only four approved Google accounts can sign in** (`AllowedAccounts`). A public listing means nobody else can use the app. Decide whether the listing is public, or closed testing with those accounts.
4. **Legal text.** The privacy policy and terms inside the app are drafts. Have them checked and add a contact address.
