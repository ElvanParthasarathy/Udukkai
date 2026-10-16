import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.elvan.udukkai.App
import com.elvan.udukkai.core.mode.AppLaunchConfig
import com.elvan.udukkai.core.mode.AppMode

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        System.err.println("UNCAUGHT on ${thread.name}: ${throwable.message}")
        throwable.printStackTrace()
    }

    println(">>> Starting Udukkai Textiles Desktop App (Fixed PATTU mode)...")

    // Lock application to Textiles / PATTU mode permanently before Compose tree mounts
    AppLaunchConfig.setFixedMode(AppMode.PATTU)

    application {
        com.elvan.udukkai.localization.LanguageManager.init()
        com.elvan.udukkai.theme.ThemeManager.init()
        com.elvan.udukkai.theme.FontManager.init()
        com.elvan.udukkai.core.auth.AuthManager.init()

        Window(
            onCloseRequest = ::exitApplication,
            title = "Udukkai Textiles",
            icon = BitmapPainter(useResource("udukkai_icon.png", ::loadImageBitmap)),
            state = rememberWindowState(
                width = 1180.dp,
                height = 800.dp,
                position = WindowPosition.Aligned(Alignment.Center)
            )
        ) {
            App()
        }
    }
}
