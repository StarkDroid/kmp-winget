package utils


import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import java.util.*


/**
 * init fonts here
 */
//changed to google sans
val headingFont = FontFamily(Font("font/GoogleSans-Bold.ttf"))
val bodyFont = FontFamily(Font("font/GoogleSans-Regular.ttf"))//changed to google sans

/**
 * Function to load strings (Non Compose way)
 */
fun loadString(key: String): String {
    val bundle = ResourceBundle.getBundle("strings/strings")
    return bundle.getString(key)
}