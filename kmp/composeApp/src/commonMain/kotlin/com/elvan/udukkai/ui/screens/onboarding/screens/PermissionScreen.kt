package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.platform.PlatformType
import com.elvan.udukkai.core.platform.currentPlatform
import com.elvan.udukkai.core.platform.isStoragePermissionGranted
import com.elvan.udukkai.core.platform.requestStoragePermission
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.ThemeManager
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import com.elvan.udukkai.ui.screens.onboarding.components.AuthBackdrop
import kotlinx.coroutines.delay

/** Asks for the storage permission, laid out like the Welcome page: the tile and the words in the middle, the button at the foot. */
@Composable
fun AnumadhiKaavalarThirai(
    onPermissionGranted: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (currentPlatform == PlatformType.DESKTOP || isStoragePermissionGranted()) {
            onPermissionGranted()
            return@LaunchedEffect
        }
        while (true) {
            delay(1000)
            if (isStoragePermissionGranted()) {
                onPermissionGranted()
                break
            }
        }
    }

    val dark = ThemeManager.isDark()
    Box(Modifier.fillMaxSize()) {
        AuthBackdrop()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            NeramElement(delayIndex = 0) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(36.dp))
                        .background(if (dark) Color.White else Color(0xFF111111)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = MaterialSymbols.Rounded.Lock,
                        contentDescription = null,
                        tint = if (dark) Color.Black else Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            NeramElement(delayIndex = 1) {
                NeramHeader(title = K.storagePermissionRequired.tr(), subtitle = K.storagePermissionDesc.tr())
            }

            Spacer(Modifier.weight(1f))

            NeramElement(delayIndex = 2, modifier = Modifier.fillMaxWidth()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    NeramButton(text = K.grantPermissionBtn.tr(), onClick = { requestStoragePermission() })
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }
}
