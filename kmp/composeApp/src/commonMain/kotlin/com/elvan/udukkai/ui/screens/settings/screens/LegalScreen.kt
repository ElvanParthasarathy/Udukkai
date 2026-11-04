package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.navil.NavilMozhimaatri
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.LocalAppLanguage
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight

enum class LegalPage { PRIVACY, TERMS, LICENCES }

private data class Section(val heading: String, val body: String)

/** An open-source licence entry in the document flow with full notice and inline link. */
private data class LicenceEntry(
    val name: String,
    val licenceType: String,
    val fullText: String
)

private val apache2FullText = """
Apache License
Version 2.0, January 2004
http://www.apache.org/licenses/

TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

1. Definitions.
"License" shall mean the terms and conditions for use, reproduction, and distribution as defined by Sections 1 through 9 of this document.
"Licensor" shall mean the copyright owner or entity authorized by the copyright owner that is granting the License.
"Legal Entity" shall mean the union of the acting entity and all other entities that control, are controlled by, or are under common control with that entity.

2. Grant of Copyright License.
Subject to the terms and conditions of this License, each Contributor hereby grants to You a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable copyright license to reproduce, prepare Derivative Works of, publicly display, publicly perform, sublicense, and distribute the Work and such Derivative Works in Source or Object form.

3. Grant of Patent License.
Subject to the terms and conditions of this License, each Contributor hereby grants to You a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable patent license to make, have made, use, offer to sell, sell, import, and otherwise transfer the Work.

4. Redistribution.
You may reproduce and distribute copies of the Work or Derivative Works thereof in any medium, with or without modifications, and in Source or Object form, provided that You meet the following conditions:
(a) You must give any other recipients of the Work or Derivative Works a copy of this License; and
(b) You must cause any modified files to carry prominent notices stating that You changed the files; and
(c) You must retain, in the Source form of any Derivative Works that You distribute, all copyright, patent, trademark, and attribution notices from the Source form of the Work; and
(d) If the Work includes a "NOTICE" text file as part of its distribution, You must include a readable copy of the attribution notices contained within such NOTICE file.

5. Submission of Contributions.
Unless You explicitly state otherwise, any Contribution intentionally submitted for inclusion in the Work by You to the Licensor shall be under the terms and conditions of this License, without any additional terms or conditions.

6. Trademarks.
This License does not grant permission to use the trade names, trademarks, service marks, or product names of the Licensor, except as required for reasonable and customary use in describing the origin of the Work.

7. Disclaimer of Warranty.
Unless required by applicable law or agreed to in writing, Licensor provides the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied, including, without limitation, any warranties or conditions of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE.

8. Limitation of Liability.
In no event and under no legal theory, whether in tort (including negligence), contract, or otherwise, unless required by applicable law or agreed to in writing, shall any Contributor be liable to You for damages, including any direct, indirect, special, incidental, or consequential damages of any character arising as a result of this License or out of the use or inability to use the Work.

9. Accepting Warranty or Additional Liability.
While redistributing the Work or Derivative Works thereof, You may choose to offer, and charge a fee for, acceptance of support, warranty, indemnity, or other liability obligations and/or rights consistent with this License.
""".trimIndent()

