import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import io.telereso.kmp.jsoncompare.App
import org.jetbrains.compose.resources.configureWebResources

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }
    CanvasBasedWindow("JsonCompare") {
        App()
    }
}