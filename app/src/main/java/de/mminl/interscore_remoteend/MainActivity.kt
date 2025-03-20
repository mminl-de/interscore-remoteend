package de.mminl.interscore_remoteend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import de.mminl.interscore_remoteend.ui.theme.InterscoreRemoteendTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InterscoreRemoteendTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
					Text(
						text = "Hello $name!",
						modifier = modifier
					)
                }
            }
        }
    }
}
