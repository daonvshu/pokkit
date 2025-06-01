import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberNotification
import androidx.compose.ui.window.rememberTrayState

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color.Blue)
    }
}


@Composable
private fun WindowScope.AppWindowTitleBar() = WindowDraggableArea {
    Box(Modifier
        .fillMaxWidth()
        .height(48.dp)
        .background(Color.DarkGray)
    ) {

    }
}

fun main() = application {
    var isOpen by remember { mutableStateOf(true) }

    if (isOpen) {
        val trayState = rememberTrayState()
        val notification = rememberNotification("Notification", "This is a notification")

        Tray(
            state = trayState,
            icon = TrayIcon,
            menu = {
                Item("Quit", onClick = {
                    isOpen = false
                })
                Item("Show Notification", onClick = {
                    trayState.sendNotification(notification)
                })
            },
        )

        Window(
            onCloseRequest = {
                isOpen = false
            },
            undecorated = true,
            resizable = true,
            transparent = true,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp)
                    .shadow(3.dp, RoundedCornerShape(6.dp)),
                color = Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            ) {
                Column {
                    WindowDraggableArea {
                        AppWindowTitleBar()
                    }
                    App()
                }
            }
        }
    }
}

@Composable
fun App() {
    MaterialTheme(
        typography = Typography()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().background(Color.White)
        ) {

        }
    }
}