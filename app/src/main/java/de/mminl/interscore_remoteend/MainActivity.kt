package de.mminl.interscore_remoteend

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
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
enum class OBS_SCENE {LIVE, REPLAY, GAME_REPLAY}

data class WebSocketClientState(
	var connected: WS_STATE = WS_STATE.NOT_CONNECTED,
	var c_rentnerend: Boolean = false,
	var c_frontend: Boolean = false,
	var c_replaybuffer: Boolean = false,
	var c_obs_connected: Boolean = false,
	var c_obs_scene: OBS_SCENE = OBS_SCENE.LIVE
	//var videofeed/rtmp server/internet vom rtmp server bzw. yt connection
)

class WebSocketClient(private var url: String, private val state: MutableState<WebSocketClientState>) {
	private val client = OkHttpClient.Builder()
		.retryOnConnectionFailure(true)
		.pingInterval(10, TimeUnit.SECONDS)
		.build()
	private var request = Request.Builder().url(url).build()

	enum class RECEIVE_CODE {CLIENTS_CON_STATUS, IMAGE, JSON, GAME_CHANGES}
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

		override fun onMessage(webSocket: WebSocket, text: String) {
			Log.d("COMPOSE_DEBUG", "Received text message (ignoring): $text")
		}

		override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
			Log.d("COMPOSE_DEBUG", "Received binary message: $bytes")
			val data = bytes.toByteArray()
			if(data[0].toInt() == RECEIVE_CODE.CLIENTS_CON_STATUS.ordinal && data.size >= 6) {
				Log.d("COMPOSE_DEBUG", "Binary message is CLIENTS_CON_STATUS")
				state.value = state.value.copy(c_rentnerend = data[1].toInt() == 1)
				state.value = state.value.copy(c_frontend = data[2].toInt() == 1)
				state.value = state.value.copy(c_obs_connected = data[3].toInt() == 1)
				state.value = state.value.copy(c_replaybuffer = data[4].toInt() == 1)
				state.value = state.value.copy(c_obs_scene = OBS_SCENE.entries[data[5].toInt()])
			}
		}

		// TODO FINAL disconnect on destroying app
	}

	var webSocket: WebSocket? = null

	fun connect() {
		webSocket = client.newWebSocket(request, listener)
	}

	// TODO
	fun reconnect(urlNew: String) {
		if(urlNew != url) {
			url = urlNew
			request = Request.Builder().url(url).build()
		}
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
	var ipAddress by remember { mutableStateOf("ws://192.168.178.57:8081") }
	var wscStateMutable = remember { mutableStateOf(WebSocketClientState()) }
	var wscState by wscStateMutable
	val wsc = remember { WebSocketClient(ipAddress, wscStateMutable) }
	var streamCloseConfirmation by remember { mutableStateOf(false) }

	//This is needed so the application doesnt close instantly when pressing the back button
	BackHandler { Log.d("BACK_HANDLER", "Back button pressed – doing nothing, fuck you") }

	wsc.connect()

	var borderAlpha: Float
	val borderColor: Color
	var glowWidth_: Dp
	if (wscState.connected == WS_STATE.CONNECTED) {
		Log.d("COMPOSE_DEBUG", "Color Green, Connected")
		borderColor = Color.Green//.copy(alpha = 0.15f)
		borderAlpha = 0.15f
		glowWidth_ = 20.dp
	} else if (wscState.connected == WS_STATE.CONNECTING || wscState.connected == WS_STATE.CLOSING){
		Log.d("COMPOSE_DEBUG", "Color Blue, Connecting")
		borderColor = Color(0xFF1565C0)
		borderAlpha = 0.7f
		glowWidth_ = 30.dp
	} else {
		Log.d("COMPOSE_DEBUG", "Color Red, Not Connected")
		borderColor = Color(0xFFE53935)
		borderAlpha = 0.3f
		glowWidth_ = 50.dp
	}

	val color_rentnerend: Color
	val color_obs: Color
	val color_web: Color
	val color_replaybuffer: Color
	color_rentnerend = if(wscState.c_rentnerend) Color.Green else Color.Red
	color_obs = if(wscState.c_obs_connected) Color.Green else Color.Red
	color_web = if(wscState.c_frontend) Color.Green else Color.Red
	color_replaybuffer = if(wscState.c_replaybuffer) Color.Green else Color.Red


	InterscoreRemoteendTheme {
		Scaffold(
			modifier = Modifier
				.fillMaxSize()
				.drawWithContent {
					drawContent()

					// TODO FINAL make this not suck, glowWidth should be Px directly, Problem is that toPx() is not accessible above
					val glowWidth = glowWidth_.toPx()

					// Left glow (red → transparent)
					drawRect(
						brush = Brush.horizontalGradient(
							0f to borderColor.copy(alpha = borderAlpha),
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
							1f to borderColor.copy(alpha = borderAlpha),
							startX = size.width - glowWidth,
							endX = size.width
						),
						topLeft = Offset(size.width - glowWidth, 0f),
						size = Size(glowWidth, size.height)
					)

					// Top glow (red → transparent)
					drawRect(
						brush = Brush.verticalGradient(
							colors = listOf(borderColor.copy(alpha = borderAlpha), Color.Transparent),
							startY = 0f,
							endY = glowWidth
						),
						size = Size(size.width, glowWidth)
					)

					// Bottom glow (transparent → red)
					drawRect(
						brush = Brush.verticalGradient(
							colors = listOf(Color.Transparent, borderColor.copy(alpha = borderAlpha)),
							startY = size.height - glowWidth,
							endY = size.height
						),
						topLeft = Offset(0f, size.height - glowWidth),
						size = Size(size.width, glowWidth)
					)
				},
		) { innerPadding ->
			Column(
				modifier = Modifier.fillMaxSize().padding(innerPadding),
				verticalArrangement = Arrangement.SpaceEvenly
			) {
				/*
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 16.dp, vertical = 8.dp)
				) {
					Row(
						modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier.height(IntrinsicSize.Min)
						) {
							Text("Kampfgericht:")
							//Box(modifier = Modifier.clip(CircleShape).background(color_rentnerend))
							Box(
								modifier = Modifier
								.matchParentSize()
								.clip(CircleShape)
								.background(color_rentnerend)
							)
						}
					}
					Row(
						modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text("Web-Overlay:")
						Box(modifier = Modifier.clip(CircleShape).background(color_web))
					}
					Row(
						modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text("OBS:")
						Box(modifier = Modifier.clip(CircleShape).background(color_obs))
					}
					Row(
						modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text("Replay buffer:")
						Box(modifier = Modifier.clip(CircleShape).background(color_replaybuffer))
					}
				}
				*/
				WidgetWSClientConStatus(wscState)
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
			if(wscState.connected != WS_STATE.CONNECTED) {
				Surface(
					modifier = Modifier.fillMaxWidth().height(90.dp),
					color = borderColor
				) {
					Row(
						modifier = Modifier.fillMaxSize()
							.padding(bottom = 6.dp, start = 12.dp, end = 12.dp),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.Bottom,
					) {
						TextField(
							value = ipAddress,
							onValueChange = { ipAddress = it },
							label = { Text("IP-Adresse") },
							singleLine = true,
							colors = TextFieldDefaults.colors(
								unfocusedContainerColor = borderColor,
								focusedContainerColor = borderColor
							),
							modifier = Modifier
								.weight(1f)
								.padding(end = 8.dp)

						)
						FilledTonalButton(
							enabled = (wscState.connected != WS_STATE.CONNECTED) || (wscState.connected != WS_STATE.CONNECTING),
							onClick = { wsc.reconnect(ipAddress) },
							colors = ButtonDefaults.buttonColors(
								containerColor = Color(0x28000000),
								contentColor = Color.White
							),
						) {
							Text(
								"Verbinden",
								style = MaterialTheme.typography.bodyLarge
							)
						}
					}
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
fun WidgetWSClientConStatus(wsState: WebSocketClientState) {
	Surface(
		modifier = Modifier
			.wrapContentHeight()
			.padding(horizontal = 16.dp)
			.absoluteOffset()
		
		,shape = MaterialTheme.shapes.extraSmall
	) {
		Column(
			modifier = Modifier.wrapContentHeight(),
			verticalArrangement =
				Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
		) {
			WidgetWSClientConStatusTextLine(
				"Kampfgericht:",
				wsState.c_rentnerend,
				wsState.connected == WS_STATE.CONNECTED
			)
			WidgetWSClientConStatusTextLine(
				"Frontend:",
				wsState.c_frontend,
				wsState.connected == WS_STATE.CONNECTED
			)
			WidgetWSClientConStatusTextLine(
				"Replaybuffer:",
				wsState.c_replaybuffer,
				wsState.connected == WS_STATE.CONNECTED
			)
			WidgetWSClientConStatusTextLine(
				"OBS:",
				wsState.c_obs_connected,
				wsState.connected == WS_STATE.CONNECTED
			)
			//WidgetWSClientConStatusTextLine("OBS Szene:", wsState.c_rentnerend, wsState.connected == WS_STATE.CONNECTED)
		}
	}
}

@Composable
fun WidgetWSClientConStatusTextLine(text: String, connected: Boolean, enabled: Boolean) {
	Surface(
		modifier = Modifier.width(150.dp),
		color = MaterialTheme.colorScheme.surfaceContainerHigh,
	) {
		Row(
			modifier = Modifier.padding(horizontal = 2.dp, vertical = 0.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			//horizontalArrangement = Arrangement.End,
			verticalAlignment = Alignment.CenterVertically
		) {
			val textStyle = MaterialTheme.typography.bodyLarge
			var circleSize = with(LocalDensity.current) { textStyle.fontSize.toDp() * 1f }
			Text(
				style = textStyle,
				text = text
			)
			val btn_color: Color = if(connected) Color.Green else Color.Red
			Box(modifier = Modifier
				.clip(CircleShape)
				.size(circleSize)
				.clip(CircleShape)
				.background(btn_color)
			)
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
