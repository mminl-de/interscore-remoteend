package de.mminl.interscoreremote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import de.mminl.interscoreremote.ui.theme.InterscoreRemoteTheme
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketClient(url: String) {
    private val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .pingInterval(10, TimeUnit.SECONDS)
        .build()
    private val request = Request.Builder().url(url).build()

    var webSocket: WebSocket? = null
    var connected: Boolean = false
    var message: String = "Verbindet..."

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            connected = true
            message = "Mit Port 8081 verbunden!"
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            connected = false
            message = "Verbindung gescheitert!"
        }

        // TODO FINAL disconnect on destroying app
    }

    fun connect() {
        webSocket = client.newWebSocket(request, listener)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RemoteApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteApp() {
    val navController = rememberNavController()

    InterscoreRemoteTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
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
                            Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Kebab Menu")
                        }
                    }
                )
            },
            bottomBar = { RemoteNavigationBar(navController) }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                NavigationHost(navController)
            }
        }
    }
}

@Composable
fun NavigationHost(navController: NavHostController) {
    NavHost(navController, startDestination = "Controls") {
        composable("Controls") { Controls() }
        composable("Game data") { GameData() }
        composable("Logs") { Logs() }
    }
}

sealed class Tab(val route: String, val icon: ImageVector) {
    data object Controls : Tab("Controls", Icons.Filled.Home)
    data object GameData : Tab("Game data", Icons.Filled.Home)
    data object Logs : Tab("Logs", Icons.Filled.Home)
}

@Composable
fun RemoteNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val tabs = arrayOf(Tab.Controls, Tab.GameData, Tab.Logs)

    NavigationBar {
        tabs.forEach { tab ->
            val selected = currentDestination?.route == tab.route
            NavigationBarItem(
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.route) },
                label = { Text(tab.route) },
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun Controls() {
    Surface(
        modifier = Modifier
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier.wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
        ) {
            Text("TODO NOW")
            Text("TODO NOW")
            Text("TODO NOW")
            Text("TODO NOW")
        }
    }
}

@Composable
fun GameData() {
    Text("TODO game data")
}

@Composable
fun Logs() {
    Text("TODO logs")
}
