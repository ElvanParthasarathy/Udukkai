package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight

enum class LegalPage { PRIVACY, TERMS, LICENCES }

private data class Section(val heading: String, val body: String)

private fun sections(page: LegalPage, ta: Boolean): List<Section> = when (page) {
    LegalPage.PRIVACY -> if (ta) listOf(
        Section("உங்கள் கைபேசியில் உள்ள தரவு", "நிறுவனத் தகவல், வாங்குநர்கள், பொருட்கள், பட்டியல்கள், பெறுகைகள், லோகோ, கையொப்பம் ஆகியவை உங்கள் கைபேசியில் செயலியின் தரவுத்தளத்தில் சேமிக்கப்படுகின்றன. பட்டியல் PDF கோப்புகளும் காப்புகளும் உங்கள் கைபேசியின் Documents கோப்புறையில் சேமிக்கப்படுகின்றன."),
        Section("கூகுள் கணக்கும் ஒத்திசைவும்", "அனுமதிக்கப்பட்ட கூகுள் கணக்கில் உள்நுழையும்போது, உங்கள் மின்னஞ்சல் முகவரியும் கணக்குப் படமும் உள்நுழைவுக்கும் செயலியில் காட்டுவதற்கும் பயன்படுத்தப்படும். உங்கள் தரவு, உங்கள் பிற சாதனங்களிலும் கிடைக்கும்படி, கூகுளின் Firebase Realtime Database-இல் ஒத்திசைக்கப்படுகிறது."),
        Section("பயன்பாட்டுப் புள்ளிவிவரம்", "செயலி எவ்வாறு பயன்படுத்தப்படுகிறது என அறிய Firebase Analytics பயன்படுத்தப்படுகிறது (சாதன விவரம், செயலி நிகழ்வுகள் போன்றவை). பட்டியல்களின் உள்ளடக்கம் இதற்கு அனுப்பப்படுவதில்லை."),
        Section("நாங்கள் செய்யாதவை", "விளம்பரங்கள் காட்டுவதில்லை. உங்கள் தரவை விற்பதில்லை."),
        Section("உங்கள் கட்டுப்பாடு", "செயலி பூட்டை இயக்கலாம்; வெளியேறலாம்; நிறுவனத்தையோ தரவையோ நீக்கலாம்; காப்புகளைச் சேமிக்கலாம் அல்லது மீட்கலாம். தரவை நீக்க, செயலியில் அதை நீக்கவும்; ஒத்திசைக்கப்பட்ட நகலும் நீக்கப்படும்.")
    ) else listOf(
        Section("Data on your phone", "Your business details, customers, items, bills, receipts, logo and signature are stored in the app's database on your phone. Bill PDFs and backups are saved in the Documents folder of your phone."),
        Section("Google account and sync", "When you sign in with an approved Google account, your email address and account picture are used to sign you in and are shown in the app. Your data is synced to Google's Firebase Realtime Database so that it is available on your other devices."),
        Section("Usage statistics", "The app uses Firebase Analytics to understand how it is used (device details and app events). The content of your bills is not sent for this."),
        Section("What we do not do", "The app shows no advertisements and does not sell your data."),
        Section("Your control", "You can turn on the app lock, sign out, delete a business or data, and save or restore backups. To remove data, delete it in the app; the synced copy is deleted too.")
    )
    LegalPage.TERMS -> if (ta) listOf(
        Section("பயன்பாடு", "இந்தச் செயலி பட்டியல்களையும் பெறுகைகளையும் உருவாக்கவும் நிர்வகிக்கவும் உதவுகிறது. இது உள்ளபடியே வழங்கப்படுகிறது; எந்த உத்தரவாதமும் இல்லை."),
        Section("உங்கள் பொறுப்பு", "பட்டியல்களில் உள்ள விவரங்கள், வரி (GST) கணக்கீடுகள், சட்டப்படியான தேவைகள் ஆகியவற்றின் சரிபார்ப்பு உங்கள் பொறுப்பு. தேவையான இடங்களில் வரி ஆலோசகரிடம் உறுதிசெய்யவும்."),
        Section("காப்புகள்", "தரவை இழக்காமல் இருக்க, அடிக்கடி காப்பு எடுத்து பாதுகாப்பான இடத்தில் வையுங்கள். மீட்பு செய்வதற்கு முன் உள்ள நிலையின் நகல் வைக்கப்படும்."),
        Section("கணக்கு", "அனுமதிக்கப்பட்ட கணக்குகள் மட்டுமே செயலியைப் பயன்படுத்தலாம். உங்கள் கணக்கையும் செயலி பூட்டையும் பாதுகாக்க வேண்டியது உங்கள் பொறுப்பு.")
    ) else listOf(
        Section("Use of the app", "This app helps you create and manage bills and receipts. It is provided as it is, without any warranty."),
        Section("Your responsibility", "You are responsible for checking the details on your bills, the GST calculations and the legal requirements that apply to you. Check with a tax adviser where needed."),
        Section("Backups", "Back up often and keep the backups in a safe place so that you do not lose data. A copy of your data is kept before every restore."),
        Section("Accounts", "Only approved accounts may use the app. You are responsible for keeping your account and your app lock safe.")
    )
    LegalPage.LICENCES -> listOf(
        Section("Kotlin and Compose Multiplatform", "Apache License 2.0"),
        Section("AndroidX libraries (Activity, Core, Credentials and others)", "Apache License 2.0"),
        Section("Firebase (Authentication, Realtime Database, Analytics) and Google Play services", "Apache License 2.0, and the Firebase and Google terms of service"),
        Section("SQLite", "Public domain"),
        Section("React (used to draw the printed bills)", "MIT License"),
        Section("Full text of the Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0")
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
    val list = sections(page, ta = LanguageManager.activeLanguageCode == "ta")
    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = Dimens.ContentPadding, end = Dimens.ContentPadding, bottom = Dimens.SubpageContentPaddingBottom),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item(key = "shell_top_spacer") { Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current)) }
        list.forEachIndexed { i, section ->
            item(key = "legal_$i") {
                Column(Modifier.fillMaxWidth().padding(horizontal = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(section.heading, style = TextStyle(fontFamily = ff, fontSize = 15.sp, fontWeight = FontWeight.SemiBold), color = colors.textPrimary)
                    Text(section.body, style = TextStyle(fontFamily = ff, fontSize = 14.sp, lineHeight = 21.sp), color = colors.textPrimary.copy(alpha = 0.7f))
                }
            }
        }
    }
}
