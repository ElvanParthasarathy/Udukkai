package com.elvan.udukkai.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.elvan.udukkai.core.print.BillHeaderPreview
import com.elvan.udukkai.core.print.PrintPayload
import com.elvan.udukkai.data.model.CustomerModel
import com.elvan.udukkai.data.model.InvoiceModel
import com.elvan.udukkai.data.model.ReceiptModel
import com.elvan.udukkai.core.utils.MiniJson
import com.elvan.udukkai.data.settings.BusinessProfile
import com.elvan.udukkai.data.settings.PrintNumbers
import com.elvan.udukkai.localization.LanguageManager
import com.elvan.udukkai.localization.K
import com.elvan.udukkai.localization.tr
import com.elvan.udukkai.theme.LocalAppFontFamily
import com.elvan.udukkai.theme.ShellColors
import com.elvan.udukkai.ui.components.shell.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** A logo's size (50% to 130% of what the bill draws) and place (pixels of the bill sheet, 0 = where the bill puts it). */
data class LogoPlace(val x: Float = 0f, val y: Float = 0f, val scale: Float = 1f) {
    fun moved(dx: Float, dy: Float) = copy(x = (x + dx).coerceIn(-MAX_MOVE, MAX_MOVE), y = (y + dy).coerceIn(-MAX_MOVE, MAX_MOVE))

    /** These settings written into the print settings JSON under [prefix] ("logo" or "wideLogo"). */
    fun writeTo(printSettingsJson: String, prefix: String, explicit: Boolean = false): String {
        // explicit keeps the values even when they are the defaults (a receipt's own place must not fall back to the invoice's)
        var json = PrintNumbers.set(printSettingsJson, prefix + "Scale", scale.toDouble(), default = if (explicit) Double.NaN else 1.0)
        json = PrintNumbers.set(json, prefix + "X", x.toDouble(), default = if (explicit) Double.NaN else 0.0)
        return PrintNumbers.set(json, prefix + "Y", y.toDouble(), default = if (explicit) Double.NaN else 0.0)
    }

    companion object {
        const val MAX_MOVE = 250f

        /** The place under [prefix]; where that has no setting, the one under [fallbackPrefix] (what is printed in that case). */
        fun readEffective(printSettingsJson: String, prefix: String, fallbackPrefix: String?): LogoPlace {
            val all = MiniJson.parseObject(printSettingsJson)
            val has = all.containsKey(prefix + "Scale") || all.containsKey(prefix + "X")
            return fromMap(all, if (has || fallbackPrefix == null) prefix else fallbackPrefix)
        }

        fun read(printSettingsJson: String, prefix: String) = fromMap(MiniJson.parseObject(printSettingsJson), prefix)

        private fun fromMap(all: Map<String, Any?>, prefix: String) = LogoPlace(
            x = (all[prefix + "X"] as? Number)?.toFloat() ?: 0f,
            y = (all[prefix + "Y"] as? Number)?.toFloat() ?: 0f,
            scale = (all[prefix + "Scale"] as? Number)?.toFloat() ?: 1f
        )
    }
}

/** A sample bill of this business with the logo being adjusted, for [BillHeaderPreview]; only the top of it is shown. */
fun logoPreviewPayload(profile: BusinessProfile, wide: Boolean, imagePath: String?, isFactory: Boolean, receipt: Boolean = false): String {
    val shown = profile.copy()
    if (wide) shown.wideLogo = imagePath.orEmpty() else shown.logo = imagePath.orEmpty()
    shown.headerLayout = if (wide) "wide" else "small"
    val invoice = InvoiceModel(invoiceNumber = "SAMPLE-001")
    val customer = CustomerModel(name = mapOf("en" to "Sample Customer", "ta" to "மாதிரி வாங்குநர்"))
    val lang = LanguageManager.activeLanguageCode
    if (receipt) {
        val sample = ReceiptModel(receiptNumber = "SAMPLE-001", customerName = customer.name, amount = 1000.0)
        return PrintPayload.forReceipt(sample, shown, emptyList(), isFactory, lang)
    }
    return if (isFactory) PrintPayload.forFactoryInvoice(invoice, shown, customer, lang)
    else PrintPayload.forTextilesInvoice(invoice, shown, customer, lang)
}

/**
 * Adjusts a logo against the real top of the bill: the React template's header (see [BillHeaderPreview]) with the logo
 * where it is printed, a joystick that moves it while it is held (the harder it is pushed, the faster) and a size
 * slider. [prefix] is "logo" or "wideLogo": the print settings keys are prefix + Scale / X / Y. Reset goes back to [saved],
 * the place and size the logo had when the editor was opened.
 * shortcut: the top is 130% so a bigger logo does not run into the bill number bar; raise it if the header gets room.
 */
