package com.elvan.udukkai.ui.components.coolie

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.factory.FactoryLaborItemCard

@Deprecated("Use FactoryLaborItemCard", ReplaceWith("FactoryLaborItemCard(workName, wageRate, units, total, modifier, onClick, colors)"))
@Composable
fun CoolieLaborItemCard(
    workName: String,
    wageRate: String,
    units: String,
    total: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: ShellColors = rememberShellColors()
) = FactoryLaborItemCard(workName, wageRate, units, total, modifier, onClick, colors)

@Deprecated("Use FactoryLaborItemCard", ReplaceWith("FactoryLaborItemCard(workName, wageRate, units, total, modifier, onClick, colors)"))
@Composable
fun KooliLaborItemCard(
    workName: String,
    wageRate: String,
    units: String,
    total: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    colors: ShellColors = rememberShellColors()
) = FactoryLaborItemCard(workName, wageRate, units, total, modifier, onClick, colors)
