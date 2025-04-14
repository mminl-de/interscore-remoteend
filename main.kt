import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Hello World") {
        App()
    }
}

@Composable
fun App() {
    MaterialTheme {
        Text("Hello, World!")
    }
}
