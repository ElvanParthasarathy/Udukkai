package com.elvan.udukkai.ui.screens.home.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.mode.LocalAppMode
import com.elvan.udukkai.core.mode.ModeManager
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.navigation.NavTab

/**
 * Elvan Desktop Sidebar (எல்வன் கணினிப் பக்கப்பட்டை).
 * Full desktop navigation sidebar matching Flutter's `elvan_kanini_pakkapattai.dart`.
 * Supports expand (260dp) and collapse (80dp) states.
 */
@Composable
fun DesktopSideBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    onSettingsClick: () -> Unit,
    colors: ShellColors,
    modifier: Modifier = Modifier
) {
    var isCollapsed by remember { mutableStateOf(false) }
    val ff = LocalAppFontFamily.current
    val currentMode = LocalAppMode.current
    val activeProfile = NiruvanaTharavugalRepository.getProfile(currentMode)
    val businessName = activeProfile.niruvanathinPeyar[activeProfile.mudhanMozhi]
        ?: activeProfile.kurumPeyar.ifEmpty { "Udukkai" }

    val sidebarWidth by animateDpAsState(
        targetValue = if (isCollapsed) 80.dp else 260.dp,
        animationSpec = tween(durationMillis = 300),
        label = "sidebar_width"
    )

    Surface(
        modifier = modifier
            .width(sidebarWidth)
            .fillMaxHeight(),
        color = colors.background,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp, horizontal = if (isCollapsed) 12.dp else 16.dp)
        ) {
            // ── 1. Top Brand Header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.SpaceBetween
            ) {
                if (!isCollapsed) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.textPrimary,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "U",
                                    fontFamily = ff,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = colors.background
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Udukkai",
                            fontFamily = ff,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Collapse / Expand toggle button
                IconButton(
                    onClick = { isCollapsed = !isCollapsed },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isCollapsed) MaterialSymbols.Rounded.ChevronRight else MaterialSymbols.Rounded.ArrowBack,
                        contentDescription = "Toggle Sidebar",
                        tint = colors.textPrimary.copy(alpha = 0.6f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── 2. Navigation Items ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NavTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val title = tab.getLocalizedLabel()
                    val icon = if (isSelected) tab.activeIcon else tab.icon
                    val itemBg = if (isSelected) colors.textPrimary else Color.Transparent
                    val itemContentColor = if (isSelected) colors.background else colors.textPrimary.copy(alpha = 0.7f)

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .clickable { onTabSelected(tab) },
                        shape = RoundedCornerShape(100.dp),
                        color = itemBg
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Start,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = if (isCollapsed) 0.dp else 16.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = title,
                                tint = itemContentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            if (!isCollapsed) {
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = title,
                                    fontFamily = ff,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 15.sp,
                                    color = itemContentColor,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // ── 3. Bottom Zone: Mode Selector & Settings ──
            Divider(
                color = colors.textPrimary.copy(alpha = 0.08f),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Mode Selector Pill
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { ModeManager.openModeSelector() },
                shape = RoundedCornerShape(16.dp),
                color = colors.textPrimary.copy(alpha = 0.05f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Start,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = colors.iconBg,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (currentMode == AppMode.KOOLI) MaterialSymbols.Mode.Coolie else MaterialSymbols.Mode.Silk,
                                contentDescription = null,
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (!isCollapsed) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = businessName,
                                fontFamily = ff,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentMode.displayName(),
                                fontFamily = ff,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.sp,
                                color = colors.textPrimary.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Settings Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { onSettingsClick() },
                shape = RoundedCornerShape(100.dp),
                color = Color.Transparent
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (isCollapsed) Arrangement.Center else Arrangement.Start,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = if (isCollapsed) 0.dp else 16.dp)
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Settings,
                        contentDescription = K.settings.tr(),
                        tint = colors.textPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(22.dp)
                    )
                    if (!isCollapsed) {
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = K.settings.tr(),
                            fontFamily = ff,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = colors.textPrimary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
