package com.daonvshu.bangumi

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.daonvshu.bangumi.pages.MikanBangumiDetailPage
import com.daonvshu.bangumi.pages.MikanDataView
import com.daonvshu.shared.components.DashedDivider
import com.daonvshu.shared.components.DividerOrientation
import com.daonvshu.shared.components.VerticalNavBar

@Composable
fun BangumiMain() {
    val viewModel = viewModel{ BangumiMainVm() }
    val sharedVm = viewModel{ BangumiSharedVm() }
    Row {
        val selectedIndex by viewModel.menuItemIndex.collectAsStateWithLifecycle()
        val menus = arrayListOf("数据源(Mikan)", "搜索", "设置", "下载")
        VerticalNavBar(
            items = menus,
            selectedIndex = selectedIndex,
            normalColor = Color(0xFF6B4D36),
            selectedColor = Color(0xFF22A9C3),
        ) {
            viewModel.menuItemIndex.value = it
        }

        DashedDivider(
            modifier = Modifier.padding(vertical = 20.dp),
            orientation = DividerOrientation.Vertical,
            color = Color(0xFFFF639C).copy(alpha = 0.5f)
        )

        Box (
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(20.dp)
        ) {
            val navController = rememberNavController()
            LaunchedEffect(sharedVm.navHost) {
                sharedVm.navHost.collect {
                    if (it.isNotEmpty()) {
                        if (it == "pop") {
                            navController.popBackStack()
                        } else {
                            navController.navigate(it)
                        }
                    }
                }
            }

            NavHost(
                navController = navController,
                startDestination = "dataView"
            ) {
                composable("dataView") {
                    MikanDataView(sharedVm)
                }
                composable("detail") {
                    MikanBangumiDetailPage(sharedVm)
                }
            }
        }
    }
}