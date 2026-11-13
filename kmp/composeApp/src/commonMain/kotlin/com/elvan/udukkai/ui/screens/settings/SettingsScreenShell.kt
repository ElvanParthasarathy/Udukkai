package com.elvan.udukkai.ui.screens.settings

import com.elvan.udukkai.data.settings.BusinessProfile
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.Transitions
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import com.elvan.udukkai.ui.screens.settings.SettingsNavState
import com.elvan.udukkai.ui.screens.settings.SettingsSearchOverlay
import com.elvan.udukkai.ui.screens.settings.SettingsSearchPill
import com.elvan.udukkai.ui.screens.settings.SettingsSearchIcon
import com.elvan.udukkai.ui.screens.settings.settingsEntries
import com.elvan.udukkai.ui.screens.settings.screens.*


import androidx.compose.ui.draw.clipToBounds
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.core.platform.SettingsPaneLayout
import com.elvan.udukkai.core.platform.settingsPaneLayout

/** Business-specific data and pages plug into a single Settings shell. */
data class SettingsBusiness(
    val profileStore: com.elvan.udukkai.data.settings.BusinessProfileStore,
    val identityRoute: SettingsRoute,
    val appName: String,
    val appTitle: String,
    val isFactory: Boolean,
    val addressFieldOrder: AddressFieldOrder
) {
    val profile: BusinessProfile get() = profileStore.profile
    fun onUpdateProfile(profile: BusinessProfile) = profileStore.updateProfile(profile)
}

