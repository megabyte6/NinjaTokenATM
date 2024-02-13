import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.apache.poi.EmptyFileException
import org.apache.poi.EncryptedDocumentException
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import javax.swing.JOptionPane

private val json = Json {
    encodeDefaults = true
    prettyPrint = true
}

private const val APP_NAME = "NinjaTokenATM"

lateinit var settings: Settings
lateinit var ninjaManager: NinjaManager

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val settingsFile = File("settings.json")
    if (!settingsFile.exists() || settingsFile.isDirectory) {
        settingsFile.createNewFile()
        json.encodeToStream(value = Settings(), stream = FileOutputStream(settingsFile))

        JOptionPane.showMessageDialog(
            null,
            "Please fill in all the fields in '${settingsFile.absolutePath}'",
            APP_NAME,
            JOptionPane.INFORMATION_MESSAGE
        )
        return
    }

    settings = Json.decodeFromStream<Settings>(FileInputStream("settings.json"))
    if (!validateSettings(settings)) return

    ninjaManager = NinjaManager(settings = settings)

    application {
        Window(onCloseRequest = ::exitApplication, title = APP_NAME) {
            App()
        }
    }
}

fun validateSettings(settings: Settings): Boolean {
    try {
        settings.validate()
        return true
    } catch (e: Exception) {
        when (e) {
            is FileNotFoundException,
            is EncryptedDocumentException,
            is EmptyFileException,
            is SheetNotFoundException,
            is SheetColumnNotFoundException -> e.message?.let {
                JOptionPane.showMessageDialog(null, it, APP_NAME, JOptionPane.ERROR_MESSAGE)
            }
        }
    }
    return false
}

@Preview
@Composable
fun AppDesktopPreview() {
    App()
}