private val licenceEntries = listOf(
    LicenceEntry(
        name = "Kotlin & Compose Multiplatform",
        licenceType = "Apache License 2.0",
        fullText = "Copyright 2010-2026 JetBrains s.r.o. and Kotlin Programming Language contributors.\nCopyright 2020-2026 JetBrains s.r.o. and Google LLC.\n\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:\nhttp://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License."
    ),
    LicenceEntry(
        name = "AndroidX (Activity, Core, Credentials, etc.)",
        licenceType = "Apache License 2.0",
        fullText = "Copyright (C) 2018-2026 The Android Open Source Project\n\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:\nhttp://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License."
    ),
    LicenceEntry(
        name = "Firebase & Google Play Services",
        licenceType = "Apache License 2.0 + Terms",
        fullText = "Copyright 2026 Google LLC\n\nLicensed under the Apache License, Version 2.0 (the \"License\"); you may not use this file except in compliance with the License. You may obtain a copy of the License at:\nhttp://www.apache.org/licenses/LICENSE-2.0\n\nSubject to Google APIs Terms of Service and Firebase Terms of Service:\nhttps://firebase.google.com/terms"
    ),
    LicenceEntry(
        name = "SQLite",
        licenceType = "Public Domain",
        fullText = "Public Domain Dedication\n\nAll of the code and documentation in SQLite has been dedicated to the public domain by the authors. All code authors, and representatives of the companies they work for, have signed affidavits dedicating their contributions to the public domain and originals of those signed affidavits are stored in a firesafe at the main offices of Hwaci.\n\nAnyone is free to copy, modify, publish, use, compile, sell, or distribute the original SQLite code, either in source code form or as a compiled binary, for any purpose, commercial or non-commercial, and by any means.\nhttps://www.sqlite.org/copyright.html"
    ),
    LicenceEntry(
        name = "React (Printed Bills Renderer)",
        licenceType = "MIT License",
        fullText = "MIT License\n\nCopyright (c) Meta Platforms, Inc. and affiliates.\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the \"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\nThe above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.\nhttps://github.com/facebook/react/blob/main/LICENSE"
    ),
    LicenceEntry(
        name = "Apache License 2.0 — Full Text",
        licenceType = "Official Specification",
        fullText = apache2FullText
    )
)

