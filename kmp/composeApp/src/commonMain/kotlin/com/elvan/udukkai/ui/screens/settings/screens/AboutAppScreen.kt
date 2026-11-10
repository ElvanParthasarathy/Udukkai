package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AppScope
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.platform.appBuildNumber
import com.elvan.udukkai.core.platform.appPackageName
import com.elvan.udukkai.core.platform.appSourceName
import com.elvan.udukkai.core.platform.appVersionName
import com.elvan.udukkai.core.platform.hasInAppUpdater
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.*
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.launch
import migrate.composeapp.generated.resources.Res
import migrate.composeapp.generated.resources.factory_logo_vector
import migrate.composeapp.generated.resources.textiles_logo_vector
import org.jetbrains.compose.resources.painterResource

/**
 * செயலி பற்றி — the app's own info page: its name and version, the plain facts of this build (version, build number, package,
 * where it came from, who made it), and what the app does, said plainly. Textiles and Factory each list their own.
 */
@Composable
fun AboutAppScreen(
    scrollState: LazyListState = rememberLazyListState(),
    colors: ShellColors = rememberShellColors(),
    isFactory: Boolean = (AuthManager.appScope == AppScope.FACTORY)
) {
    val ff = LocalAppFontFamily.current
    val scope = rememberCoroutineScope()
    var taps by remember { mutableStateOf(0) }
    val spin = remember { Animatable(0f) }

    val appNameText = if (isFactory) K.udukkaiFactory.tr() else K.udukkaiTextiles.tr()

    com.elvan.udukkai.ui.components.shell.ElvanLazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ContentPadding,
            end = Dimens.ContentPadding,
            bottom = Dimens.SubpageContentPaddingBottom
        ),
        verticalArrangement = Arrangement.spacedBy(Dimens.SectionSpacing)
    ) {
        item(key = "shell_top_spacer") {
            Spacer(modifier = Modifier.height(LocalElvanTopSpacerHeight.current))
        }

        // ── The logo, the name and the version ──
        item(key = "app_header") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Easter egg: the logo spins smoothly when tapped; 7th tap triggers the weavers mantra
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .graphicsLayer { rotationZ = spin.value }
                        .clip(RoundedCornerShape(26.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            taps++
                            scope.launch {
                                spin.animateTo(
                                    spin.value + 360f,
                                    spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                                )
                            }
                            if (taps % 7 == 0) {
                                ElvanSnackbar.show("🧵 நூலோடு நூல் சேர்த்து நெய்தது · Woven thread by thread")
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(if (isFactory) Res.drawable.factory_logo_vector else Res.drawable.textiles_logo_vector),
                        contentDescription = K.aboutApp.tr(),
                        tint = Color.Unspecified,
                        modifier = Modifier.size(100.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = appNameText,
                    style = TextStyle(fontFamily = ff, fontSize = 25.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.5).sp),
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "v${appVersionName()}",
                    style = TextStyle(fontFamily = ff, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                    color = colors.textSecondary.copy(alpha = 0.7f)
                )
            }
        }

        // ── Software update (the GitHub build only: Google Play updates the Play build itself) ──
        if (hasInAppUpdater()) {
            item(key = "about_software_update_section") {
                PlatformUpdateSection(colors = colors)
            }
        }

        // ── The plain facts of this build ──
        item(key = "app_details") {
            ElvanSettingsSection(title = K.appDetails.tr(), colors = colors) {
                InfoRow(MaterialSymbols.Rounded.Info, K.version.tr(), appVersionName(), colors)
                ElvanSettingsDivider(colors = colors)
                InfoRow(MaterialSymbols.Rounded.Code, K.buildNumber.tr(), appBuildNumber().toString(), colors)
                ElvanSettingsDivider(colors = colors)
                InfoRow(MaterialSymbols.Rounded.Download, K.packageName.tr(), appPackageName(), colors)
                ElvanSettingsDivider(colors = colors)
                InfoRow(MaterialSymbols.Rounded.Sync, K.appSource.tr(), appSourceName(), colors)
                ElvanSettingsDivider(colors = colors)
                InfoRow(MaterialSymbols.Rounded.Person, K.madeBy.tr(), K.elvanNavil.tr(), colors)
            }
        }

        // ── What the app does, said plainly ──
        item(key = "what_it_does") {
            ElvanSettingsSection(title = K.whatItDoes.tr(), colors = colors) {
                val rows = if (isFactory) {
                    listOf(
                        Triple(MaterialSymbols.Rounded.Description, K.aboutJobWorkTitle, K.aboutJobWorkDesc),
                        Triple(MaterialSymbols.Rounded.ReceiptFill, K.aboutReceiptsTitle, K.aboutReceiptsDesc),
                        Triple(MaterialSymbols.Rounded.BusinessCenter, K.aboutPartiesTitle, K.aboutPartiesDesc),
                        Triple(MaterialSymbols.Rounded.Print, K.aboutPdfTitle, K.aboutPdfDesc),
                        Triple(MaterialSymbols.Rounded.Sync, K.aboutOfflineTitle, K.aboutOfflineDesc),
                        Triple(MaterialSymbols.Rounded.Lock, K.aboutSafeTitle, K.aboutSafeDesc)
                    )
                } else {
                    listOf(
                        Triple(MaterialSymbols.Rounded.Description, K.aboutGstBillsTitle, K.aboutGstBillsDesc),
                        Triple(MaterialSymbols.Rounded.ReceiptFill, K.aboutReceiptsTitle, K.aboutReceiptsDesc),
                        Triple(MaterialSymbols.Rounded.BusinessCenter, K.aboutPartiesTitle, K.aboutPartiesDesc),
                        Triple(MaterialSymbols.Rounded.Print, K.aboutPdfTitle, K.aboutPdfDesc),
                        Triple(MaterialSymbols.Rounded.Sync, K.aboutOfflineTitle, K.aboutOfflineDesc),
                        Triple(MaterialSymbols.Rounded.Lock, K.aboutSafeTitle, K.aboutSafeDesc)
                    )
                }
                rows.forEachIndexed { i, (icon, title, desc) ->
                    if (i > 0) ElvanSettingsDivider(colors = colors)
                    InfoRow(icon, title.tr(), desc.tr(), colors)
                }
            }
        }
    }
}

/** A line of facts: nothing opens from it, so it has no arrow. */
@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String, colors: ShellColors) {
    ElvanSettingsRow(
        icon = icon,
        title = title,
        description = description,
        onClick = {},
        customTrailing = {},
        colors = colors
    )
}
