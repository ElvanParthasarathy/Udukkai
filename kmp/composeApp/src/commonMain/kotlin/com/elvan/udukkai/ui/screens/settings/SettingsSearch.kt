package com.elvan.udukkai.ui.screens.settings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.localization.trWithLang
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsRow
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay

/** One thing that can be found by the settings search: the page that holds it, its words in both languages, and loose keywords. */
class SettingsEntry(
    val route: SettingsRoute,
    val titleKey: String,
    val hintKey: String?,
    val groupKey: String,
    val icon: ImageVector,
    val keywords: List<String> = emptyList()
)

/** Everything the settings search knows. The identity page differs between the two apps, so it is passed in. */
fun settingsEntries(identity: SettingsRoute): List<SettingsEntry> = with(MaterialSymbols.Rounded) {
    listOf(
        SettingsEntry(SettingsRoute.Merchant, K.company, null, K.groupBusiness, BusinessCenter, listOf("profile", "switch company", "businesses", "manage")),
        SettingsEntry(identity, K.identifier, K.identityHint, K.groupBusiness, BusinessCenter, listOf("logo", "signature", "gstin", "gst", "pan", "owner", "name", "brand", "stamp", "seal")),
        SettingsEntry(SettingsRoute.Address, K.address, K.companyAddressDetails, K.groupBusiness, LocationOn, listOf("pincode", "pin code", "state", "district", "city", "street", "town")),
        SettingsEntry(SettingsRoute.Bank, K.bank, K.bankDetails, K.groupBusiness, CreditCard, listOf("ifsc", "account number", "upi", "branch", "qr")),
        SettingsEntry(SettingsRoute.InvoiceCreation, K.createBtn, K.pdfStylingOptions, K.groupBills, Description, listOf("invoice", "bill", "receipt", "print", "pdf", "header", "logo size", "logo place", "number", "prefix", "tax", "round off", "paper", "template", "style")),
        SettingsEntry(SettingsRoute.Display, K.display, K.themeLightDarkAuto, K.groupApp, LightMode, listOf("theme", "dark", "light", "font", "colour", "color", "appearance", "text size")),
        SettingsEntry(SettingsRoute.Language, K.appLanguage, K.tamilEnglishAuto, K.groupApp, Translate, listOf("tamil", "english", "translate", "language")),
        SettingsEntry(SettingsRoute.StorageBackup, K.storageAndBackup, K.manageDatabasesBackups, K.groupData, Folder, listOf("backup", "restore", "export", "import", "file", "database", "undo", "merge", "replace", "storage")),
        SettingsEntry(SettingsRoute.AppLock, K.appLock, K.appLockRowHint, K.groupData, Lock, listOf("pin", "fingerprint", "biometric", "password", "lock", "privacy", "forgot")),
        SettingsEntry(SettingsRoute.Security, K.security, K.syncLogoutClearData, K.groupData, Lock, listOf("sync", "sign out", "logout", "google", "account", "cloud", "clear data", "erase")),
        SettingsEntry(SettingsRoute.About, K.aboutAndLegal, K.aboutAndLegalHint, K.groupAbout, Info, listOf("about", "legal", "help")),
        SettingsEntry(SettingsRoute.AboutApp, K.aboutApp, null, K.groupAbout, Info, listOf("version", "about")),
        SettingsEntry(SettingsRoute.AboutDeveloper, K.softwareDesigner, null, K.groupAbout, Info, listOf("developer", "designer", "contact")),
        SettingsEntry(SettingsRoute.ElvanNavil, K.aboutElvanNavil, null, K.groupAbout, Translate, listOf("transliteration", "navil")),
        SettingsEntry(SettingsRoute.Privacy, K.privacyPolicy, K.privacyPolicyHint, K.groupAbout, Info, listOf("privacy", "data", "policy")),
        SettingsEntry(SettingsRoute.Terms, K.termsOfUse, K.termsOfUseHint, K.groupAbout, Info, listOf("terms", "conditions", "legal")),
        SettingsEntry(SettingsRoute.Licences, K.openSourceLicences, K.openSourceLicencesHint, K.groupAbout, Info, listOf("licence", "license", "open source", "libraries"))
    )
}