private fun sections(page: LegalPage, ta: Boolean): List<Section> = when (page) {
    LegalPage.PRIVACY -> if (ta) listOf(
        Section("உரிமையாளரும் நடைமுறைத் தொடக்கமும்", "இச்செயலி எல்வன் நவில் மென்பொருள் படைப்பகத்தால் உருவாக்கப்பட்டு நிர்வகிக்கப்படுகிறது. நடைமுறைத் தொடக்கம்: கார்த்திகை 2026."),
        Section("உங்கள் கைபேசியில் உள்ள தரவு", "நிறுவனத் தகவல், வாங்குநர்கள், பொருட்கள், பட்டியல்கள், பெறுகைகள், ஓவுரு, கையொப்பம் ஆகியவை உங்கள் கைபேசியில் செயலியின் தரவுத்தளத்தில் சேமிக்கப்படுகின்றன. பட்டியல் PDF கோப்புகளும் காப்புகளும் உங்கள் கைபேசியின் Documents கோப்புறையில் சேமிக்கப்படுகின்றன."),
        Section("கூகுள் கணக்கும் ஒத்திசைவும்", "அனுமதிக்கப்பட்ட கூகுள் கணக்கில் உள்நுழையும்போது, உங்கள் மின்னஞ்சல் முகவரியும் கணக்குப் படமும் உள்நுழைவுக்கும் செயலியில் காட்டுவதற்கும் பயன்படுத்தப்படும். உங்கள் தரவு, உங்கள் பிற சாதனங்களிலும் கிடைக்கும்படி, கூகுளின் Firebase Realtime Database-இல் ஒத்திசைக்கப்படுகிறது."),
        Section("பயன்பாட்டுப் புள்ளித்தரவுகள்", "செயலி எவ்வாறு பயன்படுத்தப்படுகிறது என அறிய Firebase Analytics பயன்படுத்தப்படுகிறது (சாதனத் தரவுகள், செயலி நிகழ்வுகள் போன்றவை). பட்டியல்களின் உள்ளடக்கம் இதற்கு அனுப்பப்படுவதில்லை."),
        Section("நாங்கள் செய்யாதவை", "விளம்பரங்கள் காட்டுவதில்லை. உங்கள் தரவை விற்பதில்லை."),
        Section("உங்கள் கட்டுப்பாடு", "செயலி பூட்டை இயக்கலாம்; வெளியேறலாம்; நிறுவனத்தையோ தரவையோ நீக்கலாம்; காப்புகளைச் சேமிக்கலாம் அல்லது மீட்கலாம். தரவை நீக்க, செயலியில் அதை நீக்கவும்; ஒத்திசைக்கப்பட்ட நகலும் நீக்கப்படும்."),
        Section("பயன்பாட்டு வரம்பு", "இச்செயலி வணிகப் பட்டியல் மற்றும் தொழிற்கணக்கு நிர்வாகத்திற்காக மட்டுமே உருவாக்கப்பட்டது; 13 வயதிற்குட்பட்ட சிறுவர்களுக்கானது அல்ல."),
        Section("தொடர்பும் வினவல்களும்", "தனியுரிமை தொடர்பான வினவல்கள் அல்லது கோரிக்கைகளுக்கு எங்களைத் தொடர்பு கொள்ள: elvanpartha@gmail.com")
    ) else listOf(
        Section("Ownership and Effective Date", "This application is developed, owned and operated by Elvan Navil. Effective date: November 2026."),
        Section("Data on your phone", "Your business details, customers, items, bills, receipts, logo and signature are stored in the app's database on your phone. Bill PDFs and backups are saved in the Documents folder of your phone."),
        Section("Google account and sync", "When you sign in with an approved Google account, your email address and account picture are used to sign you in and are shown in the app. Your data is synced to Google's Firebase Realtime Database so that it is available on your other devices."),
        Section("Usage statistics", "The app uses Firebase Analytics to understand how it is used (device details and app events). The content of your bills is not sent for this."),
        Section("What we do not do", "The app shows no advertisements and does not sell your data."),
        Section("Your control", "You can turn on the app lock, sign out, delete a business or data, and save or restore backups. To remove data, delete it in the app; the synced copy is deleted too."),
        Section("Intended Audience", "This application is intended strictly for commercial and business billing purposes and is not directed at children under the age of 13."),
        Section("Contact and Inquiries", "For privacy questions, data requests or concerns, contact us at: elvanpartha@gmail.com")
    )
    LegalPage.TERMS -> if (ta) listOf(
        Section("உடன்படிக்கையும் உரிமையும்", "எல்வன் நவில் மென்பொருள் படைப்பகத்தால் வழங்கப்படும் இச்செயலியைப் பயன்படுத்துவதற்கான விதிமுறைகள் இவை. நடைமுறைத் தொடக்கம்: கார்த்திகை 2026."),
        Section("பயன்பாடு", "இந்தச் செயலி பட்டியல்களையும் பெறுகைகளையும் உருவாக்கவும் நிர்வகிக்கவும் உதவுகிறது. இது உள்ளபடியே வழங்கப்படுகிறது; எந்த உத்தரவாதமும் இல்லை."),
        Section("உங்கள் பொறுப்பு", "பட்டியல்களில் உள்ள தரவுகள், வரி (GST) கணக்கீடுகள், சட்டப்படியான தேவைகள் ஆகியவற்றின் சரிபார்ப்பு உங்கள் பொறுப்பு. தேவையான இடங்களில் வரி ஆலோசகரிடம் உறுதிசெய்யவும்."),
        Section("காப்புகள்", "தரவை இழக்காமல் இருக்க, அடிக்கடி காப்பு எடுத்து பாதுகாப்பான இடத்தில் வையுங்கள். மீட்பு செய்வதற்கு முன் உள்ள நிலையின் நகல் வைக்கப்படும்."),
        Section("கணக்கு", "அனுமதிக்கப்பட்ட கணக்குகள் மட்டுமே செயலியைப் பயன்படுத்தலாம். உங்கள் கணக்கையும் செயலி பூட்டையும் பாதுகாக்க வேண்டியது உங்கள் பொறுப்பு."),
        Section("விதிமுறை மாற்றங்களும் தொடர்பும்", "சட்ட மற்றும் பயன்பாட்டுத் தேவைகளுக்கு ஏற்ப இவ்விதிமுறைகள் மாற்றப்படலாம். விதிமுறைகள் தொடர்பான வினவல்களுக்கு: elvanpartha@gmail.com")
    ) else listOf(
        Section("Agreement and Ownership", "These terms govern the use of this software, owned and provided by Elvan Navil. Effective date: November 2026."),
        Section("Use of the app", "This app helps you create and manage bills and receipts. It is provided as it is, without any warranty."),
        Section("Your responsibility", "You are responsible for checking the details on your bills, the GST calculations and the legal requirements that apply to you. Check with a tax adviser where needed."),
        Section("Backups", "Back up often and keep the backups in a safe place so that you do not lose data. A copy of your data is kept before every restore."),
        Section("Accounts", "Only approved accounts may use the app. You are responsible for keeping your account and your app lock safe."),
        Section("Modifications and Contact", "We reserve the right to update these terms to reflect changes in legal or operational requirements. For inquiries regarding these terms, contact us at: elvanpartha@gmail.com")
    )
    LegalPage.LICENCES -> emptyList()
}

