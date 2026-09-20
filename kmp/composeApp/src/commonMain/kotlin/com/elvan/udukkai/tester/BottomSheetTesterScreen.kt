package com.elvan.udukkai.tester

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.theme.LocalShellColors
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.UdukkaiTheme
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.shell.ElvanSelectionBottomSheet

/**
 * Pure, isolated Standalone Bottom Sheet Tester Screen.
 * 100% independent from all app shells, navigation, auth, and repositories.
 * Completely pitch-black background with pure bottom sheet gesture testing.
 */
@Composable
fun BottomSheetTesterScreen() {
    var isDark by remember { mutableStateOf(true) }
    var showShortSheet by remember { mutableStateOf(false) }
    var showLongSheet by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf("Item 1") }

    val shortItems = remember { listOf("Option Alpha", "Option Beta", "Option Gamma", "Option Delta", "Option Epsilon") }
    val longItems = remember { (1..40).map { "Long List Choice #$it" } }

    UdukkaiTheme {
        val colors = rememberShellColors()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Raw Bottom Sheet Tester",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Text(
                    text = "Selected: $selectedItem",
                    style = TextStyle(
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showShortSheet = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Test Short List (5 items)", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { showLongSheet = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Test Long List (40 items - Scroll)", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Checklist to test:\n• Pull DOWN anywhere on sheet to dismiss\n• Scroll up/down inside long list\n• Pull UP hard at bottom - ZERO bounce/detach\n• Tap outside dark scrim to dismiss",
                    style = TextStyle(fontSize = 13.sp, color = Color.LightGray, lineHeight = 20.sp)
                )
            }

            // Short Sheet
            if (showShortSheet) {
                ElvanSelectionBottomSheet(
                    title = "Select Option (Short)",
                    items = shortItems,
                    currentValue = selectedItem,
                    onSelected = { selectedItem = it },
                    onDismissRequest = { showShortSheet = false },
                    itemLabelBuilder = { it },
                    colors = colors
                )
            }

            // Long Sheet
            if (showLongSheet) {
                ElvanSelectionBottomSheet(
                    title = "Select Option (Long)",
                    items = longItems,
                    currentValue = selectedItem,
                    onSelected = { selectedItem = it },
                    onDismissRequest = { showLongSheet = false },
                    itemLabelBuilder = { it },
                    showSearch = true,
                    colors = colors
                )
            }
        }
    }
}
