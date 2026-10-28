package com.elvan.udukkai.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.sync.RestorePhase
import com.elvan.udukkai.core.sync.getFirebaseSyncManager
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.Dimens
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.rememberShellColors
import com.elvan.udukkai.ui.components.ExpressiveContainedLoader

/** Shown while the data is being restored from the cloud; the app stays usable and fills up as it goes. */
@Composable
fun RestoreBanner(modifier: Modifier = Modifier) {
    val progress by getFirebaseSyncManager().restoreProgress
    val current = progress ?: return
    val colors = rememberShellColors()
    val ff = LocalAppFontFamily.current

    val phaseName = when (current.phase) {
        RestorePhase.PROFILES -> K.restorePhaseProfiles.tr()
        RestorePhase.CUSTOMERS -> K.restorePhaseCustomers.tr()
        RestorePhase.ITEMS -> K.restorePhaseItems.tr()
        RestorePhase.INVOICES -> K.restorePhaseInvoices.tr()
        RestorePhase.RECEIPTS -> K.restorePhaseReceipts.tr()
    }
    val count = if (current.total > 0) "  ${current.done}/${current.total}" else ""

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.ContentPadding)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ExpressiveContainedLoader(
            modifier = Modifier.size(46.dp),
            containerColor = colors.textPrimary.copy(alpha = 0.08f),
            indicatorColor = colors.textPrimary
        )
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                text = K.restoreCloudTitle.tr(),
                style = TextStyle(fontFamily = ff, fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
                color = colors.textPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = phaseName + count,
                style = TextStyle(fontFamily = ff, fontSize = 12.sp),
                color = colors.textSecondary
            )
        }
    }
}