@Composable
fun SettingsScreenShell(
    business: SettingsBusiness,
    initialRoute: SettingsRoute = SettingsRoute.Hub,
    onNavigateOverride: ((SettingsRoute) -> Boolean)? = null,
    onBack: () -> Unit = {},
    businessPage: @Composable (SettingsRoute, LazyListState, () -> Unit, (SettingsRoute) -> Unit, ShellColors) -> Unit
) {
    var searchOpen by remember { mutableStateOf(false) }
    var landscapeQuery by remember { mutableStateOf("") }
    var voiceQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    val colors = rememberShellColors()
    val focus = androidx.compose.ui.platform.LocalFocusManager.current
    val hubScrollState = rememberLazyListState()
    val subpageScrollStates = remember { mutableStateMapOf<SettingsRoute, LazyListState>() }
    val pageNavigation = remember(initialRoute) { SettingsNavigator(initialRoute) }
    val detailNavigation = remember(initialRoute) {
        SettingsNavigator(if (initialRoute == SettingsRoute.Hub) SettingsRoute.Display else initialRoute)
    }
    val entries = remember(business.identityRoute) { settingsEntries(business.identityRoute) }

    BoxWithConstraints(Modifier.fillMaxSize().background(colors.background)) {
        val paneLayout = settingsPaneLayout(maxWidth, maxHeight, pageNavigation.state.route.opensStandalonePage)
        val twoPane = paneLayout != SettingsPaneLayout.SinglePane
        val navigation = if (twoPane) detailNavigation else pageNavigation
        val chromeHandled = LocalElvanLandscapeChromeHandled.current
        val sideChrome = paneLayout == SettingsPaneLayout.AndroidLandscape && !chromeHandled
        fun navigateTo(route: SettingsRoute) {
            if (route.opensStandalonePage && com.elvan.udukkai.core.platform.platformLayoutPolicy.supportsLandscapeControls) {
                focus.clearFocus()
                if (onNavigateOverride?.invoke(route) != true) pageNavigation.push(route)
                return
            }
            if (!twoPane && onNavigateOverride?.invoke(route) == true) return
            navigation.push(route)
        }
        val handleBack: () -> Unit = {
            if (searchOpen) { searchOpen = false; focus.clearFocus() }
            else if (!(if (twoPane) navigation.popOrReturnTo(SettingsRoute.Display) else navigation.pop())) onBack()
        }
        fun selectRoot(route: SettingsRoute) {
            searchOpen = false
            focus.clearFocus()
            if (route.opensStandalonePage && com.elvan.udukkai.core.platform.platformLayoutPolicy.supportsLandscapeControls) {
                navigateTo(route)
                return
            }
            detailNavigation.selectRoot(route)
        }
        AppBackHandler(enabled = searchOpen || navigation.canGoBack || (twoPane && navigation.state.route != SettingsRoute.Display)) { handleBack() }

        @Composable
        fun RoutePage(route: SettingsRoute, handleBack: () -> Unit, navigateTo: (SettingsRoute) -> Unit, twoPane: Boolean, showBack: Boolean) {
            val scrollState = if (route == SettingsRoute.Hub) hubScrollState else subpageScrollStates.getOrPut(route) { LazyListState() }
            val pageTitle = when (route) {
                SettingsRoute.Hub -> K.settings.tr()
                SettingsRoute.Display -> K.appearance.tr()
                SettingsRoute.Language -> K.appLanguage.tr()
                SettingsRoute.Merchant -> K.company.tr()
                SettingsRoute.ManageProfiles -> K.manage.tr()
                SettingsRoute.FactoryIdentity -> K.identifier.tr()
                SettingsRoute.TextilesIdentity -> K.identifier.tr()
                SettingsRoute.Address -> K.address.tr()
                SettingsRoute.Bank -> K.bank.tr()
                SettingsRoute.InvoiceCreation -> K.createBtn.tr()
                SettingsRoute.BilingualRules -> K.bilingualSettings.tr()
                SettingsRoute.LogoSettings -> K.logoSettings.tr()
                SettingsRoute.Account -> K.account.tr()
                SettingsRoute.EditName -> K.editName.tr()
                SettingsRoute.LinkedAccounts -> K.linkedAccounts.tr()
                SettingsRoute.ChangePassword -> K.changePassword.tr()
                SettingsRoute.CreatePassword -> K.createPassword.tr()
                SettingsRoute.DeleteAccount -> K.deleteAccount.tr()
                SettingsRoute.StorageBackup -> K.storageAndBackup.tr()
                SettingsRoute.Security -> K.accountAndSecurity.tr()
                SettingsRoute.AboutDeveloper -> K.softwareDesigner.tr()
                SettingsRoute.AboutApp -> K.aboutApp.tr()
                SettingsRoute.ElvanNavil -> K.aboutElvanNavil.tr()
                SettingsRoute.Privacy -> K.privacyPolicy.tr()
                SettingsRoute.Terms -> K.termsOfUse.tr()
                SettingsRoute.Licences -> K.openSourceLicences.tr()
                SettingsRoute.About -> K.groupAbout.tr()
            }

            val isSelfShelled = route in setOf(
                SettingsRoute.Account,
                SettingsRoute.Security,
                SettingsRoute.EditName,
                SettingsRoute.LinkedAccounts,
                SettingsRoute.ChangePassword,
                SettingsRoute.CreatePassword,
                SettingsRoute.DeleteAccount
            )

            if (isSelfShelled) {
                when (route) {
                    SettingsRoute.Account, SettingsRoute.Security -> AccountSettingsScreen(
                        onBack = handleBack,
                        onNavigate = { navigateTo(it) },
                        scrollState = scrollState,
                        colors = colors
                    )
                    SettingsRoute.EditName -> EditNameScreen(
                        onBack = handleBack,
                        scrollState = scrollState,
                        colors = colors
                    )
                    SettingsRoute.LinkedAccounts -> LinkedAccountsScreen(
                        onBack = handleBack,
                        onNavigate = { navigateTo(it) },
                        scrollState = scrollState,
                        colors = colors
                    )
                    SettingsRoute.ChangePassword -> ChangePasswordScreen(
                        onBack = handleBack,
                        scrollState = scrollState,
                        colors = colors
                    )
                    SettingsRoute.CreatePassword -> CreatePasswordScreen(
                        onBack = handleBack,
                        scrollState = scrollState,
                        colors = colors
                    )
                    SettingsRoute.DeleteAccount -> DeleteAccountScreen(
                        onBack = handleBack,
                        scrollState = scrollState,
                        colors = colors
                    )
                    else -> {}
                }
            } else {
                ElvanSubShell(
                    title = pageTitle,
                    onBack = if (showBack) handleBack else null,
                    collapsedOnly = twoPane,
                    hideTitleSideways = twoPane && route == SettingsRoute.Hub,
                    scrollState = scrollState,
                    hasFloatingBar = route == SettingsRoute.Hub,
                    floatingBar = { if (route == SettingsRoute.Hub) { if (twoPane) { if (!sideChrome) SettingsSearchIcon(colors, hidden = searchOpen || searchActive) { voiceQuery = ""; searchOpen = true } } else SettingsSearchPill(colors, onVoice = { voiceQuery = it; searchOpen = true }, hidden = false) { voiceQuery = ""; searchOpen = true } } }
                ) {
                    when (route) {
                        SettingsRoute.Hub -> SettingsHubContent(
                            business = business,
                            onNavigate = { navigateTo(it) },
                            scrollState = scrollState
                        )
                        SettingsRoute.Display -> DisplaySettingsScreen(scrollState = scrollState, colors = colors)
                        SettingsRoute.Language -> LanguageSettingsScreen(scrollState = scrollState, colors = colors)
                        SettingsRoute.Merchant -> CompanySettingsScreen(
                            store = business.profileStore,
                            onNavigateToManageProfiles = { navigateTo(SettingsRoute.ManageProfiles) },
                            scrollState = scrollState, colors = colors
                        )
                        SettingsRoute.ManageProfiles -> ManageProfilesSettingsScreen(
                            store = business.profileStore, onBack = handleBack,
                            scrollState = scrollState, colors = colors
                        )
                        SettingsRoute.FactoryIdentity, SettingsRoute.TextilesIdentity -> IdentitySettingsScreen(
                            store = business.profileStore, scrollState = scrollState, colors = colors
                        )
                        SettingsRoute.Address -> AddressSettingsScreen(
                            store = business.profileStore, fieldOrder = business.addressFieldOrder,
                            scrollState = scrollState, colors = colors
                        )
                        SettingsRoute.Bank -> BankSettingsScreen(
                            scrollState = scrollState,
                            colors = colors,
                            profile = business.profile,
                            onUpdateProfile = { business.onUpdateProfile(it) }
                        )
                        SettingsRoute.InvoiceCreation -> InvoiceCreationSettingsScreen(
                            store = business.profileStore, showGstSplit = !business.isFactory,
                            onNavigate = navigateTo, scrollState = scrollState, colors = colors
                        )
                        SettingsRoute.BilingualRules -> BilingualRulesScreen(
                            profile = business.profile,
                            colors = colors,
                            scrollState = scrollState,
                            onSave = { business.onUpdateProfile(it) }
                        )
                        SettingsRoute.LogoSettings -> LogoSettingsScreen(
                            profile = business.profile,
                            isFactory = business.isFactory,
                            colors = colors,
                            scrollState = scrollState,
                            onSave = { business.onUpdateProfile(it) }
                        )
                        SettingsRoute.StorageBackup -> businessPage(route, scrollState, handleBack, navigateTo, colors)
                        SettingsRoute.AboutDeveloper -> AboutDeveloperScreen(scrollState = scrollState, colors = colors)
                        SettingsRoute.AboutApp -> AboutAppScreen(scrollState = scrollState, colors = colors)
                        SettingsRoute.ElvanNavil -> TransliteratorSettingsScreen(scrollState = scrollState, colors = colors)
                        SettingsRoute.Privacy -> LegalScreen(LegalPage.PRIVACY, scrollState, colors)
                        SettingsRoute.Terms -> LegalScreen(LegalPage.TERMS, scrollState, colors)
                        SettingsRoute.Licences -> LegalScreen(LegalPage.LICENCES, scrollState, colors)
                        SettingsRoute.About -> AboutHubScreen(versionText = "${business.appName} v${com.elvan.udukkai.core.platform.appVersionName()}", onNavigate = { navigateTo(it) }, scrollState = scrollState, colors = colors)
                        else -> {}
                    }
                }
            }
        }


        when (paneLayout) {
            SettingsPaneLayout.AndroidLandscape -> CompositionLocalProvider(LocalElvanPaneChrome provides true) {
                Row(
                    Modifier.fillMaxSize().then(
                        if (sideChrome) Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)).padding(end = 80.dp)
                        else Modifier
                    )
                ) {
                    Box(Modifier.weight(0.4f).fillMaxHeight()) {
                        SettingsHubContent(
                            business = business,
                            onNavigate = ::selectRoot,
                            scrollState = hubScrollState,
                            searchQuery = landscapeQuery,
                            onSearchQuery = { landscapeQuery = it; searchOpen = true },
                            onSearchFocus = { searchOpen = true }
                        )
                    }
                    Box(Modifier.weight(0.6f).fillMaxHeight().clipToBounds()) {
                        if (searchOpen) {
                            SettingsPaneSearchResults(
                                query = landscapeQuery, entries = entries, colors = colors,
                                onOpen = { route -> RecentSettingSearches.add(landscapeQuery); selectRoot(route) }
                            )
                        } else CompositionLocalProvider(
                            LocalElvanPaneNoBack provides true,
                            LocalLandscapeSettingsHeader provides true
                        ) {
                            AnimatedContent(
                                targetState = detailNavigation.state,
                                modifier = Modifier.fillMaxSize(),
                                transitionSpec = {
                                    Transitions.settingsPaneRise(targetState.index >= initialState.index)
                                },
                                label = "SettingsDetailTransition"
                            ) { state ->
                                RoutePage(state.route, handleBack, ::navigateTo, true, false)
                            }
                        }
                    }
                }
                if (sideChrome) com.elvan.udukkai.ui.navigation.LandscapeTopActionButton(
                    icon = MaterialSymbols.Rounded.ArrowBack,
                    contentDescription = K.back.tr(), onClick = handleBack, colors = colors,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
            SettingsPaneLayout.Desktop -> CompositionLocalProvider(LocalElvanPaneChrome provides true) {
                Row(Modifier.fillMaxSize()) {
                    Box(Modifier.weight(0.4f).fillMaxHeight()) {
                        RoutePage(SettingsRoute.Hub, onBack, ::selectRoot, true, !chromeHandled)
                    }
                    Box(Modifier.weight(0.6f).fillMaxHeight()) {
                        RoutePage(detailNavigation.state.route, handleBack, ::navigateTo, true, detailNavigation.canGoBack)
                    }
                }
            }
            SettingsPaneLayout.SinglePane -> AnimatedContent(
                targetState = pageNavigation.state,
                modifier = Modifier.fillMaxSize().background(colors.background),
                transitionSpec = {
                    with(Transitions) {
                        when {
                            targetState.route == SettingsRoute.ManageProfiles -> smoothVertical(true)
                            initialState.route == SettingsRoute.ManageProfiles -> smoothVertical(false)
                            else -> smoothHorizontal(targetState.index >= initialState.index)
                        }
                    }
                },
                label = "SettingsPageTransition"
            ) { state ->
                RoutePage(state.route, handleBack, ::navigateTo, false, true)
            }
        }
        SettingsSearchOverlay(
            open = searchOpen && paneLayout != SettingsPaneLayout.AndroidLandscape,
            entries = entries, colors = colors,
            onOpen = { route ->
                if (twoPane) selectRoot(route) else navigateTo(route)
                searchOpen = false
            },
            onClose = { searchOpen = false },
            initialQuery = voiceQuery,
            onActive = { searchActive = it }
        )
    }
}

