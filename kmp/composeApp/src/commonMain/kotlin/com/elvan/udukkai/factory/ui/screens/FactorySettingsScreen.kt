package com.elvan.udukkai.factory.ui.screens

import com.elvan.udukkai.data.settings.BusinessProfile
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.factory.data.FactoryProfileRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.FontManager
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.Transitions
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.SettingsRoute
import com.elvan.udukkai.ui.screens.settings.SettingsNavState
import com.elvan.udukkai.ui.screens.settings.SettingsSearchOverlay
import com.elvan.udukkai.ui.screens.settings.SettingsSearchPill
import com.elvan.udukkai.ui.screens.settings.settingsEntries
import com.elvan.udukkai.ui.screens.settings.screens.*
import com.elvan.udukkai.factory.ui.screens.settings.*

@Composable
fun FactorySettingsScreen(
    onBack: () -> Unit = {}
) {
    var navState by remember { mutableStateOf(SettingsNavState(SettingsRoute.Hub, 0)) }
    var searchOpen by remember { mutableStateOf(false) }
    val colors = rememberShellColors()

    val hubScrollState = rememberLazyListState()
    val subpageScrollStates = remember { mutableStateMapOf<SettingsRoute, LazyListState>() }

    // History stack for correct pop destinations (Hub is always at the bottom)
    val routeHistory = remember { mutableStateListOf<SettingsRoute>(SettingsRoute.Hub) }

    fun navigateTo(route: SettingsRoute) {
        if (routeHistory.last() == route) return
        routeHistory.add(route)
        navState = SettingsNavState(route, navState.index + 1)
    }

    val handleBack: () -> Unit = {
        if (routeHistory.size > 1) {
            routeHistory.removeLast()
            navState = SettingsNavState(routeHistory.last(), navState.index - 1)
        } else {
            onBack()
        }
    }

    AppBackHandler(enabled = searchOpen || routeHistory.size > 1) {
        if (searchOpen) searchOpen = false else handleBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AnimatedContent(
            targetState = navState,
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            transitionSpec = {
                val isForward = targetState.index >= initialState.index
                with(Transitions) {
                    if (targetState.route == SettingsRoute.ManageProfiles) {
                        smoothVertical(forward = true)
                    } else if (initialState.route == SettingsRoute.ManageProfiles) {
                        smoothVertical(forward = false)
                    } else {
                        smoothHorizontal(forward = isForward)
                    }
                }
            },
            label = "FactorySettingsTransition"
        ) { state ->
            val route = state.route
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
                SettingsRoute.StorageBackup -> K.storageAndBackup.tr()
                SettingsRoute.Security -> K.security.tr()
                SettingsRoute.AboutDeveloper -> K.softwareDesigner.tr()
                SettingsRoute.AboutApp -> K.aboutApp.tr()
                SettingsRoute.ElvanNavil -> K.aboutElvanNavil.tr()
                SettingsRoute.Privacy -> K.privacyPolicy.tr()
                SettingsRoute.Terms -> K.termsOfUse.tr()
                SettingsRoute.Licences -> K.openSourceLicences.tr()
                SettingsRoute.AppLock -> K.appLock.tr()
                SettingsRoute.About -> K.groupAbout.tr()
            }

            ElvanSubShell(
                title = pageTitle,
                onBack = handleBack,
                scrollState = scrollState,
                floatingBar = { if (route == SettingsRoute.Hub) SettingsSearchPill(colors) { searchOpen = true } }
            ) {
                when (route) {
                    SettingsRoute.Hub -> FactorySettingsHubContent(
                        onNavigate = { navigateTo(it) },
                        scrollState = scrollState
                    )
                    SettingsRoute.Display -> DisplaySettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.Language -> LanguageSettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.Merchant -> FactoryMerchantSettingsScreen(
                        onNavigateToManageProfiles = { navigateTo(SettingsRoute.ManageProfiles) },
                        scrollState = scrollState,
                        colors = colors
                    )
                    SettingsRoute.ManageProfiles -> FactoryManageProfilesScreen(
                        onBack = { handleBack() },
                        scrollState = scrollState,
                        colors = colors
                    )
                    SettingsRoute.FactoryIdentity -> FactoryIdentityScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.TextilesIdentity -> {}
                    SettingsRoute.Address -> FactoryAddressSettingsScreen(
                        scrollState = scrollState,
                        colors = colors
                    )
                    SettingsRoute.Bank -> BankSettingsScreen(
                        scrollState = scrollState,
                        colors = colors,
                        profile = FactoryProfileRepository.profile,
                        onUpdateProfile = { FactoryProfileRepository.updateProfile(it) }
                    )
                    SettingsRoute.InvoiceCreation -> FactoryInvoiceCreationSettingsScreen(
                        scrollState = scrollState,
                        colors = colors
                    )
                    SettingsRoute.StorageBackup -> FactoryStorageBackupScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.Security -> FactorySecuritySettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.AboutDeveloper -> AboutDeveloperScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.AboutApp -> AboutAppScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.ElvanNavil -> TransliteratorSettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.Privacy -> LegalScreen(LegalPage.PRIVACY, scrollState, colors)
                    SettingsRoute.Terms -> LegalScreen(LegalPage.TERMS, scrollState, colors)
                    SettingsRoute.Licences -> LegalScreen(LegalPage.LICENCES, scrollState, colors)
                    SettingsRoute.AppLock -> AppLockSettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.About -> AboutHubScreen(versionText = "Udukkai Factory v1.0", onNavigate = { navigateTo(it) }, scrollState = scrollState, colors = colors)
                }
            }
        }
        SettingsSearchOverlay(
            open = searchOpen,
            entries = remember { settingsEntries(SettingsRoute.FactoryIdentity) },
            colors = colors,
            onOpen = { navigateTo(it); searchOpen = false },
            onClose = { searchOpen = false }
        )
    }
}

