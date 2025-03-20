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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mminl.interscore_remoteend.ui.theme.InterscoreRemoteendTheme

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
						Text("Connected to port 8081")
						FilledTonalButton(onClick = {}) { Text("Change port") }
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
						ActionButton(text = "Toggle Scoreboard", onClick = {})
						ActionButton(text = "Toggle Livetable", onClick = {})
						ActionButton(text = "Toggle Gameplan", onClick = {})
						ActionButton(text = "Toggle Gamestart", onClick = {})
					}
				}
			}
		}
	}
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
	Surface(
		modifier = Modifier.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surfaceContainerHigh,
		onClick = onClick
	) {
		Text(
			modifier = Modifier.padding(30.dp),
			style = MaterialTheme.typography.headlineLarge,
			text = text
		)
	}
}
