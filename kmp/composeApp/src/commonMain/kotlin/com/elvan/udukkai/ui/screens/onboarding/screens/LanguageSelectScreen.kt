package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.extensions.cssShadow
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.trWithLang
import com.elvan.udukkai.core.platform.getSystemLanguageCode
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.navigation.MaterialSymbols

private data class LanguageChoice(val code: String, val title: String)

/**
 * The first page of the logged-out flow, laid out and animated like Neram's LanguageSelectionScreen:
 * a globe, the title in the language being previewed, a rounded card of languages, then Continue.
 */
@Composable
fun LanguageSelectScreen(onContinue: () -> Unit) {
    var selected by remember { mutableStateOf(LanguageManager.currentLanguage.code) }
    val preview = if (selected == "system") getSystemLanguageCode() else selected
    val dark = ThemeManager.isDark()
    val cardShape = RoundedCornerShape(24.dp)
    val choices = listOf(
        LanguageChoice("system", K.deviceLanguage.trWithLang(preview)),
        LanguageChoice("ta", "தமிழ்"),
        LanguageChoice("ta-Latn", "Thamizh"),
        LanguageChoice("en", "English")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        NeramElement(delayIndex = 0) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Language,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = NeramColors.textPrimary()
            )
        }

        Spacer(Modifier.height(14.dp))

        NeramElement(delayIndex = 1) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = K.language.trWithLang(preview),
                    style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = NeramColors.textPrimary(), lineHeight = 32.sp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = K.selectPreferredLanguage.trWithLang(preview),
                    style = TextStyle(fontSize = 14.sp, color = NeramColors.textSecondary(), lineHeight = 20.sp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        NeramElement(delayIndex = 2, modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (dark) Modifier
                        else Modifier.cssShadow(color = Color.Black, alpha = 0.12f, blurRadius = 14.dp, offsetY = 4.dp)
                    )
                    .clip(cardShape)
                    .background(NeramColors.inputBackground())
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                choices.forEachIndexed { index, choice ->
                    val isSelected = selected == choice.code
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selected = choice.code }
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = choice.title,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                lineHeight = 21.sp
                            ),
                            color = if (isSelected) NeramColors.textPrimary() else NeramColors.textPrimary().copy(alpha = 0.75f)
                        )
                        if (isSelected) {
                            Icon(
                                imageVector = MaterialSymbols.Rounded.CheckCircleFill,
                                contentDescription = null,
                                tint = NeramColors.textPrimary(),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    if (index < choices.size - 1) {
                        HorizontalDivider(
                            color = if (dark) Color.White.copy(alpha = 0.055f) else Color.Black.copy(alpha = 0.055f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        NeramElement(delayIndex = 3, modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                NeramButton(
                    text = K.continueText.trWithLang(preview),
                    onClick = {
                        LanguageManager.setLanguageByCode(selected)
                        onContinue()
                    }
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