/** The entries that hold every word of [query] (in the title or hint in Tamil or English, or in the keywords), best matches first. */
fun searchSettings(entries: List<SettingsEntry>, query: String): List<SettingsEntry> {
    val words = query.trim().lowercase().split(' ').filter { it.isNotEmpty() }
    if (words.isEmpty()) return emptyList()
    fun titles(e: SettingsEntry) = listOf("ta", "en").map { e.titleKey.trWithLang(it).lowercase() }
    fun hay(e: SettingsEntry) = titles(e) + listOf("ta", "en").mapNotNull { l -> e.hintKey?.trWithLang(l)?.lowercase() } + e.keywords
    return entries
        .filter { e -> hay(e).let { h -> words.all { w -> h.any { it.contains(w) } } } }
        .sortedBy { e -> if (titles(e).any { t -> words.all { t.contains(it) } }) 0 else 1 }
}

/** The last searches, newest first, kept on the phone only. */
object RecentSettingSearches {
    private const val KEY = "settings_recent_searches"
    private const val MAX = 8

    fun all(): List<String> = getPreferencesHelper().getString(KEY).orEmpty().split('\n').filter { it.isNotBlank() }

    fun add(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return
        save(listOf(q) + all().filterNot { it.equals(q, ignoreCase = true) })
    }

    fun remove(query: String) = save(all().filterNot { it == query })

    fun clear() = save(emptyList())

    private fun save(list: List<String>) = getPreferencesHelper().setString(KEY, list.take(MAX).joinToString("\n"))
}

