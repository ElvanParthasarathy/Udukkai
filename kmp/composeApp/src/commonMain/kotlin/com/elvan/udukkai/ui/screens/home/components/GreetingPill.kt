package com.elvan.udukkai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.ShellDefaults
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.shell.ElvanCompanySelectionBottomSheet
import com.elvan.udukkai.ui.components.shell.ElvanSectionContainer
import com.elvan.udukkai.ui.components.shell.resolveCompanyItemLabel
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.home.HomeAnimations
import kotlinx.coroutines.delay

@Composable
fun GreetingPill(
    colors: ShellColors,
    profile: BusinessProfile,
    allProfiles: List<BusinessProfile>,
    defaultTitle: String,
    modifier: Modifier = Modifier,
    showWelcomeMessage: Boolean? = null,
    onProfileChanging: ((Long) -> Unit)? = null,
    onManageProfiles: (() -> Unit)? = null
) {
    val ff = LocalAppFontFamily.current

    val fullLegalBusinessName = remember(profile, defaultTitle) {
        val primary = resolveCompanyItemLabel(profile).trim()
        if (primary.isNotEmpty()) {
            primary
        } else if (profile.shortName.isNotBlank()) {
            profile.shortName.trim()
        } else {
            defaultTitle
        }
    }

    var showCompanySheet by remember { mutableStateOf(false) }

    // Welcome transition state: only triggers once during initial app launch.
    // Once triggered, hasShownOnAppLaunch is immediately set to true so navigating
    // to other tabs/screens and returning will never replay the welcome greeting.
    var internalShowWelcome by remember {
        val shouldShowOnLaunch = !HomeAnimations.WelcomeTransition.hasShownOnAppLaunch
        if (shouldShowOnLaunch) {
            HomeAnimations.WelcomeTransition.hasShownOnAppLaunch = true
        }
        mutableStateOf(shouldShowOnLaunch)
    }
    val isWelcomeVisible = showWelcomeMessage ?: internalShowWelcome

    LaunchedEffect(Unit) {
        if (internalShowWelcome) {
            delay(HomeAnimations.WelcomeTransition.DurationMs)
            internalShowWelcome = false
        }
    }

    ElvanSectionContainer(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp)),
            shape = RoundedCornerShape(999.dp),
            color = colors.surface,
            shadowElevation = 0.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // 1. Welcome Greeting (Visually identical to Neram: 👋 + Welcome + Glad you're here)
                androidx.compose.animation.AnimatedVisibility(
                    visible = isWelcomeVisible,
                    enter = HomeAnimations.WelcomeTransition.EnterTransition,
                    exit = HomeAnimations.WelcomeTransition.ExitTransition
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ShellDefaults.ripple(colors, bounded = true),
                                onClick = {
                                    internalShowWelcome = false
                                    HomeAnimations.WelcomeTransition.hasShownOnAppLaunch = true
                                }
                            )
                            .padding(start = 11.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colors.iconBg,
                            modifier = Modifier.size(62.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "👋",
                                    style = TextStyle(fontSize = 26.sp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = K.welcomeToUdukkai.tr().preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 22.sp
                                ),
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = K.gladYouAreHere.tr().preventBrokenLigatures(),
                                style = TextStyle(
                                    fontFamily = ff,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = colors.textPrimary.copy(alpha = 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 2. Profile Content (Visible after 3s or when dismissed: Initial + வணக்கம் ✨ + Business Name)
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isWelcomeVisible,
                    enter = HomeAnimations.WelcomeTransition.EnterTransition,
                    exit = HomeAnimations.WelcomeTransition.ExitTransition
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(999.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ShellDefaults.ripple(colors, bounded = true),
                                onClick = { showCompanySheet = true }
                            )
                            .padding(start = 11.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = colors.iconBg,
                            modifier = Modifier.size(62.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = fullLegalBusinessName.take(1).uppercase(),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = K.vanakkam.tr().preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 22.sp
                                    ),
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.AutoAwesome,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(3.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = fullLegalBusinessName.preventBrokenLigatures(),
                                    style = TextStyle(
                                        fontFamily = ff,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = colors.textPrimary.copy(alpha = 0.5f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = MaterialSymbols.Rounded.KeyboardArrowDown,
                                    contentDescription = "Switch Company",
                                    tint = colors.textPrimary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCompanySheet) {
        ElvanCompanySelectionBottomSheet(
            profiles = allProfiles,
            activeProfileId = profile.id,
            onSelectProfile = { selectedId ->
                if (selectedId != profile.id) {
                    onProfileChanging?.invoke(selectedId)
                }
            },
            onManageProfiles = onManageProfiles,
            onDismissRequest = { showCompanySheet = false },
            colors = colors
        )
    }
}
