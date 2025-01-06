import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.MainScreen

fun main() = application {
    val icon = painterResource("drawables/kmp-winget.png")
    Window(
        onCloseRequest = ::exitApplication,
        title = "kmp-winget",
        icon = icon,
        resizable = false) {
        MainScreen()
    }
}