/** The search pill kept at the bottom of the settings page: it opens the search page when tapped. */
@Composable
fun SettingsSearchPill(colors: ShellColors, onClick: () -> Unit) {
    val ff = LocalAppFontFamily.current
    Box(Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.navigationBars).padding(horizontal = PILL_SIDE, vertical = PILL_GAP)) {
        Row(
            Modifier.fillMaxWidth().height(PILL_HEIGHT)
                .clip(CircleShape)
                .background(colors.surface)
                .border(BorderStroke(1.dp, colors.textPrimary.copy(alpha = 0.10f)), CircleShape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(MaterialSymbols.Rounded.Search, null, tint = colors.textPrimary.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Text(K.searchWord.tr(), style = TextStyle(fontFamily = ff, fontSize = 16.sp, fontWeight = FontWeight.Medium), color = colors.textPrimary.copy(alpha = 0.6f))
        }
    }
}

private val PILL_HEIGHT = 52.dp
private val PILL_SIDE = 20.dp
private val PILL_GAP = 12.dp

/**
 * The search page, opened the way Samsung's own apps do it: the pill itself grows until it fills the screen (its corners
 * straighten, the page fades in inside it), and on closing the page shrinks back down into the pill. Inside, the results
 * show as you type, the last searches show as removable chips when nothing is typed, and the field is a pill at the bottom,
 * above the keyboard.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsSearchOverlay(open: Boolean, entries: List<SettingsEntry>, colors: ShellColors, onOpen: (SettingsRoute) -> Unit, onClose: () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(open) {
        progress.animateTo(if (open) 1f else 0f, tween(if (open) 460 else 360, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)))
    }
    if (!open && progress.value == 0f) return
    val p = progress.value

    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val safeBottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
    val safeTop = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Where the pill sits: the container starts exactly there and grows to the whole screen
        val pillBottom = navInset + PILL_GAP
        val startTop = maxHeight - pillBottom - PILL_HEIGHT
        val corner = (PILL_HEIGHT / 2) * (1f - p)
        Box(
            Modifier.fillMaxSize()
                .padding(start = PILL_SIDE * (1f - p), end = PILL_SIDE * (1f - p), top = startTop * (1f - p), bottom = pillBottom * (1f - p))
                .clip(RoundedCornerShape(corner))
                .background(lerp(colors.surface, colors.background, p))
                // The page holds every touch while it is on screen
                .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } } }
        ) {
            SearchPageContent(p, entries, colors, open, safeTop, safeBottom, onOpen, onClose)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchPageContent(
    p: Float, entries: List<SettingsEntry>, colors: ShellColors, open: Boolean,
    safeTop: Dp, safeBottom: Dp, onOpen: (SettingsRoute) -> Unit, onClose: () -> Unit
) {
    val ff = LocalAppFontFamily.current
    var query by remember { mutableStateOf("") }
    var recents by remember { mutableStateOf(RecentSettingSearches.all()) }
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val results = remember(query, entries) { searchSettings(entries, query) }
    // After the page has grown, so the keyboard does not fight the animation; closing puts the keyboard away
    LaunchedEffect(open) {
        if (open) { delay(300); runCatching { focus.requestFocus() } } else focusManager.clearFocus()
    }

    fun openResult(route: SettingsRoute) {
        RecentSettingSearches.add(query)
        focusManager.clearFocus()
        onOpen(route)
    }

    Column(Modifier.fillMaxSize()) {
        // Header and body fade in as the page grows
        Column(Modifier.weight(1f).fillMaxWidth().graphicsLayer { alpha = ((p - 0.35f) / 0.65f).coerceIn(0f, 1f) }) {
            Row(Modifier.fillMaxWidth().padding(top = safeTop + 8.dp, start = 8.dp, end = 20.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
                    Icon(MaterialSymbols.Rounded.ArrowBack, null, tint = colors.textPrimary, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(4.dp))
                Text(K.searchWord.tr(), style = TextStyle(fontFamily = ff, fontSize = 22.sp, fontWeight = FontWeight.Bold), color = colors.textPrimary)
            }

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when {
                    query.isBlank() && recents.isNotEmpty() -> Column(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(24.dp)).background(colors.surface).padding(16.dp)
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(K.recentSearches.tr(), Modifier.weight(1f), style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.Medium), color = colors.textPrimary.copy(alpha = 0.6f))
                            Text(
                                K.clearAll.tr(),
                                style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                                color = colors.textPrimary,
                                modifier = Modifier.clip(CircleShape).clickable { RecentSettingSearches.clear(); recents = emptyList() }.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recents.forEach { word ->
                                Row(
                                    Modifier.clip(CircleShape).background(colors.iconBg).clickable { query = word }.padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(word, style = TextStyle(fontFamily = ff, fontSize = 15.sp), color = colors.textPrimary)
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        Modifier.size(28.dp).clip(CircleShape).clickable { RecentSettingSearches.remove(word); recents = RecentSettingSearches.all() },
                                        contentAlignment = Alignment.Center
                                    ) { Icon(MaterialSymbols.Rounded.Close, null, tint = colors.textPrimary.copy(alpha = 0.7f), modifier = Modifier.size(16.dp)) }
                                }
                            }
                        }
                    }

                    query.isBlank() -> Text(
                        K.searchSettingsHint.tr(),
                        style = TextStyle(fontFamily = ff, fontSize = 15.sp, textAlign = TextAlign.Center),
                        color = colors.textPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 40.dp)
                    )

                    results.isEmpty() -> Text(
                        K.noResults.tr(),
                        style = TextStyle(fontFamily = ff, fontSize = 16.sp, textAlign = TextAlign.Center),
                        color = colors.textPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.align(Alignment.Center).padding(horizontal = 40.dp)
                    )

                    else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                        item {
                            ElvanSettingsSection(colors = colors) {
                                results.forEachIndexed { i, e ->
                                    if (i > 0) ElvanSettingsDivider(colors = colors)
                                    ElvanSettingsRow(icon = e.icon, title = e.titleKey.tr(), description = e.groupKey.tr(), onClick = { openResult(e.route) }, colors = colors)
                                }
                            }
                        }
                    }
                }
            }
        }

        // The field: the pill itself. It sits where the pill was, and its margins and background arrive as the page grows
        Row(
            Modifier.fillMaxWidth()
                .padding(start = PILL_SIDE * p, end = PILL_SIDE * p, top = PILL_GAP * p, bottom = (safeBottom + PILL_GAP) * p)
                .height(PILL_HEIGHT)
                .clip(CircleShape)
                .background(colors.surface.copy(alpha = p))
                .border(BorderStroke(1.dp, colors.textPrimary.copy(alpha = 0.10f * p)), CircleShape)
                .padding(start = 18.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(MaterialSymbols.Rounded.Search, null, tint = colors.textPrimary.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) Text(K.searchWord.tr(), style = TextStyle(fontFamily = ff, fontSize = 16.sp, fontWeight = FontWeight.Medium), color = colors.textPrimary.copy(alpha = 0.6f))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = ff, fontSize = 16.sp, color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.textPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { results.firstOrNull()?.let { openResult(it.route) } }),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus)
                )
            }
            if (query.isNotEmpty()) {
                Box(Modifier.size(36.dp).clip(CircleShape).clickable { query = "" }, contentAlignment = Alignment.Center) {
                    Icon(MaterialSymbols.Rounded.Close, null, tint = colors.textPrimary.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
