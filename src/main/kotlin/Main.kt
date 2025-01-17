import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import ui.MainScreen

fun main() = application {
    val icon = painterResource("drawables/kmp-winget.png")
    val state = rememberWindowState(
        width = 800.dp,
        height = 800.dp
    )
    Window(
        onCloseRequest = ::exitApplication,
        title = "kmp-winget",
        icon = icon,
        resizable = false,
        state = state
    ) {
        MainScreen()
    }
}