/**
 * Text composable that automatically detects URLs in text and makes them clickable
 * inline links without doubling or repeating them.
 */
@Composable
private fun LinkableText(
    text: String,
    style: TextStyle,
    color: Color,
    linkColor: Color,
    onUrlClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val urlRegex = remember { Regex("https?://[a-zA-Z0-9./_~%#-]+|[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}") }
    val annotatedString = remember(text, color, linkColor) {
        buildAnnotatedString {
            var lastIndex = 0
            for (match in urlRegex.findAll(text)) {
                val start = match.range.first
                val end = match.range.last + 1
                if (start > lastIndex) {
                    append(text.substring(lastIndex, start))
                }
                val raw = match.value
                val destination = if (raw.contains("@") && !raw.startsWith("http") && !raw.startsWith("mailto:")) "mailto:$raw" else raw
                pushStringAnnotation(tag = "URL", annotation = destination)
                withStyle(
                    SpanStyle(
                        color = linkColor,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    )
                ) {
                    append(raw)
                }
                pop()
                lastIndex = end
            }
            if (lastIndex < text.length) {
                append(text.substring(lastIndex))
            }
        }
    }

    ClickableText(
        text = annotatedString,
        style = style.copy(color = color),
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    onUrlClick(annotation.item)
                }
        }
    )
}

/** Privacy policy, terms of use and open-source licences, in the app's language, inside the settings. */
@Composable
fun LegalScreen(
    page: LegalPage,
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors()
) {
    val ff = LocalAppFontFamily.current
    val uriHandler = LocalUriHandler.current

    val currentLang = LocalAppLanguage.current
    val isTa = currentLang.startsWith("ta")
    val isTanglish = currentLang == "ta-Latn"
    val rawSections = remember(page, isTa) { sections(page, ta = isTa) }
    val list = remember(rawSections, isTanglish) {
        if (isTanglish) {
            rawSections.map { sec ->
                sec.copy(
                    heading = NavilMozhimaatri.transliterate(sec.heading),
                    body = NavilMozhimaatri.transliterate(sec.body)
                )
            }
        } else {
            rawSections
        }
    }

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Dimens.ContentPadding, end = Dimens.ContentPadding, bottom = Dimens.SubpageContentPaddingBottom),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "shell_top_spacer") { Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current)) }

        if (page == LegalPage.LICENCES) {
            // ── Word-file/document text flow for licenses without any card ──
            licenceEntries.forEachIndexed { index, entry ->
                item(key = "licence_$index") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = entry.name,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = entry.licenceType,
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = colors.accent
                            )
                        }

                        LinkableText(
                            text = entry.fullText,
                            style = TextStyle(
                                fontFamily = ff,
                                fontSize = 13.5.sp,
                                lineHeight = 20.sp
                            ),
                            color = colors.textPrimary.copy(alpha = 0.75f),
                            linkColor = colors.accent,
                            onUrlClick = { uriHandler.openUri(it) }
                        )

                        if (index < licenceEntries.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(colors.textPrimary.copy(alpha = 0.08f))
                            )
                        }
                    }
                }
            }
        } else {
            list.forEachIndexed { i, section ->
                item(key = "legal_$i") {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            section.heading,
                            style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary
                        )
                        LinkableText(
                            text = section.body,
                            style = TextStyle(fontFamily = ff, fontSize = 14.sp, lineHeight = 21.sp),
                            color = colors.textPrimary.copy(alpha = 0.7f),
                            linkColor = colors.accent,
                            onUrlClick = { uriHandler.openUri(it) }
                        )
                    }
                }
            }
        }
    }
}
