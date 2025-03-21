package de.mminl.interscore_remoteend

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import de.mminl.interscore_remoteend.ui.theme.InterscoreRemoteendTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent { RemoteendApp() }
	}
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteendApp() {
	var message by remember { mutableStateOf("") }
	val client = remember { OkHttpClient() }
	var webSocket: WebSocket? = null
	var port = 8081
	var changePort = remember { mutableStateOf(false) }
	var textState = remember { mutableStateOf("") }
	val keyboardController = LocalSoftwareKeyboardController.current

	LaunchedEffect(Unit) {
		val request = Request.Builder().url("ws://192.168.0.69:$port").build()
		webSocket = client.newWebSocket(request, object : WebSocketListener() {
			override fun onOpen(webSocket: WebSocket, response: Response) {
				message = "Mit Port $port verbunden!"
			}

			override fun onMessage(webSocket: WebSocket, text: String) {
				// TODO TEMP
				message = text
			}

			override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
				message = "Verbindung gescheitert!"
			}
		})
	}

	InterscoreRemoteendTheme {
		Scaffold(
			modifier = Modifier.fillMaxSize(),
			topBar = { TopAppBar(title = { Text("Interscore – Remoteend") }) },
			bottomBar = {
				BottomAppBar {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(16.dp),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(message)
						FilledTonalButton(onClick = { changePort.value = true }) {
							Text("Port ändern")
						}
					}
				}
			}
		) { _ ->
			Column(
				modifier = Modifier.fillMaxSize(),
				verticalArrangement = Arrangement.Center
			) {
				Surface(
					modifier = Modifier
						.wrapContentHeight()
						.padding(horizontal = 16.dp),
					shape = RoundedCornerShape(30.dp)
				) {
					Column(
						modifier = Modifier.wrapContentHeight(),
						verticalArrangement =
							Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
					) {
						// TODO MOVE
						var scoreboardHandle = remember { mutableStateOf(false) }
						var livetableHandle = remember { mutableStateOf(false) }
						var gameplanHandle = remember { mutableStateOf(false) }
						var gamestartHandle = remember { mutableStateOf(false) }
						var adHandle = remember { mutableStateOf(false) }

						ActionButton(text = "Scoreboard", handle = scoreboardHandle.value, onClick = {
							scoreboardHandle.value = !scoreboardHandle.value
							webSocket?.send(ByteString.of(0))
						})
						ActionButton(text = "Livetable", handle = livetableHandle.value, onClick = {
							livetableHandle.value = !livetableHandle.value
							webSocket?.send(ByteString.of(1))
						})
						ActionButton(text = "Gameplan", handle = gameplanHandle.value, onClick = {
							gameplanHandle.value = !gameplanHandle.value
							webSocket?.send(ByteString.of(2))
						})
						ActionButton(text = "Gamestart", handle = gamestartHandle.value, onClick = {
							gamestartHandle.value = !gamestartHandle.value
							webSocket?.send(ByteString.of(3))
						})
						ActionButton(text = "Ad", handle = adHandle.value, onClick = {
							adHandle.value = !adHandle.value
							webSocket?.send(ByteString.of(4))
						})
					}
				}
			}

			if (changePort.value) {
				BasicAlertDialog(onDismissRequest = { changePort.value = false }) {
					Card(
						modifier = Modifier.size(300.dp, 100.dp),
						shape = RoundedCornerShape(30.dp)
					) {
						Column(
							modifier = Modifier.fillMaxSize(),
							horizontalAlignment = Alignment.CenterHorizontally,
							verticalArrangement = Arrangement.Center
						) {
							OutlinedTextField(
								value = textState.value,
								onValueChange = { it -> textState.value = it },
								label = { Text("Server-Adresse") },
								shape = RoundedCornerShape(30.dp),
								singleLine = true,
								keyboardActions = KeyboardActions(
									onDone = {
										keyboardController?.hide()
										changePort.value = false
									}
								),
								keyboardOptions = KeyboardOptions.Default.copy(
									imeAction = ImeAction.Done
								)
							)
						}
					}
				}
			}
		}
	}
}

@Composable
fun ActionButton(text: String, handle: Boolean, onClick: (Boolean) -> Unit) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surfaceContainerHigh
	) {
		Row(
			modifier = Modifier.padding(24.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				style = MaterialTheme.typography.headlineMedium,
				text = text
			)
			Switch(
				checked = handle,
				onCheckedChange = onClick
			)
		}
	}
}
