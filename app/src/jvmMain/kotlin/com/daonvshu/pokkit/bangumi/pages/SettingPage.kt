package com.daonvshu.pokkit.bangumi.pages

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daonvshu.shared.components.CustomTextArea
import com.daonvshu.shared.components.InputColors
import com.daonvshu.shared.components.NormalCheckbox
import com.daonvshu.shared.components.VSpacer
import com.daonvshu.shared.styles.TextStyleProvider
import com.daonvshu.shared.utils.PrimaryColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.roundToInt

@Composable
fun SettingPage() {

    val vm = viewModel { SettingPageVm() }

    LaunchedEffect(vm) {
        vm.loadSettings()
    }

    TextStyleProvider(
        color = PrimaryColors.Text_Normal,
        fontSize = 14.sp
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SpeedSettingRow(vm)
                ProxySettingRow(vm)
                TrackerListRow(vm)
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(8.dp)
            )
        }
    }
}

@Composable
private fun SpeedSettingRow(vm: SettingPageVm) {
    Column {
        TitleRow(title = "限速")
        SpeedRow(
            title = "下载限速",
            speed = vm.downloadSpeed,
            speedDst = vm.downloadSpeedDst
        ) {
            vm.updateSpeedLimit()
        }
        SpeedRow(
            title = "上传限速",
            speed = vm.uploadSpeed,
            speedDst = vm.uploadSpeedDst
        ) {
            vm.updateSpeedLimit()
        }
    }
}

@Composable
private fun ProxySettingRow(vm: SettingPageVm) {
    Column {
        TitleRow(title = "代理")

        val proxyEnabled by vm.proxyEnabled.collectAsStateWithLifecycle()
        NormalCheckbox(
            checked = proxyEnabled,
            onCheckedChange = {
                vm.proxyEnabled.value = it
                vm.proxyChanged()
            },
            label = "启用代理"
        )

        VSpacer(8.dp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val proxyAddress by vm.proxyAddress.collectAsStateWithLifecycle()
            Text(text = "代理地址：")
            CustomTextArea(
                modifier = Modifier.size(300.dp, 32.dp),
                value = proxyAddress,
                singleLine = true,
                onValueChange = {
                    vm.proxyAddress.value = it
                    vm.proxyChanged()
                },
                colors = InputColors().copy(
                    borderInFocus = PrimaryColors.Bangumi_Primary
                )
            )
        }

        VSpacer(8.dp)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val proxyPort by vm.proxyPort.collectAsStateWithLifecycle()
            Text(text = "代理端口：")
            CustomTextArea(
                modifier = Modifier.size(100.dp, 32.dp),
                value = proxyPort.toString(),
                singleLine = true,
                onValueChange = {
                    vm.proxyPort.value = it.toInt()
                    vm.proxyChanged()
                },
                colors = InputColors().copy(
                    borderInFocus = PrimaryColors.Bangumi_Primary
                )
            )
        }
    }
}

@Composable
private fun TrackerListRow(vm: SettingPageVm) {
    Column {
        TitleRow(title = "Tracker列表")

        val trackerEnabled by vm.trackerEnabled.collectAsStateWithLifecycle()
        NormalCheckbox(
            checked = trackerEnabled,
            onCheckedChange = {
                vm.trackerEnabled.value = it
                vm.updateTrackSetting()
            },
            label = "下载时附加以下Tracker列表"
        )

        val trackerList by vm.trackerList.collectAsStateWithLifecycle()
        CustomTextArea(
            modifier = Modifier.fillMaxWidth().height(400.dp),
            value = trackerList,
            onValueChange = {
                vm.trackerList.value = it
                vm.updateTrackSetting()
            },
            colors = InputColors().copy(
                borderInFocus = PrimaryColors.Bangumi_Primary
            )
        )
    }
}

@Composable
private fun TitleRow(
    title: String,
) {
    Column {
        Text(
            text = title,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        VSpacer(8.dp)
        Divider(color = PrimaryColors.GRAY.color(3))
        VSpacer(8.dp)
    }
}

@Composable
private fun SpeedRow(
    title: String,
    speed: MutableStateFlow<Float>,
    speedDst: MutableStateFlow<Float>,
    valueChanged: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = title)

        val downloadSpeed by speed.collectAsStateWithLifecycle()
        val downloadSpeedDst by speedDst.collectAsStateWithLifecycle()
        SpeedSlider(value = downloadSpeed, onValueChangeFinished = valueChanged) {
            speed.value = it
            speedDst.value = (it * 2).roundToInt() / 2f
        }
        Text(text = if (downloadSpeedDst != 0f) "${downloadSpeedDst}Mb/s" else "无限制")
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SpeedSlider(
    value: Float,
    onValueChangeFinished: () -> Unit = {},
    onValueChange: (Float) -> Unit,
) {
    Slider(
        modifier = Modifier.width(300.dp),
        value = value,
        colors = SliderDefaults.colors(
            thumbColor = PrimaryColors.Bangumi_Primary,
            activeTrackColor = PrimaryColors.Bangumi_Primary,
            inactiveTrackColor = PrimaryColors.GRAY.color(4),
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent,
        ),
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        steps = 41,
        valueRange = 0f..20f
    )
}