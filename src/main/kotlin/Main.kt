import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.MainScren

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Package Manager",
        resizable = false) {
        MainScren()
    }
}
