package com.elvan.udukkai.ui.screens.settings.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.auth.AppScope
import com.elvan.udukkai.core.auth.AuthManager
import com.elvan.udukkai.core.utils.DateGroupUtils
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LocalAppLanguage
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
 * செயலி பற்றி திரை — About App Screen.
 * Purpose-built, distinct About App screen tailored specifically for Factory (ஆலை) vs Textiles (சவுளி).
 * Brand: Udukkai · Studio: Elvan Navil · Designed & engineered by Elvan Parthasarathy.
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
    val isTa = LocalAppLanguage.current.startsWith("ta")

    val currentYear = remember {
        DateGroupUtils.getDateComponents(System.currentTimeMillis()).year
    }

    val appNameText = if (isFactory) K.udukkaiFactory.tr() else K.udukkaiTextiles.tr()
    val appSubtitleText = if (isFactory) {
        if (isTa) "நெசவாலை மேலாண்மை மென்பொருள்" else "Weaving Factory Management System"
    } else {
        if (isTa) "சவுளி வணிக மேலாண்மை முறைமை" else "Textiles & Silk ERP Billing System"
    }

    LazyColumn(
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

        // ── App Header: Emblem + Name + Subtitle + Studio + Version ──
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
                                    spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
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
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = appSubtitleText,
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colors.textSecondary.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isTa) "எல்வன் நவில் · v1.0.0" else "Elvan Navil · v1.0.0",
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = colors.textSecondary.copy(alpha = 0.6f)
                )
            }
        }

        // ── About App Dedicated Purpose Description ──
        item(key = "about_description_section") {
            ElvanSettingsSection(colors = colors) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Text(
                        text = if (isFactory) {
                            if (isTa) {
                                "விசைத்தறி மற்றும் கைத்தறி ஆலைகளின் உற்பத்தி, நெசவுக் கூலிச் சீட்டுகள் மற்றும் கூலிப் பட்டியல் மேலாண்மைக்கான மென்பொருள் முறைமை."
                            } else {
                                "A dedicated operational system crafted for powerloom and handloom weaving factories to manage production, loom wages, and coolie bills."
                            }
                        } else {
                            if (isTa) {
                                "பட்டு, ஜரிகை மற்றும் சவுளி வணிகர்களுக்கான விற்பனைப் பட்டியல், வாடிக்கையாளர் கணக்கு ஏடு மற்றும் GST வரி முறைமை."
                            } else {
                                "A commercial billing and ledger system crafted for silk and textile merchants to manage multi-firm sales, customer ledgers, and GST invoicing."
                            }
                        },
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            lineHeight = 22.sp
                        ),
                        color = colors.textPrimary.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // ── Core Features Tailored to Factory or Textiles ──
        item(key = "core_features_section") {
            ElvanSettingsSection(colors = colors) {
                if (isFactory) {
                    // Factory Feature 1: Looms & Weaving Production
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Handyman,
                        title = if (isTa) "தறிகள் & நெசவு உற்பத்தி" else "Looms & Weaving Production",
                        description = if (isTa) "தறி ஒதுக்கீடு, பாவு வரவு-செலவு மற்றும் உற்பத்திப் பதிவு" else "Loom allocation, warp runs, and production output tracking",
                        onClick = {},
                        colors = colors
                    )
                    ElvanSettingsDivider(colors = colors)

                    // Factory Feature 2: Coolie Wage Settlement
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Payments,
                        title = if (isTa) "கூலிச் சீட்டு & பட்டுவாடா" else "Coolie Wage Settlements",
                        description = if (isTa) "வாராந்திர நெசவுக் கூலி கணக்கீடு மற்றும் பட்டுவாடாச் சீட்டுகள்" else "Weekly coolie wage calculations and settlement slips",
                        onClick = {},
                        colors = colors
                    )
                    ElvanSettingsDivider(colors = colors)

                    // Factory Feature 3: Factory Invoices & Receipts
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Description,
                        title = if (isTa) "ஆலைப் பட்டியல் & பெறுகைகள்" else "Factory Bills & Receipts",
                        description = if (isTa) "நெசவுக் கூலி GST பட்டியல்கள் மற்றும் கட்டணப் பெறுகைகள்" else "GST job-work invoices and payment receipts",
                        onClick = {},
                        colors = colors
                    )
                    ElvanSettingsDivider(colors = colors)

                    // Factory Feature 4: Cloud Sync & Backups
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Sync,
                        title = if (isTa) "மேகக் கணினி ஒத்திசைவு" else "Cloud Sync & Backups",
                        description = if (isTa) "பாதுகாப்பான உள்ளூர் காப்புகள் மற்றும் சாதன ஒத்திசைவு" else "Secure local database backups and device sync",
                        onClick = {},
                        colors = colors
                    )
                } else {
                    // Textiles Feature 1: GST Tax Invoicing
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Description,
                        title = if (isTa) "GST விற்பனைப் பட்டியல்" else "GST Tax Invoicing",
                        description = if (isTa) "HSN குறியீடுகள், வரிப் பிரிப்புகள் மற்றும் அச்சிடும் பட்டியல் வடிவமைப்பு" else "HSN codes, tax splits, and custom bill print designs",
                        onClick = {},
                        colors = colors
                    )
                    ElvanSettingsDivider(colors = colors)

                    // Textiles Feature 2: Customer Ledger & Khata
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.BusinessCenter,
                        title = if (isTa) "வாடிக்கையாளர் கணக்கு ஏடு" else "Customer Credit Ledger",
                        description = if (isTa) "வாங்குவோர் நிலுவைத் தொகை, வரவு-பற்று மற்றும் கணக்கு அறிக்கை" else "Buyer balances, debit-credit tracking, and statement summaries",
                        onClick = {},
                        colors = colors
                    )
                    ElvanSettingsDivider(colors = colors)

                    // Textiles Feature 3: Payment Receipts
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.ReceiptFill,
                        title = if (isTa) "கட்டணப் பெறுகைகள்" else "Payment Receipts",
                        description = if (isTa) "நேரடிப் பணம், வங்கி, யுபிஐ ரசீதுகள் மற்றும் பட்டியல் வரவு கழித்தல்" else "Cash, Bank, and UPI receipts linked to pending bills",
                        onClick = {},
                        colors = colors
                    )
                    ElvanSettingsDivider(colors = colors)

                    // Textiles Feature 4: Cloud Sync & Security
                    ElvanSettingsRow(
                        icon = MaterialSymbols.Rounded.Sync,
                        title = if (isTa) "மேகக் கணினி ஒத்திசைவு" else "Cloud Sync & Security",
                        description = if (isTa) "நிறுவனத் தரவுப் பாதுகாப்பு மற்றும் தானியங்கி சாதன ஒத்திசைவு" else "Multi-firm data protection and automatic device synchronization",
                        onClick = {},
                        colors = colors
                    )
                }
            }
        }

        // ── Dynamic Copyright Year Footer ──
        item(key = "about_footer") {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "© $currentYear ${if (isTa) "எல்வன் நவில்" else "Elvan Navil"}",
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = colors.textPrimary.copy(alpha = 0.38f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
