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
import kotlinx.coroutines.*
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
private val settings: Settings by lazy {
    loadSettings() ?: Settings.EMPTY
}
private val ninjaManager: NinjaManager by lazy {
    NinjaManager(settings = settings)
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun App(isRunning: MutableState<Boolean> = mutableStateOf(true)) {
    if (settings == Settings.EMPTY) isRunning.value = false
    if (!isRunning.value) return

    AppTheme(useDarkTheme = settings.darkTheme) {
        val focusRequester = remember { FocusRequester() }

        var id by remember { mutableStateOf("") }

        var ninjaName by remember { mutableStateOf(Strings.DEFAULT_STARTUP_MESSAGE) }
        var ninjaTokens by remember { mutableStateOf(0) }

        var resetNinjaNameText: Job? by remember { mutableStateOf(null) }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

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
                    value = id,
                    singleLine = true,
                    onValueChange = {
                        id = it

                        resetNinjaNameText?.cancel()
                        resetNinjaNameText = GlobalScope.launch {
                            delay(30000)
                            ninjaName = Strings.DEFAULT_STARTUP_MESSAGE
                        }

                        if (!settings.rfidScannerHitsEnter
                            && settings.rfidLength != 0
                            && it.length >= settings.rfidLength
                        ) {
                            ninjaName = ninjaManager.findNameFromRFID(rfid = id)
                            ninjaTokens = ninjaManager.findBalanceByRFID(rfid = id)
                            id = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .focusRequester(focusRequester)
                        .onKeyEvent {
                            if (settings.rfidScannerHitsEnter && it.key == Key.Enter) {
                                ninjaName = if (ninjaManager.nameExists(name = id)) {
                                    // Use this to find proper cased name.
                                    ninjaManager.findNameFromRFID(rfid = ninjaManager.findRFIDFromName(name = id))
                                } else {
                                    ninjaManager.findNameFromRFID(rfid = id)
                                }
                                ninjaTokens = if (ninjaManager.rfidExists(rfid = id)) {
                                    ninjaManager.findBalanceByRFID(rfid = id)
                                } else {
                                    ninjaManager.findBalanceByName(name = id)
                                }
                                id = ""

                                true
                            } else {
                                false
                            }
                        }
                )
            }
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
private fun loadSettings(): Settings? {
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

    return json.decodeFromStream<Settings>(FileInputStream(settingsFile)).takeIf { validateSettings(it) }
}