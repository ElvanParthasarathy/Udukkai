package com.elvan.udukkai.ui.screens.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.elvan.udukkai.core.platform.AppBackHandler
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Transitions
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanActionSheet
import com.elvan.udukkai.ui.components.shell.ElvanSubShell
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.settings.screens.*

/**
 * Master One UI Settings Screen with subpage routing and Brick Wall architecture.
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {}
) {
    var currentRoute by remember { mutableStateOf<SettingsRoute>(SettingsRoute.Hub) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    val colors = rememberShellColors()

    val hubScrollState = rememberLazyListState()
    val subpageScrollStates = remember { mutableStateMapOf<SettingsRoute, LazyListState>() }

    val handleBack: () -> Unit = {
        if (currentRoute == SettingsRoute.ManageProfiles) {
            currentRoute = SettingsRoute.Merchant
        } else if (currentRoute != SettingsRoute.Hub) {
            currentRoute = SettingsRoute.Hub
        } else {
            onBack()
        }
    }

    AppBackHandler(enabled = true) {
        handleBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        AnimatedContent(
            targetState = currentRoute,
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
            transitionSpec = {
                if (targetState == SettingsRoute.ManageProfiles) {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
                    ) togetherWith fadeOut(targetAlpha = 0.9f, animationSpec = tween(durationMillis = 50))
                } else if (initialState == SettingsRoute.ManageProfiles) {
                    fadeIn(initialAlpha = 0.9f) togetherWith slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Down,
                        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
                    )
                } else {
                    val isForward = targetState != SettingsRoute.Hub
                    if (isForward) {
                        slideIntoContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Left,
                            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
                        ) togetherWith fadeOut(targetAlpha = 0.9f, animationSpec = tween(durationMillis = 50))
                    } else {
                        fadeIn(initialAlpha = 0.9f) togetherWith slideOutOfContainer(
                            towards = AnimatedContentTransitionScope.SlideDirection.Right,
                            animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioNoBouncy)
                        )
                    }
                }
            },
            label = "SettingsRouteTransition"
        ) { route ->
            val scrollState = if (route == SettingsRoute.Hub) hubScrollState else subpageScrollStates.getOrPut(route) { LazyListState() }
            val pageTitle = when (route) {
                SettingsRoute.Hub -> K.settings.tr()
                SettingsRoute.Display -> K.appearance.tr()
                SettingsRoute.Language -> K.appLanguage.tr()
                SettingsRoute.Merchant -> K.company.tr()
                SettingsRoute.ManageProfiles -> K.manage.tr()
                SettingsRoute.KooliIdentity -> K.coolieCompanyBranding.tr()
                SettingsRoute.PattuIdentity -> K.silkCompanyBranding.tr()
                SettingsRoute.Address -> K.address.tr()
                SettingsRoute.Bank -> K.bank.tr()
                SettingsRoute.InvoiceCreation -> K.createBtn.tr()
                SettingsRoute.UserProfile -> K.user.tr()
                SettingsRoute.StorageBackup -> K.storageAndBackup.tr()
                SettingsRoute.Security -> K.security.tr()
                SettingsRoute.AboutDeveloper -> K.softwareDesigner.tr()
                SettingsRoute.AboutApp -> K.aboutApp.tr()
                SettingsRoute.ElvanNavil -> K.aboutElvanNavil.tr()
            }

            val leadingIcon = if (route == SettingsRoute.ManageProfiles) {
                MaterialSymbols.Rounded.Close
            } else {
                null
            }

            ElvanSubShell(
                title = pageTitle,
                onBack = handleBack,
                leadingIcon = leadingIcon,
                scrollState = scrollState
            ) {
                when (route) {
                    SettingsRoute.Hub -> SettingsHubScreen(
                        onNavigate = { currentRoute = it },
                        onSignOutClick = { showSignOutDialog = true },
                        scrollState = scrollState,
                        colors = colors
                    )
                    SettingsRoute.Display -> DisplaySettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.Language -> LanguageSettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.Merchant -> MerchantSettingsScreen(
                        onNavigateToManageProfiles = { currentRoute = SettingsRoute.ManageProfiles },
                        scrollState = scrollState,
                        colors = colors
                    )
                    SettingsRoute.ManageProfiles -> ManageProfilesScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.KooliIdentity -> CoolieIdentityScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.PattuIdentity -> SilkIdentityScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.Address -> AddressSettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.Bank -> BankSettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.InvoiceCreation -> InvoiceCreationSettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.UserProfile -> UserProfileSettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.StorageBackup -> StorageBackupSettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.Security -> SecuritySettingsScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.AboutDeveloper -> AboutDeveloperScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.AboutApp -> AboutAppScreen(scrollState = scrollState, colors = colors)
                    SettingsRoute.ElvanNavil -> TransliteratorSettingsScreen(scrollState = scrollState, colors = colors)
                }
            }
        }

    if (showSignOutDialog) {
        ElvanActionSheet(
            title = K.signOutConfirmPrompt.tr(),
            cancelText = K.cancel.tr(),
            confirmText = K.logout.tr(),
            onDismissRequest = { showSignOutDialog = false },
            onConfirm = {
                showSignOutDialog = false
                onBack()
            },
            confirmColor = Color(0xFFBA1A1A),
            colors = colors
        )
    }
    }
}
