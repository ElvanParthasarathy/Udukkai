package com.elvan.udukkai.ui.screens.onboarding.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.elvan.udukkai.core.platform.PlatformType
import com.elvan.udukkai.core.platform.currentPlatform
import com.elvan.udukkai.core.platform.isStoragePermissionGranted
import com.elvan.udukkai.core.platform.requestStoragePermission
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.ui.screens.onboarding.components.AuthAnimatedElement
import com.elvan.udukkai.ui.screens.onboarding.components.AuthButton
import com.elvan.udukkai.ui.screens.onboarding.components.AuthHeader
import com.elvan.udukkai.ui.screens.onboarding.components.AuthLayout
import com.elvan.udukkai.ui.navigation.MaterialSymbols
import kotlinx.coroutines.delay

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

    AuthLayout(showBranding = true) {
        AuthAnimatedElement(delayIndex = 0) {
            Icon(
                imageVector = MaterialSymbols.Rounded.Lock,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        AuthHeader(
            title = K.storagePermissionRequired.tr(),
            subtitle = K.storagePermissionDesc.tr()
        )

        Spacer(modifier = Modifier.height(48.dp))

        AuthButton(
            text = K.grantPermissionBtn.tr(),
            onClick = {
                requestStoragePermission()
            }
        )
    }
}

