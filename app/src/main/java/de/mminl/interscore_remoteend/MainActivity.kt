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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

	LaunchedEffect(Unit) {
		val request = Request.Builder().url("ws://192.168.0.28:$port").build()
		webSocket = client.newWebSocket(request, object : WebSocketListener() {
			override fun onOpen(webSocket: WebSocket, response: Response) {
				message = "Mit Port $port verbunden!"
			}

			override fun onMessage(webSocket: WebSocket, text: String) {
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
						FilledTonalButton(onClick = {}) { Text("Port ändern") }
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
						ActionButton(text = "Scoreboard", onClick = {
							webSocket?.send(ByteString.of(42))
						})
						ActionButton(text = "Livetable", onClick = {})
						ActionButton(text = "Gameplan", onClick = {})
						ActionButton(text = "Gamestart", onClick = {})
						ActionButton(text = "Ads", onClick = {})
					}
				}
			}
		}
	}
}

@Composable
fun ActionButton(text: String, onClick: (Boolean) -> Unit) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surfaceContainerHigh
	) {
		Row(
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = Modifier.padding(30.dp),
				style = MaterialTheme.typography.headlineMedium,
				text = text
			)
			Switch(
				modifier = Modifier.padding(end = 30.dp),
				checked = true,
				onCheckedChange = onClick
			)
		}
	}
}