@Composable
fun LogoAdjuster(payloadJson: String, prefix: String, place: LogoPlace, saved: LogoPlace, onPlace: (LogoPlace) -> Unit, title: String, colors: ShellColors) {
    val ff = LocalAppFontFamily.current
    val latest by rememberUpdatedState(place)

    key(payloadJson) {
        BillHeaderPreview(
            payloadJson = payloadJson,
            settingsJson = "{\"${prefix}Scale\":${place.scale},\"${prefix}X\":${place.x},\"${prefix}Y\":${place.y}}",
            modifier = Modifier.fillMaxWidth().aspectRatio(794f / 300f).clip(RoundedCornerShape(12.dp))
                .border(BorderStroke(1.dp, colors.textPrimary.copy(alpha = 0.15f)), RoundedCornerShape(12.dp))
        )
    }
    Spacer(Modifier.height(14.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LogoJoystick(onMove = { dx, dy -> onPlace(latest.moved(dx, dy)) }, colors = colors)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        style = TextStyle(fontFamily = ff, fontSize = 13.sp, fontWeight = FontWeight.Medium),
                        color = colors.textPrimary.copy(alpha = 0.7f),
                        maxLines = 1,
                        softWrap = false
                    )
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.iconBg)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "${(place.scale * 100f).roundToInt()}%",
                            style = TextStyle(fontFamily = ff, fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.iconBg)
                        .clickable { onPlace(saved) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = K.resetLogo.tr(),
                        style = TextStyle(fontFamily = ff, fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = colors.textPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            androidx.compose.material3.Slider(
                value = place.scale,
                onValueChange = { onPlace(latest.copy(scale = it)) },
                valueRange = 0.5f..1.3f,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * A round pad with a cross on it: the knob slides along the cross only, up, down, left or right (never across), and
 * while it is held off the centre [onMove] gets a step (up to 1.5 sheet pixels) every frame in that direction;
 * letting go centres it.
 */
@Composable
private fun LogoJoystick(onMove: (Float, Float) -> Unit, colors: ShellColors) {
    val move by rememberUpdatedState(onMove)
    var knob by remember { mutableStateOf(Offset.Zero) } // -1..1 on each side
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            if (knob != Offset.Zero) move(knob.x * 1.5f, knob.y * 1.5f)
        }
    }
    val ring = colors.textPrimary.copy(alpha = 0.25f)
    Canvas(
        Modifier
            .size(96.dp)
            .clip(CircleShape)
            .background(colors.iconBg)
            .pointerInput(Unit) {
            val reach = size.width / 2f * 0.7f
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                fun set(p: Offset) {
                    val v = p - Offset(size.width / 2f, size.height / 2f)
                    // The stronger direction wins and the other is dropped: the knob stays on the cross
                    val along = if (kotlin.math.abs(v.x) >= kotlin.math.abs(v.y)) Offset(v.x, 0f) else Offset(0f, v.y)
                    val len = along.getDistance()
                    knob = when {
                        len < reach * 0.15f -> Offset.Zero
                        len > reach -> along * (reach / len) / reach
                        else -> along / reach
                    }
                }
                set(down.position)
                down.consume()
                while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                    if (!change.pressed) break
                    set(change.position)
                    change.consume()
                }
                knob = Offset.Zero
            }
        }
    ) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val reach = size.width / 2f * 0.7f
        drawLine(ring, Offset(c.x, 10.dp.toPx()), Offset(c.x, size.height - 10.dp.toPx()), 1.dp.toPx())
        drawLine(ring, Offset(10.dp.toPx(), c.y), Offset(size.width - 10.dp.toPx(), c.y), 1.dp.toPx())
        drawCircle(ring, radius = reach, center = c, style = Stroke(1.dp.toPx()))
        drawCircle(if (colors.isDark) Color(0xFFCCCCCC) else Color(0xFF666666), radius = 15.dp.toPx(), center = c + knob * reach)
    }
}

/**
 * The logo settings of the Create page: the one logo the bill header uses (the small or the wide one, as the header
 * style says), with its place and size on the invoice and on the receipt, each set apart. Shown only when that logo
 * exists. Stored in the print settings (the database and the cloud sync).
 */
@Composable
fun LogoPlaceSection(profile: BusinessProfile, isFactory: Boolean, colors: ShellColors, onSave: (BusinessProfile) -> Unit) {
    val wide = profile.headerLayout == "wide" && profile.wideLogo.isNotEmpty()
    val path = if (wide) profile.wideLogo else profile.logo
    if (path.isEmpty()) return
    var editing by remember { mutableStateOf<String?>(null) }
    var temp by remember { mutableStateOf(LogoPlace()) }
    val rows = listOf("invoice" to K.invoiceLogoPlace, "receipt" to K.receiptLogoPlace)

    ElvanSettingsSection(colors = colors) {
        rows.forEachIndexed { i, (doc, title) ->
            if (i > 0) ElvanSettingsDivider(colors = colors)
            val receipt = doc == "receipt"
            val prefix = when {
                receipt && wide -> "receiptWideLogo"
                receipt -> "receiptLogo"
                wide -> "wideLogo"
                else -> "logo"
            }
            // A receipt without a place of its own prints the invoice's wide place, so that is where it starts
            val fallback = if (receipt && wide) "wideLogo" else null
            val saved = remember(profile.printSettingsJson, prefix, fallback) { LogoPlace.readEffective(profile.printSettingsJson, prefix, fallback) }
            ElvanSettingsAnimatedExpand(
                isEditing = editing == doc,
                displayContent = {
                    ElvanSettingsDisplayRow(
                        title = title.tr(),
                        primaryValue = "${(saved.scale * 100f).roundToInt()}%",
                        onEdit = { temp = saved; editing = doc },
                        colors = colors
                    )
                },
                editContent = {
                    ElvanSettingsEditContainer(
                        title = title.tr(),
                        onCancel = { editing = null },
                        onSave = {
                            val updated = profile.copy()
                            updated.printSettingsJson = temp.writeTo(updated.printSettingsJson, prefix, explicit = receipt)
                            onSave(updated)
                            editing = null
                        },
                        colors = colors
                    ) {
                        LogoAdjuster(
                            remember(path, receipt, wide) { logoPreviewPayload(profile, wide, path, isFactory, receipt) },
                            prefix, temp, saved, { temp = it }, (if (wide) K.wideLogoSize else K.logoSize).tr(), colors
                        )
                    }
                }
            )
        }
    }
}
