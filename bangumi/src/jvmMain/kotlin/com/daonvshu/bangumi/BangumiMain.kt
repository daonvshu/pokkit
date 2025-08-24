package com.daonvshu.bangumi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.daonvshu.bangumi.pages.DownloadPage
import com.daonvshu.bangumi.pages.MikanBangumiDetailPage
import com.daonvshu.bangumi.pages.MikanDataView
import com.daonvshu.bangumi.pages.SearchPage
import com.daonvshu.bangumi.pages.SettingPage
import com.daonvshu.shared.backendservice.BackendDataObserver
import com.daonvshu.shared.backendservice.BackendService
import com.daonvshu.shared.components.DashedDivider
import com.daonvshu.shared.components.DividerOrientation
import com.daonvshu.shared.components.HSpacer
import com.daonvshu.shared.components.LogBox
import com.daonvshu.shared.components.VerticalNavBar
import com.daonvshu.shared.generated.resources.Res
import com.daonvshu.shared.generated.resources.ic_double_arrow_down
import com.daonvshu.shared.styles.TextStyleProvider
import com.daonvshu.shared.utils.LogCollector
import com.daonvshu.shared.utils.PrimaryColors
import com.daonvshu.shared.utils.rememberNavHostController
import org.jetbrains.compose.resources.painterResource

@Composable
fun BangumiMain() {
    val viewModel = viewModel{ BangumiMainVm() }
    val sharedVm = viewModel{ BangumiSharedVm() }

    Row {
        val selectedIndex by viewModel.menuItemIndex.collectAsStateWithLifecycle()
        val menus = arrayListOf("数据源(Mikan)", "搜索", "设置", "下载")
        Column(
            modifier = Modifier.width(200.dp)
        ) {
            VerticalNavBar(
                modifier = Modifier.weight(1f),
                items = menus,
                selectedIndex = selectedIndex,
                normalColor = PrimaryColors.Text_Normal,
                selectedColor = PrimaryColors.Text_Selected,
            ) {
                viewModel.menuItemIndex.value = it
                sharedVm.navHost.value = when (it) {
                    0 -> "dataView"
                    1 -> "search"
                    2 -> "setting"
                    3 -> "download"
                    else -> "dataView"
                }
            }

            val logs by LogCollector.logs.collectAsState()
            LogBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                textColor = PrimaryColors.Text_Selected.copy(alpha = 0.4f),
                logList = logs
            )
        }

        DashedDivider(
            modifier = Modifier.padding(vertical = 20.dp),
            orientation = DividerOrientation.Vertical,
            color = PrimaryColors.MAGENTA.color(level = 5, alpha = 0.5f)
        )

        Box (
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 10.dp)
        ) {
            Column {
                NavHost(
                    modifier = Modifier.weight(1f),
                    navController = rememberNavHostController(sharedVm.navHost),
                    startDestination = "dataView"
                ) {
                    composable("dataView") {
                        MikanDataView(sharedVm)
                    }
                    composable("detail") {
                        MikanBangumiDetailPage(sharedVm)
                    }
                    composable("download") {
                        DownloadPage(sharedVm)
                    }
                    composable("search") {
                        SearchPage()
                    }
                    composable("setting") {
                        SettingPage()
                    }
                }

                TextStyleProvider(
                    fontSize = 12.sp,
                    color = PrimaryColors.Bangumi_Primary
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        HSpacer()

                        val speedStatus by BackendDataObserver.torrentSpeedUpdated.collectAsStateWithLifecycle()
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(Res.drawable.ic_double_arrow_down),
                            contentDescription = null,
                            tint = PrimaryColors.GREEN.color()
                        )
                        Text(speedStatus?.downloadSpeed ?: "0.0 B/s", modifier = Modifier.width(68.dp))

                        Icon(
                            modifier = Modifier.size(16.dp).rotate(180f),
                            painter = painterResource(Res.drawable.ic_double_arrow_down),
                            contentDescription = null,
                            tint = PrimaryColors.BLUE.color()
                        )
                        Text(speedStatus?.uploadSpeed ?: "0.0 B/s", modifier = Modifier.width(68.dp))
                    }
                }
            }
        }
    }
}