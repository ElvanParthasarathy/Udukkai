package com.elvan.udukkai.ui.components.silk

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.textiles.TextilesItemRowCard

@Composable
fun SilkItemCard(
    itemName: String,
    hsnCode: String,
    weightGrams: String,
    gstRate: String,
    price: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: ShellColors = rememberShellColors()
) = TextilesItemRowCard(itemName, hsnCode, weightGrams, gstRate, price, modifier, onClick, colors)
