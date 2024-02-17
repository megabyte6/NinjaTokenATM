package com.github.megabyte6.ninjatokenatm

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.megabyte6.ninjatokenatm.ui.theme.AppTheme
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

private val settingsFile = File("settings.json")
private var settings: Settings? = null

@Composable
fun App(isRunning: MutableState<Boolean> = mutableStateOf(true)) {
    val settings = loadSettings(settingsFile)
    if (settings == null) {
        isRunning.value = false
        return
    }

    val ninjaManager = NinjaManager(settings = settings)

    AppTheme(useDarkTheme = settings.darkTheme) {
        val focusRequester = remember { FocusRequester() }

        var rfid by remember { mutableStateOf("") }

        var ninjaName by remember { mutableStateOf(Strings.DEFAULT_STARTUP_MESSAGE) }
        var ninjaTokens by remember { mutableStateOf(0) }

        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, true)
                ) {
                    Text(
                        text = ninjaName,
                        fontSize = 50.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = ninjaTokens.toString(),
                        fontSize = 80.sp,
                    )
                }
                TextField(
                    value = rfid,
                    singleLine = true,
                    onValueChange = {
                        rfid = it

                        if (settings.rfidLength != 0 && it.length >= settings.rfidLength) {
                            ninjaName = ninjaManager.findName(rfid = rfid)
                            ninjaTokens = ninjaManager.findBalance(rfid = rfid)
                            rfid = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .focusRequester(focusRequester)
                        .onKeyEvent {
                            if (settings.rfidScannerHitsEnter && it.key == Key.Enter) {
                                ninjaName = ninjaManager.findName(rfid = rfid)
                                ninjaTokens = ninjaManager.findBalance(rfid = rfid)
                                rfid = ""
                                true
                            } else {
                                false
                            }
                        }
                )

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
fun loadSettings(settingsFile: File): Settings? {
    settings?.let { return it }

    if (!settingsFile.exists() || settingsFile.isDirectory) {
        settingsFile.createNewFile()
        json.encodeToStream(value = Settings(), stream = FileOutputStream(settingsFile))

        JOptionPane.showMessageDialog(
            null,
            "Please make sure all the fields in '${settingsFile.absolutePath}' are filled in correctly.",
            Constants.APP_NAME,
            JOptionPane.INFORMATION_MESSAGE
        )

        return null
    }

    return json.decodeFromStream<Settings>(FileInputStream(settingsFile)).takeIf { validateSettings(it) }
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
                JOptionPane.showMessageDialog(null, it, Constants.APP_NAME, JOptionPane.ERROR_MESSAGE)
            }
        }
    }
    return false
}