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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mminl.interscore_remoteend.ui.theme.InterscoreRemoteendTheme

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		enableEdgeToEdge() // TODO CONSIDER
		setContent { RemoteendApp() }
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteendApp() {
	var showMenu by remember { mutableStateOf(false) }

	InterscoreRemoteendTheme {
		Scaffold(
			// TODO CONSIDER
			modifier = Modifier.fillMaxSize(),
			topBar = { TopAppBar(title = { Text("Interscore – Remoteend") }) },
			bottomBar = {
				BottomAppBar {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = 16.dp),
						horizontalArrangement = Arrangement.SpaceBetween,
						verticalAlignment = Alignment.CenterVertically
					) {
						Text("Connected to port 8081")
						FilledTonalButton(onClick = {}) { Text("Change port") }
					}
				}
			}
		) { innerPadding ->
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(innerPadding),
				verticalArrangement = Arrangement.Center
			) {
				Surface(
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.wrapContentHeight(),
					shape = RoundedCornerShape(30.dp)
				) {
					Column(
						modifier = Modifier
							.wrapContentHeight(),
						verticalArrangement = Arrangement.spacedBy(
							10.dp, // TODO VALUE
							Alignment.CenterVertically
						)
					) {
						ActionButton(text = "Toggle scoreboard", onClick = {})
						ActionButton(text = "Toggle livetable", onClick = {})
						ActionButton(text = "Toggle gameplan", onClick = {})
						ActionButton(text = "Toggle gamestart", onClick = {})
					}
				}
			}
		}
	}
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
	Surface(
		modifier = Modifier
			.fillMaxWidth(),
		color = MaterialTheme.colorScheme.surfaceContainerHigh,
		onClick = onClick
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Button(onClick = {}) { Text("TODO icon") }
			Text(
				modifier = Modifier.padding(30.dp),
				text = text
			)
		}
	}
}
