import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.megabyte6.ninjatokenatm.App
import com.github.megabyte6.ninjatokenatm.Constants


fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = Constants.APP_NAME
        ) {
            App()
        }
    }
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}