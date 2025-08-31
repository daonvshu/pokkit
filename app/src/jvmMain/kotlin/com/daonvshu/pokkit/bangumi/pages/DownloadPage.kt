package com.daonvshu.pokkit.bangumi.pages

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.daonvshu.pokkit.bangumi.BangumiSharedVm
import com.daonvshu.shared.components.TabNavBar
import com.daonvshu.shared.components.VSpacer
import com.daonvshu.shared.utils.PrimaryColors
import com.daonvshu.shared.utils.rememberNavHostController

@Composable
fun DownloadPage(sharedVm: BangumiSharedVm) {
    val vm = viewModel { DownloadPageVm() }

    Column {
        val typeIndex = vm.typeIndex.collectAsStateWithLifecycle()
        val tabs = listOf("全部", "正在下载", "其他下载")
        TabNavBar(
            titles = tabs,
            selectedIndex = typeIndex.value,
            normalColor = PrimaryColors.Text_Normal,
            selectedColor = PrimaryColors.Text_Selected,
            scrollable = true,
        ) {
            vm.typeIndex.value = it

            sharedVm.showOnlyDownloading = it == 1
            sharedVm.showExtraDownloading = it == 2
            sharedVm.targetDownloadId.value = -1
            when (it) {
                0 -> vm.navHost.value = "bangumiView"
                1 -> vm.navHost.value = "downloadListView"
                2 -> vm.navHost.value = "downloadOtherView"
            }
        }

        VSpacer(height = 8.dp)

        NavHost(
            modifier = Modifier.weight(1f),
            navController = rememberNavHostController(vm.navHost),
            startDestination = "bangumiView"
        ) {
            composable("bangumiView") {
                DownloadBangumiPage { item ->
                    sharedVm.showOnlyDownloading = false
                    sharedVm.showExtraDownloading = false
                    sharedVm.targetDownloadId.value = item.bindId
                    vm.navHost.value = "downloadListView"
                }
            }
            composable("downloadListView") {
                DownloadListPage(sharedVm)
            }
            composable("downloadOtherView") {
                DownloadOtherPage()
            }
        }
    }
}