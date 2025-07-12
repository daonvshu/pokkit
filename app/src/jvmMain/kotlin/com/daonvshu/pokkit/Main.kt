package com.daonvshu.pokkit

import com.daonvshu.bangumi.BangumiMain
import com.daonvshu.shared.font.Calista
import com.daonvshu.shared.font.ChillRoundGothic
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberNotification
import androidx.compose.ui.window.rememberTrayState
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.daonvshu.shared.backendservice.BackendService
import com.daonvshu.shared.database.Databases

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color.Blue)
    }
}

@Composable
private fun WindowScope.AppWindowTitleBar(viewModel: MainViewModel) = WindowDraggableArea {

    Box(Modifier
        .fillMaxWidth()
        .height(48.dp)
    ) {
        val selectedIndex by viewModel.selectedMenuIndex.collectAsStateWithLifecycle()
        val selectedColor by animateColorAsState(viewModel.menuItems[selectedIndex].color)

        Box(modifier = Modifier
            .fillMaxSize()
            .background(selectedColor.copy(alpha = 0.1f))
        )

        Text(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp),
            text = "Pokkit",
            fontFamily = Calista(),
            fontSize = 30.sp,
            color = selectedColor
        )

        Row(
            Modifier
                .fillMaxHeight()
                .align(Alignment.TopEnd)
                .padding(end = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            viewModel.menuItems.forEachIndexed { i, item ->
                val itemColor by animateColorAsState(if (i == selectedIndex) item.color else Color(0xFF4C3A28))
                Spacer(Modifier.width(if (i == 0) 0.dp else 24.dp))
                Row(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { viewModel.selectedMenuIndex.value = i }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Box(modifier = Modifier
                        .width(24.dp)
                    ) {
                        if (i == selectedIndex) {
                            Text(
                                "✿",
                                modifier = Modifier.align(Alignment.CenterStart),
                                color = itemColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }
                    }
                    Text(
                        text = item.label.uppercase(),
                        color = itemColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

fun main() = application {
    Databases.init()

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

        val windowState = rememberWindowState(width = 1280.dp, height = 768.dp)
        Window(
            onCloseRequest = {
                isOpen = false
                BackendService.close()
            },
            undecorated = true,
            resizable = true,
            transparent = true,
            state = windowState,
        ) {
            val mainVm = viewModel { MainViewModel() }
            val selectedMenuItem by mainVm.selectedMenuIndex.collectAsStateWithLifecycle()
            val selectedColor by animateColorAsState(mainVm.menuItems[selectedMenuItem].color)

            LaunchedEffect(mainVm.selectedMenuIndex) {
                mainVm.selectedMenuIndex.collect {
                    if (it == 2) {
                        isOpen = false
                    }
                }
            }

            MaterialTheme(
                typography = Typography(
                    defaultFontFamily = ChillRoundGothic()
                )
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(5.dp)
                        .shadow(3.dp, RoundedCornerShape(6.dp))
                        .drawBehind {
                            val squareSize = 16f
                            val squareColor = selectedColor.copy(alpha = 0.04f)

                            drawRect(
                                color = Color.White,
                                topLeft = Offset.Zero,
                                size = size
                            )

                            val cols = (size.width / squareSize).toInt() + 1
                            val rows = (size.height / squareSize).toInt() + 1
                            for (row in 0 until rows) {
                                for (col in 0 until cols) {
                                    // 只绘制偶数行偶数列、奇数行奇数列(呈棋盘格)
                                    if ((row + col) % 2 == 0) {
                                        drawRect(
                                            color = squareColor,
                                            topLeft = Offset(
                                                x = col * squareSize,
                                                y = row * squareSize
                                            ),
                                            size = Size(squareSize, squareSize),
                                            style = Fill
                                        )
                                    }
                                }
                            }
                        },
                    color = Color.Transparent,
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Column {
                        WindowDraggableArea {
                            AppWindowTitleBar(mainVm)
                        }
                        App()
                    }
                }
            }
        }
    }
}

@Composable
fun App() {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        NavHost(
            navController = rememberNavController(),
            startDestination = "bangumi"
        ) {
            composable("bangumi") {
                BangumiMain()
            }
            composable("pixiv") {
            }
        }
    }
}