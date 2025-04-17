package de.mminl.interscore_remoteend

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Dialog
import de.mminl.interscore_remoteend.ui.theme.InterscoreRemoteendTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit
enum class WS_STATE {CONNECTED, CONNECTING, CONNECTION_FAILED, NOT_CONNECTED, CLOSING}

data class WebSocketClientState(
	var connected: WS_STATE = WS_STATE.NOT_CONNECTED
)



class WebSocketClient(url: String, private val state: MutableState<WebSocketClientState>) {
	private val client = OkHttpClient.Builder()
		.retryOnConnectionFailure(true)
		.pingInterval(10, TimeUnit.SECONDS)
		.build()
	private val request = Request.Builder().url(url).build()

	private val listener = object : WebSocketListener() {
		override fun onOpen(webSocket: WebSocket, response: Response) {
			state.value = state.value.copy(connected = WS_STATE.CONNECTED)
			Log.d("COMPOSE_DEBUG", "Connection successful: ${state.value.connected}")
		}

		override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
			if(state.value.connected != WS_STATE.CLOSING) {
				Log.d("COMPOSE_DEBUG", "Connection failed (P1): ${state.value.connected}")
				state.value = state.value.copy(connected = WS_STATE.CONNECTION_FAILED)
				Log.d("COMPOSE_DEBUG", "Connection failed (P2): ${state.value.connected}")
			} else {
				Log.d("COMPOSE_DEBUG", "Connection closed by us: ${state.value.connected}")
			}
		}

		// TODO FINAL disconnect on destroying app
	}

	var webSocket: WebSocket? = null

	fun connect() {
		webSocket = client.newWebSocket(request, listener)
	}

	// TODO
	fun reconnect() {
		state.value = state.value.copy(connected = WS_STATE.CLOSING)
		webSocket?.cancel()
		connect()
		state.value = state.value.copy(connected = WS_STATE.CONNECTING)
		Log.d("COMPOSE_DEBUG", "Reconnecting: ${state.value.connected}")
	}
}

//data class Connection(var connected: Boolean, var message: String, var streaming: Boolean)

// TODO FINAL dont make the phone sleep
// TODO FINAL confirmation dialogue for closing stream
class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
		super.onCreate(savedInstanceState)
		enableEdgeToEdge()
		setContent { RemoteendApp() }
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteendApp() {
	// TODO READ where to properly put this
	var wscStateMutable = remember { mutableStateOf(WebSocketClientState()) }
	var wscState by wscStateMutable
	val wsc = remember { WebSocketClient("ws://192.168.178.57:8081", wscStateMutable) }
	var streamCloseConfirmation by remember { mutableStateOf(false) }
	wsc.connect()

	val borderColor by remember(wscState.connected) {
		mutableStateOf(
			if (wscState.connected == WS_STATE.CONNECTED) {
				Log.d("COMPOSE_DEBUG", "Color Green, Connected")
				Color.Green.copy(alpha = 0.15f)
			} else if (wscState.connected == WS_STATE.CONNECTING || wscState.connected == WS_STATE.CLOSING){
				Log.d("COMPOSE_DEBUG", "Color Blue, Connecting")
				Color.Blue.copy(alpha = 0.15f)
			} else {
				Log.d("COMPOSE_DEBUG", "Color Red, Not Connected")
				Color(0x99FF5722).copy(alpha = 0.15f)
			}
		)
	}

	InterscoreRemoteendTheme {
		Scaffold(
			modifier = Modifier
				.fillMaxSize()
				.drawWithContent {
					drawContent()

					val glowWidth = 200.dp.toPx()

					// Left glow (red → transparent)
					drawRect(
						brush = Brush.horizontalGradient(
							0f to borderColor,
							1f to Color.Transparent,
							startX = 0f,
							endX = glowWidth
						),
						size = Size(glowWidth, size.height)
					)

					// Right glow (transparent → red)
					drawRect(
						brush = Brush.horizontalGradient(
							0f to Color.Transparent,
							1f to borderColor,
							startX = size.width - glowWidth,
							endX = size.width
						),
						topLeft = Offset(size.width - glowWidth, 0f),
						size = Size(glowWidth, size.height)
					)

					// Top glow (red → transparent)
					drawRect(
						brush = Brush.verticalGradient(
							colors = listOf(borderColor, Color.Transparent),
							startY = 0f,
							endY = glowWidth
						),
						size = Size(size.width, glowWidth)
					)

					// Bottom glow (transparent → red)
					drawRect(
						brush = Brush.verticalGradient(
							colors = listOf(Color.Transparent, borderColor),
							startY = size.height - glowWidth,
							endY = size.height
						),
						topLeft = Offset(0f, size.height - glowWidth),
						size = Size(size.width, glowWidth)
					)
				},
			topBar = {
				TopAppBar(
					title = {
						Text(
							"Interscore Remote",
							style = MaterialTheme.typography.headlineMedium
						)
					},
					actions = {
						IconButton(onClick = { /* TODO */ }) {
							Icon(
								imageVector = Icons.Filled.MoreVert,
								contentDescription = "Menu"
							)
						}
						IconButton(onClick = { /* TODO */ }) {
							Icon(
								imageVector = Icons.Filled.MoreVert,
								contentDescription = "Menu"
							)
						}
					}
				)
			}
		) { innerPadding ->
			Column(
				modifier = Modifier.fillMaxSize().padding(innerPadding),
				verticalArrangement = Arrangement.SpaceEvenly
			) {
				Column(
					modifier = Modifier.fillMaxWidth(),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.spacedBy(
						16.dp,
						Alignment.CenterVertically
					)
				) {
					FilledTonalButton(onClick = { wsc.reconnect() }) {
						Text(
							"Neu verbinden",
							style = MaterialTheme.typography.bodyLarge
						)
					}
				}
				WidgetButtonColumn(wsc.webSocket, wscState.connected == WS_STATE.CONNECTED)
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceEvenly
				) {
					ActionButton(
						Icons.AutoMirrored.Filled.Send,
						labelOff = "Start stream",
						labelOn = "Stop stream",
						onOff = {},
						onOn = { streamCloseConfirmation = true },
						enabled = wscState.connected == WS_STATE.CONNECTED
					)
					ActionButton(
						Icons.Filled.Star,
						labelOff = "Capture replay",
						labelOn = "Abort replay",
						onOff = {
							wsc.webSocket?.send(ByteString.of(7))
						},
						onOn = {
							wsc.webSocket?.send(ByteString.of(8))
						},
						enabled = wscState.connected == WS_STATE.CONNECTED
					)
				}
			}
		}

		if (streamCloseConfirmation) {
			Dialog(onDismissRequest = { streamCloseConfirmation = false }) {
				Card(
					modifier = Modifier
						.fillMaxWidth()
						.height(200.dp)
						.padding(16.dp),
					shape = MaterialTheme.shapes.extraLarge
				) {
					Text(
						text = "Möchten Sie den Stream wirklich schließen?"
					)
				}
			}
		}
	}
}

