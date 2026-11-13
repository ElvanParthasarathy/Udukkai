# மாற்றப் பதிவேடு · Changelog

அனைத்து குறிப்பிடத்தக்க மாற்றங்களும் இந்த ஆவணத்தில் ஆவணப்படுத்தப்படும்.
All notable changes to this project will be documented in this file.

## Unreleased

## 1.0.10

### New
- Sync pauses and asks before it would overwrite many records, on the phone or in the cloud.
- Going back after a restore from a file is offered for 24 hours only.

### Fixed
- Sync no longer deletes cloud records the phone does not have. A phone with old or empty data could erase newer bills.
- An empty database can no longer replace a backup that holds data, and the older backup copy is kept for at least an hour.
- Factory items use the Warp Silk and Weft Silk English names.

## 1.0.8

### New
- The update window is a compact bottom sheet, the same size as the item picker, with only the release notes scrolling inside it.

## 1.0.7

### New
- Customer and item pages have a Details tab and a Transactions tab: every invoice, receipt and proforma, with filter and sort.
- Delete a customer, item, invoice or receipt from its own page, in the three-dot menu.
- A proforma page now says "Proforma", with its own symbol and number label.
- Updates install quietly in the background when you leave the app on Wi-Fi, once the app has updated itself once.
- Country and district in Settings are picked from a bottom sheet; choosing Custom lets you type both.

### Fixed
- Update: each app now downloads only its own APK. The Textiles app was fetching the Factory file, which Play Protect scanned and Android then refused, on every Update tap.
- Back with the keyboard open now only closes the keyboard, in every editor.
- Top-bar button ripples follow the shape, and the Save button's ripple fills its pill.
- A smaller logo on the welcome page, and clearer Tamil wording for "All" and "All companies".

## 1.0.0

### New
- விசைத்தறி மற்றும் கைத்தறி ஆலை நெசவுக் கூலி மேலாண்மை முறைமை (Udukkai Factory).
- பட்டு மற்றும் சவுளி வணிக விற்பனைப் பட்டியல் மற்றும் GST முறைமை (Udukkai Textiles).
- இருமொழித் தமிழ் இடைமுகம் (Pure Senthamizh & English).
- கைரேகை மற்றும் முக அடையாளப் பூட்டுப் பாதுகாப்பு (Biometric Screen Lock).
- கூகிள் மேகக் கணினி ஒத்திசைவு (Cloud Sync & Backup).
