package com.daonvshu.pokkit

import com.daonvshu.pokkit.bangumi.BangumiMain
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowScope
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.daonvshu.BuildConfig
import com.daonvshu.pokkit.backendservice.BackendDataObserver
import com.daonvshu.pokkit.backendservice.BackendService
import com.daonvshu.shared.components.HSpacer
import com.daonvshu.pokkit.database.Databases
import com.daonvshu.pokkit.settings.AppSettings
import com.daonvshu.shared.generated.resources.Res
import com.daonvshu.shared.generated.resources.logo
import com.daonvshu.shared.utils.LogCollector
import com.daonvshu.shared.utils.PrimaryColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.jetbrains.compose.resources.painterResource
import java.io.File

object TrayIcon : Painter() {
    override val intrinsicSize = Size(256f, 256f)

    override fun DrawScope.onDraw() {
        drawOval(Color.Blue)
    }
}

@Composable
private fun WindowScope.AppWindowTitleBar(viewModel: MainViewModel) = WindowDraggableArea {

    Box(
        Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val selectedIndex by viewModel.selectedMenuIndex.collectAsStateWithLifecycle()
        val selectedColor by animateColorAsState(viewModel.menuItems[selectedIndex].color)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(selectedColor.copy(alpha = 0.1f))
        )

        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "Pokkit",
                fontFamily = Calista(),
                fontSize = 30.sp,
                color = selectedColor
            )

            Text(
                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
                text = "V${BuildConfig.VERSION}",
                fontFamily = ChillRoundGothic(),
                fontSize = 12.sp,
                color = selectedColor
            )
        }

        Row(
            Modifier
                .fillMaxHeight()
                .align(Alignment.TopEnd)
                .padding(end = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            viewModel.menuItems.forEachIndexed { i, item ->
                val itemColor by animateColorAsState(if (i == selectedIndex) item.color else PrimaryColors.Text_Normal)
                HSpacer(if (i == 0) 0.dp else 24.dp)
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
                    Box(
                        modifier = Modifier
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

@OptIn(DelicateCoilApi::class)
fun main() = application {
    var initFinished by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope { Dispatchers.IO }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                ProcessBuilder("cmd", "/c", "start", "\"\"", "./pokkit_backend.exe").start()
            } catch (e: Exception) {
                LogCollector.addLog(e.message ?: "")
            }
        }
        delay(200)

        var tryConnectSize = 20
        var success = false
        while (tryConnectSize-- > 0) {
            success = BackendService.tryCreatePipeIfNeeded()
            if (success) {
                BackendDataObserver.backendServiceConnectError.value = ""
                break
            }
            delay(500)
        }
        if (!success) {
            throw RuntimeException("Backend service cannot not started!")
        }

        Databases.init()
        initFinished = true
    }

    var isOpen by remember { mutableStateOf(true) }
    if (!initFinished) {
        Window(
            onCloseRequest = {},
            undecorated = true,
            transparent = true,
            icon = painterResource(Res.drawable.logo),
            state = rememberWindowState(width = 1.dp, height = 1.dp),
        ) {}
    } else if (isOpen) {
        val windowState = rememberWindowState(width = 1280.dp, height = 768.dp)
        Window(
            onCloseRequest = {
                scope.cancel()
                isOpen = false
                BackendService.close()
            },
            undecorated = true,
            resizable = true,
            transparent = true,
            icon = painterResource(Res.drawable.logo),
            state = windowState,
        ) {
            LaunchedEffect(Unit) {
                AppSettings.proxyFlow.collect { proxy ->
                    SingletonImageLoader.setUnsafe { context ->
                        ImageLoader.Builder(context)
                            .crossfade(true)
                            .components {
                                add(
                                    OkHttpNetworkFetcherFactory(
                                        callFactory = {
                                            OkHttpClient.Builder()
                                                .proxy(proxy)
                                                .build()
                                        }
                                    )
                                )
                            }
                            .memoryCache {
                                MemoryCache.Builder()
                                    .maxSizePercent(context, 0.25)
                                    .build()
                            }
                            .diskCache {
                                DiskCache.Builder()
                                    .directory(File(".cache/image_cache"))
                                    .maxSizePercent(0.02)
                                    .build()
                            }
                            .build()
                    }
                }
            }

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
                                color = PrimaryColors.White,
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
                        App(mainVm)
                    }
                }
            }
        }
    }
}

@Composable
fun App(vm: MainViewModel) {
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

    val showServiceError by vm.showServiceError.collectAsStateWithLifecycle()
    if (showServiceError) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text("Service Error")
            },
            text = {
                Text(BackendDataObserver.backendServiceConnectError.value)
            },
            confirmButton = {
                Button(
                    onClick = {
                        vm.showServiceError.value = false
                        BackendDataObserver.backendServiceConnectError.value = ""
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}