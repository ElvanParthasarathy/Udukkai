package com.elvan.udukkai.textiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import com.elvan.udukkai.core.platform.PlatformType
import com.elvan.udukkai.core.platform.currentPlatform
import com.elvan.udukkai.core.platform.isDebugBuild
import com.elvan.udukkai.core.platform.isStoragePermissionGranted
import com.elvan.udukkai.localization.ProvideAppLanguage
import com.elvan.udukkai.textiles.data.TextilesProfileRepository
import com.elvan.udukkai.textiles.dev.TextilesDevMenu
import com.elvan.udukkai.textiles.ui.TextilesHomeScreen
import com.elvan.udukkai.theme.UdukkaiTheme
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanBottomSheetController
import com.elvan.udukkai.ui.components.shell.ElvanBottomSheetHost
import com.elvan.udukkai.ui.components.shell.ElvanSnackbarHost
import com.elvan.udukkai.ui.components.shell.LocalElvanBottomSheetController
import com.elvan.udukkai.ui.screens.onboarding.screens.AnumadhiKaavalarThirai

/**
 * Root Composable for Udukkai Textiles (சவுளி).
 * Standalone application entry point with zero AppMode dependency.
 */
@Composable
fun TextilesApp() {
    var hasPermission by remember {
        mutableStateOf(currentPlatform == PlatformType.DESKTOP || isStoragePermissionGranted())
    }

    LaunchedEffect(Unit) {
        TextilesProfileRepository.refreshFromDatabase()
    }

    ProvideAppLanguage {
        UdukkaiTheme {
            val bottomSheetController = remember { ElvanBottomSheetController() }
            val colors = rememberShellColors()

            CompositionLocalProvider(
                LocalElvanBottomSheetController provides bottomSheetController
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background)
                ) {
                    if (!hasPermission) {
                        AnumadhiKaavalarThirai(
                            onPermissionGranted = {
                                hasPermission = true
                                TextilesProfileRepository.refreshFromDatabase()
                            }
                        )
                    } else {
                        TextilesHomeScreen()
                    }

                    ElvanBottomSheetHost(bottomSheetController)
                    ElvanSnackbarHost(colors = colors)

                    if (isDebugBuild) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(999f)
                        ) {
                            TextilesDevMenu()
                        }
                    }
                }
            }
        }
    }
}
