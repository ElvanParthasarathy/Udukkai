package com.elvan.udukkai.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.mode.AppMode
import com.elvan.udukkai.core.utils.CurrencyUtils
import com.elvan.udukkai.core.utils.DateUtils
import com.elvan.udukkai.data.model.PattiyalTharavuru
import com.elvan.udukkai.data.settings.NiruvanaTharavugalRepository
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.preventBrokenLigatures
import com.elvan.udukkai.ui.components.ElvanCommonCard
import com.elvan.udukkai.ui.components.ElvanCardLeadingIcon
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.theme.LocalShellColors

/**
 * Pixel-perfect port of Flutter's SilkDashboardCard.
 */
@Composable
fun SilkDashboardCard(
    index: Int,
    pattiyal: PattiyalTharavuru,
    colors: ShellColors,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val ff = LocalAppFontFamily.current
    val isDark = colors.isDark

    val profile = NiruvanaTharavugalRepository.getProfile(AppMode.PATTU)
    val primaryLang = profile.mudhanMozhi.ifEmpty { "ta" }
    val secondaryLang = profile.thunaiMozhi.ifEmpty { "en" }

    val name = pattiyal.vaangunarPeyar[primaryLang]
        ?: pattiyal.vaangunarPeyar[secondaryLang]
        ?: pattiyal.vaangunarPeyar.values.firstOrNull()
        ?: "-"

    val amountStr = CurrencyUtils.formatInr(pattiyal.mothaThogai)
    val dateStr = DateUtils.formatEpochMillis(pattiyal.pattiyalNaal)

    ElvanCommonCard(
        onClick = onClick,
        modifier = modifier,
        padding = PaddingValues(
            horizontal = Dimens.CardPaddingHorizontal,
            vertical = Dimens.CardPaddingVertical
        ),
        borderRadius = Dimens.CardRadius
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            ElvanCardLeadingIcon(
                icon = MaterialSymbols.Rounded.DescriptionFill,
                tint = colors.invoiceColor
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Row 1: Customer Name + Chevron
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name.preventBrokenLigatures(),
                        style = TextStyle(
                            fontFamily = ff,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        ),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Icon(
                        imageVector = MaterialSymbols.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = LocalShellColors.current.border,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Row 2: Invoice #  •  Date
                Text(
                    text = "${pattiyal.patrucheettuEn}  •  $dateStr",
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = 13.5.sp,
                        color = LocalShellColors.current.textSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Row 3: Right-aligned Amount
                Text(
                    text = amountStr,
                    style = TextStyle(
                        fontFamily = ff,
                        fontSize = if (amountStr.length > 11) 13.sp else 15.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.accent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
