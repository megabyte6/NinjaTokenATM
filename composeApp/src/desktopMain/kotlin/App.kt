import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
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


@Composable
fun App() {
    MaterialTheme {
        val focusRequester = remember { FocusRequester() }

        var rfid by remember { mutableStateOf("") }

        var ninjaName by remember { mutableStateOf("Tap your wristband!") }
        var ninjaTokens by remember { mutableStateOf(0) }

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
                    fontSize = 50.sp
                )
                Text(
                    text = ninjaTokens.toString(),
                    fontSize = 80.sp
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