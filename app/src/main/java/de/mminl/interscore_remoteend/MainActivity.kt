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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
			modifier = Modifier.fillMaxSize(),
			topBar = {
				CenterAlignedTopAppBar(
					title = { Text("Interscore – Remoteend") },
					actions = {
						IconButton(onClick = { showMenu = !showMenu }) {
							Icon(
								imageVector = Icons.Filled.MoreVert,
								contentDescription = "TODO local"
							)
						}
						// TODO NOW
						DropdownMenu(
							expanded = showMenu,
							onDismissRequest = { showMenu = false }
						) {
							// TODO
							DropdownMenuItem(
								text = { Text("Change port") },
								onClick = { showMenu = false }
							)
							DropdownMenuItem(
								text = { Text("About") },
								onClick = { showMenu = false }
							)
						}
					}
				)
			},
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
						Button(onClick = {}) { Text("Change port") }
					}
				}
			}
		) { innerPadding ->
			Column(modifier = Modifier.padding(innerPadding)) {
				// TODO
				Text("TODO body text")
			}
		}
	}
}
