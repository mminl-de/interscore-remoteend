package de.mminl.interscore_remoteend

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import de.mminl.interscore_remoteend.ui.theme.InterscoreRemoteendTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

class WebSocketClient(url: String, private val updateMessage: (String) -> Unit) {
	private val client = OkHttpClient.Builder()
		.retryOnConnectionFailure(true)
		.pingInterval(10, TimeUnit.SECONDS)
		.build()
	private val request = Request.Builder().url(url).build()

	private val listener = object : WebSocketListener() {
		override fun onOpen(webSocket: WebSocket, response: Response) {
			updateMessage("Mit Port 8081 verbunden!")
		}

		override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
			updateMessage("Verbindung gescheitert!")
		}

		// TODO FINAL disconnect on destroying app
	}

	var webSocket: WebSocket? = null

	fun connect() {
		webSocket = client.newWebSocket(request, listener)
	}

	// TODO
	fun reconnect() {
		webSocket?.cancel()
		updateMessage("Verbindet neu...")
		connect()
	}
}

// TODO FINAL dont make the phone sleep
// TODO FINAL confirmation dialogue for closing stream
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent { RemoteendApp() }
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteendApp() {
	// TODO READ where to properly put this
	var message by remember { mutableStateOf("Verbindet...") }
	val wsc = WebSocketClient("ws://192.168.0.28:8081") { msg ->
		message = msg
	}
	wsc.connect()

	InterscoreRemoteendTheme {
		Scaffold(
			modifier = Modifier.fillMaxSize(),
			topBar = {
				TopAppBar(title = {
					Text(
						"Interscore Remote",
						style = MaterialTheme.typography.headlineMedium
					)
				})
			}
		) { innerPadding ->
			Column(
				modifier = Modifier.fillMaxSize().padding(innerPadding),
				verticalArrangement = Arrangement.SpaceEvenly
			) {
				Column(
					modifier = Modifier.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
				) {
					Text(
						message,
						style = MaterialTheme.typography.headlineSmall
					)
					FilledTonalButton(onClick = { wsc.reconnect() }) {
						Text(
							"Neu verbinden",
							style = MaterialTheme.typography.bodyLarge
						)
					}
				}
				WidgetButtonColumn(wsc.webSocket)
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceEvenly
				) {
					ActionButton(
						Icons.AutoMirrored.Filled.Send,
						labelOff = "Start stream",
						labelOn = "Stop stream",
						onOff = {},
						onOn = {}
					)
					ActionButton(
						Icons.Filled.Star,
						labelOff = "Capture replay",
						labelOn = "Abort replay",
						onOff = {},
						onOn = {}
					)
				}
			}
		}
	}
}

@Composable
fun WidgetButtonColumn(webSocket: WebSocket?) {
	Surface(
		modifier = Modifier
			.wrapContentHeight()
			.padding(horizontal = 16.dp),
		shape = MaterialTheme.shapes.extraLarge
	) {
		Column(
			modifier = Modifier.wrapContentHeight(),
			verticalArrangement =
				Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
		) {
			var scoreboardHandle by remember { mutableStateOf(false) }
			var gameplanHandle by remember { mutableStateOf(false) }
			var livetableHandle by remember { mutableStateOf(false) }
			var gamestartHandle by remember { mutableStateOf(false) }
			var adHandle by remember { mutableStateOf(false) }

			WidgetButton("Scoreboard", scoreboardHandle) {
				scoreboardHandle = !scoreboardHandle
				webSocket?.send(ByteString.of(0))
			}
			WidgetButton("Gameplan", gameplanHandle) {
				gameplanHandle = !gameplanHandle
				livetableHandle = false
				gamestartHandle = false
				webSocket?.send(ByteString.of(1))
			}
			WidgetButton("Livetable", livetableHandle) {
				livetableHandle = !livetableHandle
				gameplanHandle = false
				gamestartHandle = false
				webSocket?.send(ByteString.of(2))
			}
			WidgetButton("Gamestart", gamestartHandle) {
				gamestartHandle = !gamestartHandle
				livetableHandle = false
				gameplanHandle = false
				webSocket?.send(ByteString.of(3))
			}
			WidgetButton("Ad", adHandle) {
				adHandle = !adHandle
				webSocket?.send(ByteString.of(4))
			}
		}
	}
}

@Composable
fun WidgetButton(text: String, handle: Boolean, onClick: () -> Unit) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surfaceContainerHigh,
		onClick = onClick
	) {
		Row(
			modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				style = MaterialTheme.typography.headlineMedium,
				text = text
			)
			Switch(
				checked = handle,
				onCheckedChange = { onClick() }
			)
		}
	}
}

@Composable
fun ActionButton(
	imageVector: ImageVector,
	labelOff: String, labelOn: String,
	onOff: () -> Unit, onOn: () -> Unit
) {
	var isClicked by remember { mutableStateOf(false) }
	if (isClicked) ActionButtonOn(imageVector = Icons.Filled.Close, label = labelOn, onClick = onOn) {
		isClicked = !isClicked
	} else ActionButtonOff(imageVector = imageVector, label = labelOff, onClick = onOff) {
		isClicked = !isClicked
	}
}

@Composable
fun ActionButtonOn(imageVector: ImageVector, label: String, onClick: () -> Unit, switch: () -> Unit) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Button(
			modifier = Modifier.size(80.dp),
			onClick = {
				onClick()
				switch()
			},
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.primary,
				contentColor = MaterialTheme.colorScheme.surface
			)
		) {
			Icon(
				modifier = Modifier.size(32.dp),
				imageVector = imageVector,
				contentDescription = label
			)
		}
		Text(label)
	}
}

@Composable
fun ActionButtonOff(imageVector: ImageVector, label: String, onClick: () -> Unit, switch: () -> Unit) {
	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(16.dp)
	) {
		Button(
			modifier = Modifier.size(80.dp),
			onClick = {
				onClick()
				switch()
			},
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.surfaceVariant,
				contentColor = MaterialTheme.colorScheme.onSurface
			)
		) {
			Icon(
				modifier = Modifier.size(32.dp),
				imageVector = imageVector,
				contentDescription = label
			)
		}
		Text(label)
	}
}
