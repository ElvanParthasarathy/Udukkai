package com.elvan.udukkai.ui.navigation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import com.composables.icons.materialsymbols.MaterialSymbols as LibSymbols
import com.composables.icons.materialsymbols.rounded.*
import com.composables.icons.materialsymbols.roundedfilled.*

/**
 * MaterialSymbols — Google Material Symbols (new) Rounded icons.
 * Provides dynamic filled/outline support:
 * - Inactive navigation items and interactive edit actions use Rounded Outline.
 * - Active navigation items, settings categories, and entity badges use Rounded Filled.
 */
object MaterialSymbols {
    private fun symbol(name: String, pathData: String): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).addPath(
            fill = SolidColor(Color.Black),
            pathData = PathParser().parsePathString(pathData).toNodes()
        ).build()
    }

    private fun symbol24(name: String, pathData: String): ImageVector {
        return ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).addPath(
            fill = SolidColor(Color.Black),
            pathData = PathParser().parsePathString(pathData).toNodes()
        ).build()
    }

    // ── Invoice Icons (Google Material Symbols Rounded receipt_long) ──
    private val InvoiceOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "receipt_long_outline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.Companion.NonZero,
            ) {
                moveTo(6f, 22f)
                quadTo(4.75f, 22f, 3.88f, 21.13f)
                reflectiveQuadTo(3f, 19f)
                verticalLineTo(17f)
                quadTo(3f, 16.58f, 3.29f, 16.29f)
                reflectiveQuadTo(4f, 16f)
                horizontalLineTo(6f)
                verticalLineTo(2.6f)
                quadTo(6f, 2.42f, 6.15f, 2.36f)
                reflectiveQuadTo(6.43f, 2.42f)
                lineTo(7.15f, 3.15f)
                quadTo(7.3f, 3.3f, 7.5f, 3.3f)
                reflectiveQuadTo(7.85f, 3.15f)
                lineToRelative(0.8f, -0.8f)
                quadTo(8.8f, 2.2f, 9f, 2.2f)
                reflectiveQuadTo(9.35f, 2.35f)
                lineToRelative(0.8f, 0.8f)
                quadTo(10.3f, 3.3f, 10.5f, 3.3f)
                reflectiveQuadTo(10.85f, 3.15f)
                lineToRelative(0.8f, -0.8f)
                quadTo(11.8f, 2.2f, 12f, 2.2f)
                reflectiveQuadToRelative(0.35f, 0.15f)
                lineToRelative(0.8f, 0.8f)
                quadTo(13.3f, 3.3f, 13.5f, 3.3f)
                reflectiveQuadTo(13.85f, 3.15f)
                lineToRelative(0.8f, -0.8f)
                quadTo(14.8f, 2.2f, 15f, 2.2f)
                reflectiveQuadToRelative(0.35f, 0.15f)
                lineToRelative(0.8f, 0.8f)
                quadTo(16.3f, 3.3f, 16.5f, 3.3f)
                reflectiveQuadTo(16.85f, 3.15f)
                lineToRelative(0.8f, -0.8f)
                quadTo(17.8f, 2.2f, 18f, 2.2f)
                reflectiveQuadToRelative(0.35f, 0.15f)
                lineToRelative(0.8f, 0.8f)
                quadTo(19.3f, 3.3f, 19.5f, 3.3f)
                reflectiveQuadTo(19.85f, 3.15f)
                lineTo(20.58f, 2.42f)
                quadTo(20.7f, 2.3f, 20.85f, 2.36f)
                reflectiveQuadTo(21f, 2.6f)
                verticalLineTo(19f)
                quadToRelative(0f, 1.25f, -0.88f, 2.13f)
                reflectiveQuadTo(18f, 22f)
                horizontalLineTo(6f)
                close()
                moveTo(18f, 20f)
                quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                quadTo(19f, 19.43f, 19f, 19f)
                verticalLineTo(5f)
                horizontalLineTo(8f)
                verticalLineTo(16f)
                horizontalLineToRelative(8f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(17f, 17f)
                verticalLineToRelative(2f)
                quadToRelative(0f, 0.43f, 0.29f, 0.71f)
                reflectiveQuadTo(18f, 20f)
                close()
                moveTo(10f, 7f)
                horizontalLineToRelative(4f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(15f, 8f)
                quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                reflectiveQuadTo(14f, 9f)
                horizontalLineTo(10f)
                quadTo(9.58f, 9f, 9.29f, 8.71f)
                reflectiveQuadTo(9f, 8f)
                quadTo(9f, 7.57f, 9.29f, 7.29f)
                quadTo(9.58f, 7f, 10f, 7f)
                close()
                moveToRelative(0f, 3f)
                horizontalLineToRelative(4f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(15f, 11f)
                reflectiveQuadToRelative(-0.29f, 0.71f)
                reflectiveQuadTo(14f, 12f)
                horizontalLineTo(10f)
                quadTo(9.58f, 12f, 9.29f, 11.71f)
                quadTo(9f, 11.43f, 9f, 11f)
                reflectiveQuadTo(9.29f, 10.29f)
                quadTo(9.58f, 10f, 10f, 10f)
                close()
                moveTo(17f, 9f)
                quadTo(16.58f, 9f, 16.29f, 8.71f)
                reflectiveQuadTo(16f, 8f)
                quadTo(16f, 7.57f, 16.29f, 7.29f)
                reflectiveQuadTo(17f, 7f)
                reflectiveQuadToRelative(0.71f, 0.29f)
                reflectiveQuadTo(18f, 8f)
                quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                reflectiveQuadTo(17f, 9f)
                close()
                moveToRelative(0f, 3f)
                quadToRelative(-0.43f, 0f, -0.71f, -0.29f)
                quadTo(16f, 11.43f, 16f, 11f)
                reflectiveQuadToRelative(0.29f, -0.71f)
                reflectiveQuadTo(17f, 10f)
                reflectiveQuadToRelative(0.71f, 0.29f)
                reflectiveQuadTo(18f, 11f)
                reflectiveQuadToRelative(-0.29f, 0.71f)
                reflectiveQuadTo(17f, 12f)
                close()
                moveTo(6f, 20f)
                horizontalLineToRelative(9f)
                verticalLineTo(18f)
                horizontalLineTo(5f)
                verticalLineToRelative(1f)
                quadToRelative(0f, 0.43f, 0.29f, 0.71f)
                reflectiveQuadTo(6f, 20f)
                close()
                moveTo(5f, 20f)
                quadToRelative(0f, 0f, 0f, -0.29f)
                quadTo(5f, 19.43f, 5f, 19f)
                verticalLineTo(18f)
                verticalLineToRelative(2f)
                close()
            }
        }.build()
    }

    private val InvoiceFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "receipt_long_fill",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.Companion.NonZero,
            ) {
                moveTo(6f, 22f)
                quadTo(4.75f, 22f, 3.88f, 21.13f)
                reflectiveQuadTo(3f, 19f)
                verticalLineTo(17f)
                quadTo(3f, 16.58f, 3.29f, 16.29f)
                reflectiveQuadTo(4f, 16f)
                horizontalLineTo(6f)
                verticalLineTo(2.6f)
                quadTo(6f, 2.42f, 6.15f, 2.36f)
                reflectiveQuadTo(6.43f, 2.42f)
                lineTo(7.15f, 3.15f)
                quadTo(7.3f, 3.3f, 7.5f, 3.3f)
                reflectiveQuadTo(7.85f, 3.15f)
                lineToRelative(0.8f, -0.8f)
                quadTo(8.8f, 2.2f, 9f, 2.2f)
                reflectiveQuadTo(9.35f, 2.35f)
                lineToRelative(0.8f, 0.8f)
                quadTo(10.3f, 3.3f, 10.5f, 3.3f)
                reflectiveQuadTo(10.85f, 3.15f)
                lineToRelative(0.8f, -0.8f)
                quadTo(11.8f, 2.2f, 12f, 2.2f)
                reflectiveQuadToRelative(0.35f, 0.15f)
                lineToRelative(0.8f, 0.8f)
                quadTo(13.3f, 3.3f, 13.5f, 3.3f)
                reflectiveQuadTo(13.85f, 3.15f)
                lineToRelative(0.8f, -0.8f)
                quadTo(14.8f, 2.2f, 15f, 2.2f)
                reflectiveQuadToRelative(0.35f, 0.15f)
                lineToRelative(0.8f, 0.8f)
                quadTo(16.3f, 3.3f, 16.5f, 3.3f)
                reflectiveQuadTo(16.85f, 3.15f)
                lineToRelative(0.8f, -0.8f)
                quadTo(17.8f, 2.2f, 18f, 2.2f)
                reflectiveQuadToRelative(0.35f, 0.15f)
                lineToRelative(0.8f, 0.8f)
                quadTo(19.3f, 3.3f, 19.5f, 3.3f)
                reflectiveQuadTo(19.85f, 3.15f)
                lineTo(20.58f, 2.42f)
                quadTo(20.7f, 2.3f, 20.85f, 2.36f)
                reflectiveQuadTo(21f, 2.6f)
                verticalLineTo(19f)
                quadToRelative(0f, 1.25f, -0.88f, 2.13f)
                reflectiveQuadTo(18f, 22f)
                horizontalLineTo(6f)
                close()
                moveTo(18f, 20f)
                quadToRelative(0.43f, 0f, 0.71f, -0.29f)
                quadTo(19f, 19.43f, 19f, 19f)
                verticalLineTo(5f)
                horizontalLineTo(8f)
                verticalLineTo(16f)
                horizontalLineToRelative(8f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(17f, 17f)
                verticalLineToRelative(2f)
                quadToRelative(0f, 0.43f, 0.29f, 0.71f)
                reflectiveQuadTo(18f, 20f)
                close()
                moveTo(10f, 7f)
                horizontalLineToRelative(4f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(15f, 8f)
                quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                reflectiveQuadTo(14f, 9f)
                horizontalLineTo(10f)
                quadTo(9.58f, 9f, 9.29f, 8.71f)
                reflectiveQuadTo(9f, 8f)
                quadTo(9f, 7.57f, 9.29f, 7.29f)
                quadTo(9.58f, 7f, 10f, 7f)
                close()
                moveToRelative(0f, 3f)
                horizontalLineToRelative(4f)
                quadToRelative(0.43f, 0f, 0.71f, 0.29f)
                reflectiveQuadTo(15f, 11f)
                reflectiveQuadToRelative(-0.29f, 0.71f)
                reflectiveQuadTo(14f, 12f)
                horizontalLineTo(10f)
                quadTo(9.58f, 12f, 9.29f, 11.71f)
                quadTo(9f, 11.43f, 9f, 11f)
                reflectiveQuadTo(9.29f, 10.29f)
                quadTo(9.58f, 10f, 10f, 10f)
                close()
                moveTo(17f, 9f)
                quadTo(16.58f, 9f, 16.29f, 8.71f)
                reflectiveQuadTo(16f, 8f)
                quadTo(16f, 7.57f, 16.29f, 7.29f)
                reflectiveQuadTo(17f, 7f)
                reflectiveQuadToRelative(0.71f, 0.29f)
                reflectiveQuadTo(18f, 8f)
                quadToRelative(0f, 0.42f, -0.29f, 0.71f)
                reflectiveQuadTo(17f, 9f)
                close()
                moveToRelative(0f, 3f)
                quadToRelative(-0.43f, 0f, -0.71f, -0.29f)
                quadTo(16f, 11.43f, 16f, 11f)
                reflectiveQuadToRelative(0.29f, -0.71f)
                reflectiveQuadTo(17f, 10f)
                reflectiveQuadToRelative(0.71f, 0.29f)
                reflectiveQuadTo(18f, 11f)
                reflectiveQuadToRelative(-0.29f, 0.71f)
                reflectiveQuadTo(17f, 12f)
                close()
            }
        }.build()
    }

    // ── Receipt Icons (Phosphor SVG custom path & line vectors) ──
    private val ReceiptOutline: ImageVector by lazy {
        ImageVector.Builder(
            name = "ReceiptOutline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).addPath(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 16f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathData = PathParser().parsePathString("M80,104L176,104").toNodes()
        ).addPath(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 16f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathData = PathParser().parsePathString("M80,136L176,136").toNodes()
        ).addPath(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 16f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
            pathData = PathParser().parsePathString("M32,208V56a8,8,0,0,1,8-8H216a8,8,0,0,1,8,8V208l-32-16-32,16-32-16L96,208,64,192Z").toNodes()
        ).build()
    }

    private val ReceiptFilled: ImageVector by lazy {
        ImageVector.Builder(
            name = "ReceiptFilled",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 256f,
            viewportHeight = 256f
        ).addPath(
            fill = SolidColor(Color.Black),
            pathData = PathParser().parsePathString(
                "M216,40H40A16,16,0,0,0,24,56V208a8,8,0,0,0,11.58,7.15L64,200.94l28.42,14.21a8,8,0,0,0,7.16,0L128,200.94l28.42,14.21a8,8,0,0,0,7.16,0L192,200.94l28.42,14.21A8,8,0,0,0,232,208V56A16,16,0,0,0,216,40ZM176,144H80a8,8,0,0,1,0-16h96a8,8,0,0,1,0,16Zm0-32H80a8,8,0,0,1,0-16h96a8,8,0,0,1,0,16Z"
            ).toNodes()
        ).build()
    }

    object Rounded {
        // ── Navigation Dynamic Fill (Inactive = Outline, Active = Filled) ──
        val Home: ImageVector get() = LibSymbols.Rounded.Home
        val HomeFill: ImageVector get() = LibSymbols.RoundedFilled.Home

        // Invoices use Google Material Symbols Rounded receipt_long
        val Invoice: ImageVector get() = InvoiceOutline
        val InvoiceFill: ImageVector get() = InvoiceFilled

        // Receipts use Phosphor receipt outline and fill
        val Receipt: ImageVector get() = ReceiptOutline
        val ReceiptFill: ImageVector get() = ReceiptFilled

        // Aliases for backwards compatibility & seamless migration
        val Description: ImageVector get() = Invoice
        val DescriptionFill: ImageVector get() = InvoiceFill
        val Notes: ImageVector get() = Invoice
        val NotesFill: ImageVector get() = InvoiceFill
        val ReceiptLong: ImageVector get() = Invoice
        val ReceiptLongFill: ImageVector get() = InvoiceFill
        val Schedule: ImageVector get() = LibSymbols.Rounded.Schedule
        val ScheduleFill: ImageVector get() = LibSymbols.RoundedFilled.Schedule
        val Calendar: ImageVector get() = LibSymbols.Rounded.Calendar_month
        val CalendarFill: ImageVector get() = LibSymbols.RoundedFilled.Calendar_month
        val Notifications: ImageVector get() = LibSymbols.Rounded.Notifications
        val NotificationsFill: ImageVector get() = LibSymbols.RoundedFilled.Notifications

        // ── Action & UI Controls (Line/Outline for clean buttons) ──
        // Neram's exact Chevron back button SVG path
        val ArrowBack: ImageVector by lazy {
            symbol("ArrowBack", "M390.13,480L680.3,770.17Q696.74,786.61 696.36,809Q695.98,831.39 679.3,848.07Q662.87,864.5 640.48,864.5Q618.09,864.5 601.65,848.07L297.24,544.65Q283.57,530.98 276.85,514.07Q270.13,497.15 270.13,480Q270.13,462.85 276.85,445.93Q283.57,429.02 297.24,415.35L601.65,111.17Q618.09,94.74 640.86,95Q663.63,95.26 680.3,111.93Q696.74,128.37 696.74,150.88Q696.74,173.39 680.3,189.83L390.13,480Z")
        }
        val ArrowForward: ImageVector get() = LibSymbols.Rounded.Arrow_forward
        val Visibility: ImageVector get() = LibSymbols.Rounded.Visibility
        val VisibilityOff: ImageVector get() = LibSymbols.Rounded.Visibility_off
        val Close: ImageVector get() = LibSymbols.Rounded.Close
        val Cancel: ImageVector get() = LibSymbols.RoundedFilled.Cancel
        val Check: ImageVector get() = LibSymbols.Rounded.Check
        val Add: ImageVector get() = LibSymbols.Rounded.Add
        val Search: ImageVector get() = LibSymbols.Rounded.Search
        val FilterList: ImageVector by lazy {
            symbol24(
                "FilterList",
                "M10,18h4c0.55,0,1-0.45,1-1s-0.45-1-1-1h-4c-0.55,0-1,0.45-1,1S9.45,18,10,18z M3,7h18c0.55,0,1-0.45,1-1s-0.45-1-1-1H3C2.45,5,2,5.45,2,6S2.45,7,3,7z M6,13h12c0.55,0,1-0.45,1-1s-0.45-1-1-1H6c-0.55,0-1,0.45-1,1S5.45,13,6,13z"
            )
        }
        val ChevronRight: ImageVector get() = LibSymbols.Rounded.Keyboard_arrow_right
        val KeyboardArrowDown: ImageVector get() = LibSymbols.Rounded.Keyboard_arrow_down
        val MoreVert: ImageVector get() = LibSymbols.Rounded.More_vert
        val Edit: ImageVector get() = LibSymbols.Rounded.Edit
        val EditFill: ImageVector get() = LibSymbols.RoundedFilled.Edit
        val SwapHoriz: ImageVector get() = LibSymbols.Rounded.Swap_horiz
        val Handyman: ImageVector get() = LibSymbols.Rounded.Handyman
        val CalendarToday: ImageVector get() = LibSymbols.Rounded.Calendar_today
        val AddCircle: ImageVector get() = LibSymbols.Rounded.Add_circle
        val CheckCircleFill: ImageVector get() = LibSymbols.RoundedFilled.Check_circle
        val RadioButtonUnchecked: ImageVector get() = LibSymbols.Rounded.Radio_button_unchecked
        val CheckBox: ImageVector get() = LibSymbols.Rounded.Check_box
        val CheckBoxOutlineBlank: ImageVector get() = LibSymbols.Rounded.Check_box_outline_blank
        val Restore: ImageVector get() = LibSymbols.Rounded.Settings_backup_restore
        val ContentCopy: ImageVector get() = LibSymbols.Rounded.Content_copy
        val ContentPaste: ImageVector get() = LibSymbols.Rounded.Content_paste

        // ── Settings Categories & Badges (Filled / Solid as per old Flutter design) ──
        val BusinessCenter: ImageVector get() = LibSymbols.RoundedFilled.Business_center
        val Apartment: ImageVector get() = LibSymbols.RoundedFilled.Apartment
        val CurrencyRupee: ImageVector get() = LibSymbols.RoundedFilled.Currency_rupee
        val CurrencyRupeeCircle: ImageVector get() = LibSymbols.RoundedFilled.Currency_rupee_circle
        val LocationOn: ImageVector get() = LibSymbols.RoundedFilled.Location_on
        val CreditCard: ImageVector get() = LibSymbols.RoundedFilled.Credit_card
        val Payments: ImageVector get() = LibSymbols.RoundedFilled.Payments
        val QrCode: ImageVector get() = LibSymbols.RoundedFilled.Qr_code
        val AccountBalance: ImageVector get() = LibSymbols.RoundedFilled.Account_balance
        val Person: ImageVector get() = LibSymbols.RoundedFilled.Person
        val LightMode: ImageVector get() = LibSymbols.RoundedFilled.Light_mode
        val DarkMode: ImageVector get() = LibSymbols.RoundedFilled.Dark_mode
        val Translate: ImageVector get() = LibSymbols.RoundedFilled.Translate
        val Folder: ImageVector get() = LibSymbols.RoundedFilled.Folder
        val Lock: ImageVector get() = LibSymbols.RoundedFilled.Lock
        val Code: ImageVector get() = LibSymbols.RoundedFilled.Code
        val Info: ImageVector get() = LibSymbols.RoundedFilled.Info
        val AutoAwesome: ImageVector get() = LibSymbols.RoundedFilled.Auto_awesome
        val Palette: ImageVector get() = LibSymbols.RoundedFilled.Palette
        val Storage: ImageVector get() = LibSymbols.RoundedFilled.Storage
        val Inventory2: ImageVector get() = LibSymbols.RoundedFilled.Inventory_2
        val Email: ImageVector get() = LibSymbols.RoundedFilled.Mail
        val Delete: ImageVector get() = LibSymbols.RoundedFilled.Delete
        val DeleteForever: ImageVector get() = LibSymbols.RoundedFilled.Delete_forever
        val CloudUpload: ImageVector get() = LibSymbols.RoundedFilled.Cloud_upload
        val Backup: ImageVector get() = LibSymbols.RoundedFilled.Backup
        val Sync: ImageVector get() = LibSymbols.RoundedFilled.Sync
        val Logout: ImageVector get() = LibSymbols.RoundedFilled.Logout
        val Settings: ImageVector get() = LibSymbols.RoundedFilled.Settings
        val EventList: ImageVector get() = LibSymbols.RoundedFilled.List
        val BrokenImage: ImageVector get() = LibSymbols.Rounded.Broken_image
        val PhotoLibrary: ImageVector get() = LibSymbols.Rounded.Photo_library
        val FolderOpen: ImageVector get() = LibSymbols.Rounded.Folder_open
        val Call: ImageVector get() = LibSymbols.Rounded.Call
        val Percent: ImageVector get() = LibSymbols.Rounded.Percent
        val Straighten: ImageVector get() = LibSymbols.Rounded.Straighten
        val LocalShipping: ImageVector get() = LibSymbols.Rounded.Local_shipping
    }

    /**
     * Mode icons copied 1:1 from Flutter's `AppSvgs` (`seyali_oaviyangal.dart`).
     */
    object Mode {
        val Coolie: ImageVector by lazy {
            ImageVector.Builder(
                name = "CoolieMode",
                defaultWidth = 32.dp,
                defaultHeight = 32.dp,
                viewportWidth = 256f,
                viewportHeight = 256f
            ).addPath(
                fill = SolidColor(Color.White),
                pathData = PathParser().parsePathString(
                    "M128.09,57.38a36,36,0,0,1,55.17-27.82,4,4,0,0,1-.56,7A52.06,52.06,0,0,0,152,84c0,1.17,0,2.34.12,3.49a4,4,0,0,1-6,3.76A36,36,0,0,1,128.09,57.38ZM240,160.61a24.47,24.47,0,0,1-13.6,22l-.44.2-38.83,16.54a6.94,6.94,0,0,1-1.19.4l-64,16A7.93,7.93,0,0,1,120,216H16A16,16,0,0,1,0,200V160a16,16,0,0,1,16-16H44.69l22.62-22.63A31.82,31.82,0,0,1,89.94,112H140a28,28,0,0,1,27.25,34.45l41.84-9.62A24.61,24.61,0,0,1,240,160.61Zm-16,0a8.61,8.61,0,0,0-10.87-8.3l-.31.08-67,15.41a8.32,8.32,0,0,1-1.79.2H112a8,8,0,0,1,0-16h28a12,12,0,0,0,0-24H89.94a15.86,15.86,0,0,0-11.31,4.69L56,155.31V200h63l62.43-15.61,38-16.18A8.56,8.56,0,0,0,224,160.61ZM168,84a36,36,0,1,0,36-36A36,36,0,0,0,168,84Z"
                ).toNodes()
            ).build()
        }

        val Silk: ImageVector by lazy {
            ImageVector.Builder(
                name = "SilkMode",
                defaultWidth = 32.dp,
                defaultHeight = 32.dp,
                viewportWidth = 256f,
                viewportHeight = 256f
            ).addPath(
                fill = SolidColor(Color.White),
                pathData = PathParser().parsePathString(
                    "M28,128a8,8,0,0,1,0-16H56a8,8,0,0,0,0-16H40a24,24,0,0,1,0-48,8,8,0,0,1,16,0h8a8,8,0,0,1,0,16H40a8,8,0,0,0,0,16H56a24,24,0,0,1,0,48,8,8,0,0,1-16,0ZM224,48H96a8,8,0,0,0,0,16H216V96H104a8,8,0,0,0,0,16h56v32H80a8,8,0,0,0,0,16h80v32H40V152a8,8,0,0,0-16,0v40a16,16,0,0,0,16,16H216a16,16,0,0,0,16-16V56A8,8,0,0,0,224,48Z"
                ).toNodes()
            ).build()
        }
    }

    /**
     * Custom Phosphor Navigation Bar SVG Icons matching Flutter 1:1.
     */
    object CustomNav {
        private fun navSymbol(name: String, pathData: String): ImageVector {
            return ImageVector.Builder(
                name = name,
                defaultWidth = 32.dp,
                defaultHeight = 32.dp,
                viewportWidth = 256f,
                viewportHeight = 256f
            ).addPath(
                fill = SolidColor(Color.Black),
                pathData = PathParser().parsePathString(pathData).toNodes()
            ).build()
        }

        // Tab 0: Home (Mugappu)
        val Home: ImageVector by lazy {
            navSymbol("Home", "M219.31,108.68l-80-80a16,16,0,0,0-22.62,0l-80,80A15.87,15.87,0,0,0,32,120v96a8,8,0,0,0,8,8h64a8,8,0,0,0,8-8V160h32v56a8,8,0,0,0,8,8h64a8,8,0,0,0,8-8V120A15.87,15.87,0,0,0,219.31,108.68ZM208,208H160V152a8,8,0,0,0-8-8H104a8,8,0,0,0-8,8v56H48V120l80-80,80,80Z")
        }
        val HomeFill: ImageVector by lazy {
            navSymbol("HomeFill", "M224,120v96a8,8,0,0,1-8,8H160a8,8,0,0,1-8-8V164a4,4,0,0,0-4-4H108a4,4,0,0,0-4,4v52a8,8,0,0,1-8,8H40a8,8,0,0,1-8-8V120a16,16,0,0,1,4.69-11.31l80-80a16,16,0,0,1,22.62,0l80,80A16,16,0,0,1,224,120Z")
        }

        // Tab 1: Create (Uruvaakku / Plus App - CupertinoIcons.plus_app / plus_app_fill 0xF775 / 0xF776)
        val Create: ImageVector by lazy {
            navSymbol("Create", "M217.9620253164557 38.82025316455696Q231.0 51.85822784810125 231.0 85.23544303797468V170.76455696202532Q231.0 204.14177215189872 217.9620253164557 217.17974683544304Q204.4025316455696 230.7392405063291 171.54683544303796 230.7392405063291H84.45316455696202Q51.597468354430376 230.7392405063291 38.037974683544306 217.17974683544304Q25.0 204.14177215189872 25.0 170.76455696202532V84.7139240506329Q25.0 51.85822784810125 38.037974683544306 38.82025316455696Q51.597468354430376 25.26075949367089 84.45316455696202 25.26075949367089H171.54683544303796Q204.4025316455696 25.26075949367089 217.9620253164557 38.82025316455696ZM213.26835443037976 82.10632911392403Q213.26835443037976 60.20253164556962 204.92405063291142 51.858227848101265Q196.57974683544305 43.51392405063291 174.6759493670886 43.51392405063291H81.84556962025317Q59.42025316455696 43.51392405063291 51.07594936708861 51.858227848101265Q42.731645569620255 60.20253164556962 42.731645569620255 82.10632911392403V173.89367088607594Q42.731645569620255 195.79746835443038 51.07594936708861 204.14177215189875Q59.42025316455696 212.4860759493671 81.3240506329114 212.4860759493671H174.6759493670886Q196.57974683544305 212.4860759493671 204.92405063291142 204.14177215189875Q213.26835443037976 195.79746835443038 213.26835443037976 173.89367088607594ZM128.2607594936709 187.453164556962Q119.91645569620253 187.453164556962 119.91645569620253 179.63037974683544V135.82278481012656H76.63037974683544Q72.97974683544304 135.82278481012656 70.63291139240506 133.4759493670886Q68.28607594936709 131.1291139240506 68.28607594936709 127.99999999999999Q68.28607594936709 124.87088607594936 70.63291139240506 122.52405063291138Q72.97974683544304 120.17721518987341 76.63037974683544 120.17721518987341H119.91645569620253V76.36962025316456Q119.91645569620253 68.02531645569618 128.2607594936709 68.02531645569618Q136.08354430379745 68.02531645569618 136.08354430379745 76.36962025316456V120.17721518987341H179.89113924050633Q187.71392405063293 120.17721518987341 187.71392405063293 127.99999999999999Q187.71392405063293 135.82278481012656 179.89113924050633 135.82278481012656H136.08354430379745V179.63037974683544Q136.08354430379745 187.453164556962 128.2607594936709 187.453164556962Z")
        }
        val CreateFill: ImageVector by lazy {
            navSymbol("CreateFill", "M231.0 85.21538461538461V170.7846153846154Q231.0 204.5897435897436 217.7948717948718 217.7948717948718Q204.5897435897436 231.0 170.7846153846154 231.0H85.21538461538461Q51.41025641025641 231.0 38.205128205128204 217.7948717948718Q25.0 204.5897435897436 25.0 170.7846153846154V85.21538461538461Q25.0 51.41025641025641 38.205128205128204 38.205128205128204Q51.41025641025641 25.0 85.21538461538461 25.0H170.7846153846154Q204.5897435897436 25.0 217.7948717948718 38.205128205128204Q231.0 51.41025641025641 231.0 85.21538461538461ZM136.9794871794872 185.57435897435897V136.9794871794872H185.57435897435897Q189.27179487179487 136.9794871794872 191.91282051282053 134.33846153846156Q194.55384615384617 131.69743589743592 194.55384615384617 128.0Q194.55384615384617 124.3025641025641 191.91282051282053 121.66153846153847Q189.27179487179487 119.02051282051282 185.57435897435897 119.02051282051282H136.9794871794872V70.42564102564103Q136.9794871794872 61.44615384615386 128.0 61.44615384615386Q124.3025641025641 61.44615384615386 121.66153846153847 64.0871794871795Q119.02051282051282 66.72820512820513 119.02051282051282 70.42564102564103V119.02051282051282H70.42564102564103Q66.72820512820513 119.02051282051282 64.0871794871795 121.66153846153847Q61.44615384615385 124.3025641025641 61.44615384615385 128.0Q61.44615384615385 131.69743589743592 64.0871794871795 134.33846153846156Q66.72820512820513 136.9794871794872 70.42564102564103 136.9794871794872H119.02051282051282V185.57435897435897Q119.02051282051282 189.2717948717949 121.66153846153847 191.91282051282053Q124.3025641025641 194.55384615384617 128.0 194.55384615384617Q131.6974358974359 194.55384615384617 134.33846153846156 191.91282051282053Q136.9794871794872 189.2717948717949 136.9794871794872 185.57435897435897Z")
        }

        // Tab 2: Products (Porul / Cube Box - CupertinoIcons.cube_box / cube_box_fill 0xF61B / 0xF61C)
        val Products: ImageVector by lazy {
            navSymbol("Products", "M32.358974358974365 196.71794871794873Q16.461538461538467 188.0 16.461538461538467 169.02564102564105V81.84615384615387Q16.461538461538467 65.43589743589746 30.307692307692314 57.74358974358978L107.74358974358974 13.641025641025664Q127.74358974358974 2.3589743589743932 147.74358974358972 13.641025641025664L225.69230769230768 57.74358974358978Q239.53846153846155 65.43589743589746 239.53846153846155 81.84615384615387V169.02564102564105Q239.53846153846155 188.0 223.64102564102564 196.71794871794873L137.4871794871795 245.43589743589743Q127.74358974358974 250.56410256410257 118.51282051282051 245.43589743589743ZM179.53846153846155 89.02564102564105 211.8461538461538 71.0769230769231 140.05128205128204 30.051282051282072Q127.74358974358974 23.384615384615415 115.94871794871794 30.051282051282072L95.43589743589743 41.33333333333337ZM127.74358974358974 118.25641025641028 161.0769230769231 99.79487179487181 76.97435897435898 52.102564102564116 44.15384615384616 71.0769230769231ZM42.61538461538462 181.33333333333334 118.51282051282051 224.4102564102564V134.66666666666669L34.410256410256416 86.46153846153848V168.51282051282053Q34.410256410256416 176.71794871794873 42.61538461538462 181.33333333333334ZM213.38461538461536 181.33333333333334Q221.5897435897436 176.71794871794873 221.5897435897436 168.51282051282053V86.46153846153848L136.97435897435895 134.66666666666669V224.4102564102564Z")
        }
        val ProductsFill: ImageVector by lazy {
            navSymbol("ProductsFill", "M190.57510729613733 86.28326180257511 85.5107296137339 26.540772532188868 107.65665236051503 13.665236051502177Q116.92703862660944 8.000000000000028 127.74248927038627 8.000000000000028Q138.5579399141631 8.000000000000028 147.8283261802575 13.665236051502177L226.11158798283262 58.47210300429185Q230.23175965665234 61.047210300429214 231.77682403433477 62.59227467811161ZM127.74248927038626 121.8197424892704 24.223175965665238 62.59227467811161Q26.283261802575108 61.047210300429214 29.888412017167383 58.47210300429185L70.57510729613733 35.296137339055804L175.1244635193133 95.03862660944208ZM135.46781115879827 248.0V134.6952789699571L238.9871244635193 75.4678111587983Q240.01716738197425 80.61802575107299 240.01716738197425 84.22317596566526V170.23175965665237Q240.01716738197425 189.2875536480687 224.05150214592274 198.04291845493563L137.52789699570815 246.96995708154506ZM120.53218884120172 248.0 118.47210300429184 246.96995708154506 31.948497854077253 198.04291845493563Q15.982832618025753 189.2875536480687 15.982832618025753 170.23175965665237V84.22317596566526Q15.982832618025753 80.61802575107299 17.01287553648069 75.4678111587983L120.53218884120172 134.6952789699571Z")
        }

        // Tab 3: Customers (Vaangunar / Users)
        val Customers: ImageVector by lazy {
            navSymbol("Customers", "M117.25,157.92a60,60,0,1,0-66.5,0A95.83,95.83,0,0,0,3.53,195.63a8,8,0,1,0,13.4,8.74,80,80,0,0,1,134.14,0,8,8,0,0,0,13.4-8.74A95.83,95.83,0,0,0,117.25,157.92ZM40,108a44,44,0,1,1,44,44A44.05,44.05,0,0,1,40,108Zm210.14,98.7a8,8,0,0,1-11.07-2.33A79.83,79.83,0,0,0,172,168a8,8,0,0,1,0-16,44,44,0,1,0-16.34-84.87,8,8,0,1,1-5.94-14.85,60,60,0,0,1,55.53,105.64,95.83,95.83,0,0,1,47.22,37.71A8,8,0,0,1,250.14,206.7Z")
        }
        val CustomersFill: ImageVector by lazy {
            navSymbol("CustomersFill", "M164.47,195.63a8,8,0,0,1-6.7,12.37H10.23a8,8,0,0,1-6.7-12.37,95.83,95.83,0,0,1,47.22-37.71,60,60,0,1,1,66.5,0A95.83,95.83,0,0,1,164.47,195.63Zm87.91-.15a95.87,95.87,0,0,0-47.13-37.56A60,60,0,0,0,144.7,54.59a4,4,0,0,0-1.33,6A75.83,75.83,0,0,1,147,150.53a4,4,0,0,0,1.07,5.53,112.32,112.32,0,0,1,29.85,30.83,23.92,23.92,0,0,1,3.65,16.47,4,4,0,0,0,3.95,4.64h60.3a8,8,0,0,0,7.73-5.93A8.22,8.22,0,0,0,252.38,195.48Z")
        }
    }
}

/**
 * AppSvgs — Direct access mirroring Flutter's AppSvgs.
 */
object AppSvgs {
    val coolieMode: ImageVector get() = MaterialSymbols.Mode.Coolie
    val silkMode: ImageVector get() = MaterialSymbols.Mode.Silk
}
