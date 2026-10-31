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







### File Size Limits
- **Target**: 200–400 lines per file.
- **Acceptable**: Up to 600 lines for complex stateful editors.
- **Action required**: If a file exceeds 600 lines, extract sub-components into a `koorugal/` subfolder.
- **Extract candidates**: Build sections, row builders, modals, and data load/save logic (into a `_udhavi.dart` helper).

## Source Control Policy
1. **NO UNAPPROVED GIT CHECKOUTS**: Agents are STRICTLY FORBIDDEN from running `git checkout` or any command that reverts code to a previous state (e.g., `git reset --hard`) WITHOUT EXPLICIT PERMISSION from the user.
2. If you realize a mistake was made, you must either manually fix the mistake with code editing tools or ask the user for permission to run a git checkout/reset command to restore the previous state.

3. Always push to git whenever you have completed a major change or feature.