@Composable
private fun SettingsHubContent(
    business: SettingsBusiness,
    onNavigate: (SettingsRoute) -> Unit,
    scrollState: LazyListState,
    searchQuery: String = "",
    onSearchQuery: (String) -> Unit = {},
    onSearchFocus: () -> Unit = {}
) {
    val colors = rememberShellColors()
    val landscape = com.elvan.udukkai.ui.components.shell.LocalLandscapeContent.current
    val accountEmail = AuthManager.signedInEmail.orEmpty()
    val accountName = AuthManager.signedInDisplayName
        ?.takeIf { it.isNotBlank() }
        ?: accountEmail.substringBefore("@").takeIf { it.isNotBlank() }
        ?: K.userAccount.tr()

    com.elvan.udukkai.ui.components.shell.ElvanLazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        landscapeRoot = landscape,
        contentPadding = PaddingValues(
            top = 0.dp,
            bottom = Dimens.SubpageContentPaddingBottom + (if (com.elvan.udukkai.ui.components.shell.LocalElvanPaneChrome.current) 0.dp else 72.dp)
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        if (landscape) {
            item(key = "settings_search") {
                com.elvan.udukkai.ui.screens.settings.SettingsPaneSearchField(
                    query = searchQuery,
                    onQuery = onSearchQuery,
                    onFocus = onSearchFocus,
                    colors = colors
                )
            }
        } else {
            item(key = "shell_top_spacer") {
                Spacer(Modifier.height(LocalElvanTopSpacerHeight.current + 6.dp))
            }
        }

        // Identity comes first. Compact layouts use the app's familiar profile pill;
        // landscape keeps the denser card-row language used by the two-pane settings list.
        item(key = "account_identity") {
            ElvanSectionContainer {
                if (landscape) {
                    ElvanSettingsSection(colors = colors) {
                        ElvanSettingsRow(
                            iconWidget = {
                                AccountAvatar(fallback = {
                                    Icon(
                                        imageVector = MaterialSymbols.Rounded.Person,
                                        contentDescription = null,
                                        tint = colors.textPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                })
                            },
                            title = accountName,
                            description = accountEmail.ifEmpty { K.signInMethods.tr() },
                            onClick = { onNavigate(SettingsRoute.Account) },
                            colors = colors
                        )
                    }
                } else {
                    ElvanProfilePillCard(
                        title = accountName,
                        subtitle = accountEmail.ifEmpty { K.signInMethods.tr() },
                        onClick = { onNavigate(SettingsRoute.Account) },
                        iconWidget = {
                            AccountAvatar(fallback = {
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.Person,
                                    contentDescription = null,
                                    tint = colors.textPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            })
                        },
                        trailing = {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = colors.textTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = colors
                    )
                }
            }
        }

        // App appearance and language stay together as the first preference group.
        item(key = "app_looks_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.LightMode,
                        title = K.display.tr(),
                        description = K.themeLightDarkAuto.tr(),
                        onClick = { onNavigate(SettingsRoute.Display) },
                        colors = colors
                    )

                    ElvanSettingsDivider(colors = colors)

                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Translate,
                        title = K.appLanguage.tr(),
                        description = K.tamilEnglishAuto.tr(),
                        onClick = { onNavigate(SettingsRoute.Language) },
                        colors = colors
                    )
                }
            }
        }

        // Data belongs below personal preferences; account details now live in the identity card above.
        item(key = "data_security_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Folder,
                        title = K.storageAndBackup.tr(),
                        description = K.manageDatabasesBackups.tr(),
                        onClick = { onNavigate(SettingsRoute.StorageBackup) },
                        colors = colors
                    )
                }
            }
        }

        // Section 5: About, listed here directly instead of behind one more page
        item(key = "system_about_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsRow(icon = MaterialSymbols.Rounded.Info, title = K.aboutApp.tr(), description = "${business.appName} v${com.elvan.udukkai.core.platform.appVersionName()}", onClick = { onNavigate(SettingsRoute.AboutApp) }, colors = colors)
                    ElvanSettingsDivider(colors = colors)
                    ElvanSettingsRow(icon = MaterialSymbols.Rounded.BusinessCenter, title = K.elvanNavil.tr(), description = K.elvanNavilStudioDesc.tr(), onClick = { onNavigate(SettingsRoute.ElvanNavil) }, colors = colors)
                }
            }
        }
        item(key = "system_legal_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsRow(icon = MaterialSymbols.Rounded.Lock, title = K.privacyPolicy.tr(), description = K.privacyPolicyHint.tr(), onClick = { onNavigate(SettingsRoute.Privacy) }, colors = colors)
                    ElvanSettingsDivider(colors = colors)
                    ElvanSettingsRow(icon = MaterialSymbols.Rounded.Info, title = K.termsOfUse.tr(), description = K.termsOfUseHint.tr(), onClick = { onNavigate(SettingsRoute.Terms) }, colors = colors)
                    ElvanSettingsDivider(colors = colors)
                    ElvanSettingsRow(icon = MaterialSymbols.Rounded.Code, title = K.openSourceLicences.tr(), description = K.openSourceLicencesHint.tr(), onClick = { onNavigate(SettingsRoute.Licences) }, colors = colors)
                }
            }
        }
    }
}
