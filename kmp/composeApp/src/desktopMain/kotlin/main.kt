import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.elvan.udukkai.factory.FactoryApp

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        System.err.println("UNCAUGHT on ${thread.name}: ${throwable.message}")
        throwable.printStackTrace()
    }

    println(">>> Starting Udukkai Factory Desktop App (ஆலை)...")

    application {
        com.elvan.udukkai.localization.LanguageManager.init()
        com.elvan.udukkai.theme.ThemeManager.init()
        com.elvan.udukkai.theme.FontManager.init()

        Window(
            onCloseRequest = ::exitApplication,
            title = "Udukkai Factory (ஆலை)",
            icon = BitmapPainter(useResource("udukkai_icon.png", ::loadImageBitmap)),
            state = rememberWindowState(
                width = 1180.dp,
                height = 800.dp,
                position = WindowPosition.Aligned(Alignment.Center)
            )
        ) {
            FactoryApp()
        }
    }
}