@Composable
fun WidgetButtonColumn(webSocket: WebSocket?, enabled: Boolean) {
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

			WidgetButton("Scoreboard", scoreboardHandle, enabled) {
				scoreboardHandle = !scoreboardHandle
				webSocket?.send(ByteString.of(0))
			}
			WidgetButton("Gameplan", gameplanHandle, enabled) {
				gameplanHandle = !gameplanHandle
				livetableHandle = false
				gamestartHandle = false
				webSocket?.send(ByteString.of(1))
			}
			WidgetButton("Livetable", livetableHandle, enabled) {
				livetableHandle = !livetableHandle
				gameplanHandle = false
				gamestartHandle = false
				webSocket?.send(ByteString.of(2))
			}
			WidgetButton("Gamestart", gamestartHandle, enabled) {
				gamestartHandle = !gamestartHandle
				livetableHandle = false
				gameplanHandle = false
				webSocket?.send(ByteString.of(3))
			}
			WidgetButton("Ad", adHandle, enabled) {
				adHandle = !adHandle
				webSocket?.send(ByteString.of(4))
			}
		}
	}
}

@Composable
fun WidgetButton(text: String, handle: Boolean, enabled: Boolean, onClick: () -> Unit) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surfaceContainerHigh,
		onClick = onClick,
		enabled = enabled
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
				onCheckedChange = { onClick() },
				enabled = enabled
			)
		}
	}
}

@Composable
fun ActionButton(
	imageVector: ImageVector,
	labelOff: String, labelOn: String,
	onOff: () -> Unit, onOn: () -> Unit,
	enabled: Boolean
) {
	var isClicked by remember { mutableStateOf(false) }
	if (isClicked) ActionButtonOn(imageVector = Icons.Filled.Close, label = labelOn, onClick = onOn, enabled) {
		isClicked = !isClicked
	} else ActionButtonOff(imageVector = imageVector, label = labelOff, onClick = onOff, enabled) {
		isClicked = !isClicked
	}
}

@Composable
fun ActionButtonOn(imageVector: ImageVector, label: String, onClick: () -> Unit, enabled: Boolean, switch: () -> Unit) {
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
			),
			enabled = enabled
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
fun ActionButtonOff(imageVector: ImageVector, label: String, onClick: () -> Unit, enabled: Boolean, switch: () -> Unit) {
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
			),
			enabled = enabled
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
