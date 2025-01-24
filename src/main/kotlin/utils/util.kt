package utils


import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import java.util.*


/**
 * init fonts here
 */
//changed to google sans
val headingFont = FontFamily(Font("font/Lato-Bold.ttf"))
val bodyFont = FontFamily(Font("font/Lato-Regular.ttf"))

/**
 * Function to load strings (Non Compose way)
 */
fun loadString(key: String): String {
    val bundle = ResourceBundle.getBundle("strings/strings")
    return bundle.getString(key)
}