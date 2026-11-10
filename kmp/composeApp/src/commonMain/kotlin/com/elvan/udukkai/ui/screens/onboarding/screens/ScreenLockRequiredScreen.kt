package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.onboarding.components.AuthBackdrop

/**
 * Screen lock requirement gate. Displays when the host device has no PIN, pattern, or
 * biometric lock configured, matching the layout and aesthetic of the storage permission
 * and onboarding screens with zero extraneous branding.
 */
@Composable
fun ScreenLockRequiredScreen(
    onOpenSettings: () -> Unit
) {
    val dark = ThemeManager.isDark()
    Box(Modifier.fillMaxSize()) {
        AuthBackdrop()
        NeramIntroPage(
            horizontalPadding = 24.dp,
            header = {
                NeramElement(delayIndex = 0) {
                    Box(
                        modifier = Modifier
                            .size(if (LocalAuthLandscape.current) 72.dp else 120.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .background(if (dark) Color.White else Color(0xFF111111)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = MaterialSymbols.Rounded.Lock,
                            contentDescription = null,
                            tint = if (dark) Color.Black else Color.White,
                            modifier = Modifier.size(if (LocalAuthLandscape.current) 36.dp else 60.dp)
                        )
                    }
                }

                Spacer(Modifier.height(if (LocalAuthLandscape.current) 12.dp else 24.dp))

                NeramElement(delayIndex = 1, modifier = Modifier.fillMaxWidth()) {
                    NeramHeader(
                        title = K.screenLockRequired.tr(),
                        subtitle = K.screenLockRequiredDesc.tr()
                    )
                }

            },
            bottom = {
                NeramElement(delayIndex = 2, modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        NeramButton(
                            text = K.goToSettings.tr(),
                            onClick = onOpenSettings
                        )
                        Spacer(Modifier.height(if (LocalAuthLandscape.current) 0.dp else 40.dp))
                    }
                }
            }
        )
    }
}
