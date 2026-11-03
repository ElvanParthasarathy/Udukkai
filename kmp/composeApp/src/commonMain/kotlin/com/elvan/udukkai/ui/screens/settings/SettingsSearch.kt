package com.elvan.udukkai.ui.screens.settings

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.core.platform.getImeBottomPadding
import com.elvan.udukkai.core.platform.getPreferencesHelper
import com.elvan.udukkai.core.platform.navigationBarsPaddingIfMobile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.localization.trWithLang
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSectionContainer
import com.elvan.udukkai.ui.components.shell.ElvanSettingsDivider
import com.elvan.udukkai.ui.components.shell.ElvanSettingsRow
import com.elvan.udukkai.ui.components.shell.ElvanSettingsSection
import com.elvan.udukkai.ui.components.shell.ElvanSubShell
import com.elvan.udukkai.ui.components.shell.LocalElvanTopSpacerHeight
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

/** The search pill: comfortable, substantial floating pill (56dp high, 248dp wide), matching the navbar style. */
private val PILL_HEIGHT = 56.dp
private val PILL_FIELD_HEIGHT = 56.dp

/** The closed pill's width, and the room left at each side when it is open. */
private val PILL_CLOSED_WIDTH = 248.dp
private val PILL_OPEN_MARGIN = 16.dp

@Composable
private fun Modifier.floatingPill(colors: ShellColors, height: Dp = PILL_HEIGHT): Modifier = this
    .height(height)
    .cssShadow(color = Color.Black, alpha = if (colors.isDark) 0.55f else 0.20f, blurRadius = 32.dp, offsetY = 12.dp)
    .background(color = colors.floatingBg.copy(alpha = 0.88f), shape = CircleShape)
    .border(width = 0.5.dp, color = colors.floatingBorder.copy(alpha = if (colors.isDark) 0.15f else 0.6f), shape = CircleShape)

/** The search pill kept at the bottom of the settings page: comfortable, elegant floating search pill. */
@Composable
fun SettingsSearchPill(colors: ShellColors, onClick: () -> Unit) {
    val ff = LocalAppFontFamily.current
    BoxWithConstraints(Modifier.fillMaxWidth().navigationBarsPaddingIfMobile().padding(bottom = Dimens.FloatingBarBottomGap), contentAlignment = Alignment.Center) {
        Row(
            Modifier.width(PILL_CLOSED_WIDTH.coerceAtMost(maxWidth - PILL_OPEN_MARGIN * 2))
                .floatingPill(colors, height = PILL_HEIGHT)
                .clip(CircleShape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClick)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(MaterialSymbols.Rounded.Search, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                K.searchWord.tr(),
                style = TextStyle(fontFamily = ff, fontSize = 16.sp, color = colors.textSecondary.copy(alpha = 0.6f)),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

/**
 * The search page, opened the way Samsung's own apps do it: the pill at the bottom widens into the search field
 * while the settings page fades away and the search page rises into its place. Closing runs the same in reverse. The
 * page has the app's usual heading, large while open and small once the list is scrolled, with the back button.
 * Inside, the results show as you type, the last searches show as removable chips when nothing is typed, and the field
 * stays at the bottom, above the keyboard.
 */
@Composable
fun SettingsSearchOverlay(open: Boolean, entries: List<SettingsEntry>, colors: ShellColors, onOpen: (SettingsRoute) -> Unit, onClose: () -> Unit) {
    val progress = remember { Animatable(0f) }
    var query by remember { mutableStateOf("") }
    var recents by remember { mutableStateOf(RecentSettingSearches.all()) }
    val focusManager = LocalFocusManager.current
    val results = remember(query, entries) { searchSettings(entries, query) }
    // Each time the page opens it starts clean
    LaunchedEffect(open) { if (open) { query = ""; recents = RecentSettingSearches.all() } }
    fun openResult(route: SettingsRoute) {
        RecentSettingSearches.add(query)
        focusManager.clearFocus()
        onOpen(route)
    }
    LaunchedEffect(open) {
        progress.animateTo(if (open) 1f else 0f, tween(if (open) 320 else 260, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)))
    }
    if (!open && progress.value == 0f) return
    val p = progress.value
    val imeBottom = getImeBottomPadding()
    // One continuous value from the keyboard down to the navigation bar, so nothing jumps when the keyboard closes
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    BoxWithConstraints(
        Modifier.fillMaxSize()
            // The settings page underneath fades away as this page fades in
            .background(colors.background.copy(alpha = p))
            // The page holds every touch while it is on screen
            .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } } }
    ) {
        val rise = with(LocalDensity.current) { (28.dp * (1f - p)).toPx() }
        val closedWidth = PILL_CLOSED_WIDTH.coerceAtMost(maxWidth - PILL_OPEN_MARGIN * 2)
        val openWidth = maxWidth - PILL_OPEN_MARGIN * 2
        Box(Modifier.fillMaxSize().graphicsLayer { alpha = p; translationY = rise }) {
            SearchPage(query, { query = it }, recents, { recents = it }, results, colors, ::openResult, onClose)
        }
        // The field is the pill itself: it starts at the pill's place and size and widens to the full field
        SearchField(
            colors = colors, open = open, query = query, onQuery = { query = it },
            onSearch = { results.firstOrNull()?.let { openResult(it.route) } },
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = maxOf(imeBottom, navInset) + Dimens.FloatingBarBottomGap)
                .width(closedWidth + (openWidth - closedWidth) * p)
        )
    }
}