@Composable
private fun FactorySettingsHubContent(
    onNavigate: (SettingsRoute) -> Unit,
    scrollState: LazyListState
) {
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current
    val profile = FactoryProfileRepository.profile

    val profileName = resolveCompanyItemLabel(profile).ifEmpty { "ஆலை" }

    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            bottom = Dimens.SubpageContentPaddingBottom + 72.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "shell_top_spacer") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current + 6.dp)) // keeps the top where it was while the gaps below are tighter
        }

        // Section 0: Factory Company Pill
        item(key = "factory_company_pill") {
            ElvanSectionContainer {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ShellDefaults.ripple(colors, bounded = true),
                            onClick = { onNavigate(SettingsRoute.Merchant) }
                        ),
                    shape = RoundedCornerShape(999.dp),
                    color = colors.surface,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .padding(start = 11.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(colors.iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            com.elvan.udukkai.ui.screens.settings.AccountAvatar {
                            Text(
                                text = profileName.take(1).uppercase(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "ஆலை",
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = profileName.preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary.copy(alpha = 0.5f)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Icon(
                            imageVector = MaterialSymbols.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = colors.textTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item(key = "group_business") { SettingsGroupTitle(K.groupBusiness.tr(), colors) }

        // Section 1: Business Identity & Finance
        item(key = "business_identity_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.BusinessCenter,
                        title = K.identifier.tr(),
                        description = K.identityHint.tr(),
                        onClick = { onNavigate(SettingsRoute.FactoryIdentity) },
                        colors = colors
                    )

                    ElvanSettingsDivider(colors = colors)

                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.LocationOn,
                        title = K.address.tr(),
                        description = K.companyAddressDetails.tr(),
                        onClick = { onNavigate(SettingsRoute.Address) },
                        colors = colors
                    )

                    ElvanSettingsDivider(colors = colors)

                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.CreditCard,
                        title = K.bank.tr(),
                        description = K.bankDetails.tr(),
                        onClick = { onNavigate(SettingsRoute.Bank) },
                        colors = colors
                    )
                }
            }
        }

        item(key = "group_bills") { SettingsGroupTitle(K.groupBills.tr(), colors) }

        // Section 2: Invoicing & Interface
        item(key = "invoicing_interface_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Description,
                        title = K.createBtn.tr(),
                        description = K.pdfStylingOptions.tr(),
                        onClick = { onNavigate(SettingsRoute.InvoiceCreation) },
                        colors = colors
                    )
                }
            }
        }

        item(key = "group_app") { SettingsGroupTitle(K.groupApp.tr(), colors) }

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

        item(key = "group_data") { SettingsGroupTitle(K.groupData.tr(), colors) }

        // Section 3: Data & Security
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

                    ElvanSettingsDivider(colors = colors)

                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Lock,
                        title = K.appLock.tr(),
                        description = K.appLockRowHint.tr(),
                        onClick = { onNavigate(SettingsRoute.AppLock) },
                        colors = colors
                    )

                    ElvanSettingsDivider(colors = colors)

                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Lock,
                        title = K.security.tr(),
                        description = K.syncLogoutClearData.tr(),
                        onClick = { onNavigate(SettingsRoute.Security) },
                        colors = colors
                    )
                }
            }
        }

        item(key = "group_about") { SettingsGroupTitle(K.groupAbout.tr(), colors) }

        item(key = "system_about_section") {
            ElvanSectionContainer {
                ElvanSettingsSection(colors = colors) {
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Info,
                        title = K.aboutAndLegal.tr(),
                        description = K.aboutAndLegalHint.tr(),
                        onClick = { onNavigate(SettingsRoute.About) },
                        colors = colors
                    )
                }
            }
        }
    }
}
