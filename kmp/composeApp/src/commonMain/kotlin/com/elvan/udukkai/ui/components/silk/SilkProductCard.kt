package com.elvan.udukkai.ui.components.silk

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.textiles.TextilesProductCard

@Deprecated("Use TextilesProductCard", ReplaceWith("TextilesProductCard(productName, hsnCode, weightGrams, gstRate, price, modifier, onClick, colors)"))
@Composable
fun SilkProductCard(
    productName: String,
    hsnCode: String,
    weightGrams: String,
    gstRate: String,
    price: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: ShellColors = rememberShellColors()
) = TextilesProductCard(productName, hsnCode, weightGrams, gstRate, price, modifier, onClick, colors)