@Composable
private fun SearchField(colors: ShellColors, open: Boolean, query: String, onQuery: (String) -> Unit, onSearch: () -> Unit, modifier: Modifier) {
    val ff = LocalAppFontFamily.current
    val focus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    // After the page has opened, so the keyboard does not fight the animation; closing puts the keyboard away
    LaunchedEffect(open) {
        if (open) { delay(260); runCatching { focus.requestFocus() } } else focusManager.clearFocus()
    }
    Row(modifier.floatingPill(colors, height = PILL_FIELD_HEIGHT).padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(MaterialSymbols.Rounded.Search, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) Text(K.searchWord.tr(), style = TextStyle(fontFamily = ff, fontSize = 16.sp, color = colors.textSecondary.copy(alpha = 0.6f)))
            BasicTextField(
                value = query,
                onValueChange = onQuery,
                singleLine = true,
                textStyle = TextStyle(fontFamily = ff, fontSize = 16.sp, color = colors.textPrimary),
                cursorBrush = SolidColor(colors.textPrimary),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                modifier = Modifier.fillMaxWidth().focusRequester(focus)
            )
        }
        if (query.isNotEmpty()) {
            Box(Modifier.size(36.dp).clip(CircleShape).clickable { onQuery("") }, contentAlignment = Alignment.Center) {
                Icon(MaterialSymbols.Rounded.Close, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** The page itself, in the app's usual shell: a large heading that shrinks into the bar as the list is scrolled. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchPage(
    query: String, onQuery: (String) -> Unit, recents: List<String>, onRecents: (List<String>) -> Unit,
    results: List<SettingsEntry>, colors: ShellColors, openResult: (SettingsRoute) -> Unit, onClose: () -> Unit
) {
    val ff = LocalAppFontFamily.current
    val scrollState = rememberLazyListState()
    ElvanSubShell(title = K.searchWord.tr(), onBack = onClose, scrollState = scrollState) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "shell_top_spacer") { Spacer(Modifier.height(LocalElvanTopSpacerHeight.current + 6.dp)) }

            when {
                query.isBlank() && recents.isNotEmpty() -> item(key = "recents") {
                    ElvanSectionContainer {
                        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(colors.surface).padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(K.recentSearches.tr(), Modifier.weight(1f), style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.Medium), color = colors.textPrimary.copy(alpha = 0.6f))
                                Text(
                                    K.clearAll.tr(),
                                    style = TextStyle(fontFamily = ff, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                                    color = colors.textPrimary,
                                    modifier = Modifier.clip(CircleShape).clickable { RecentSettingSearches.clear(); onRecents(emptyList()) }.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                recents.forEach { word ->
                                    Row(
                                        Modifier.clip(CircleShape).background(colors.iconBg).clickable { onQuery(word) }.padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(word, style = TextStyle(fontFamily = ff, fontSize = 15.sp), color = colors.textPrimary)
                                        Spacer(Modifier.width(6.dp))
                                        Box(
                                            Modifier.size(28.dp).clip(CircleShape).clickable { RecentSettingSearches.remove(word); onRecents(RecentSettingSearches.all()) },
                                            contentAlignment = Alignment.Center
                                        ) { Icon(MaterialSymbols.Rounded.Close, null, tint = colors.textPrimary.copy(alpha = 0.7f), modifier = Modifier.size(16.dp)) }
                                    }
                                }
                            }
                        }
                    }
                }

                query.isBlank() || results.isEmpty() -> item(key = "message") {
                    Text(
                        if (query.isBlank()) K.searchSettingsHint.tr() else K.noResults.tr(),
                        style = TextStyle(fontFamily = ff, fontSize = 15.sp, textAlign = TextAlign.Center),
                        color = colors.textPrimary.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 40.dp, vertical = 24.dp)
                    )
                }

                else -> item(key = "results") {
                    ElvanSectionContainer {
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
}
