import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.megabyte6.ninjatokenatm.App
import com.github.megabyte6.ninjatokenatm.Constants


fun main() {
    val app = App()

    application {
        val isRunning = remember { mutableStateOf(true) }
        if (!isRunning.value) exitApplication()

        Window(
            onCloseRequest = ::exitApplication,
            title = Constants.APP_NAME
        ) {
            app.MainActivity(isRunning)
        }
    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}