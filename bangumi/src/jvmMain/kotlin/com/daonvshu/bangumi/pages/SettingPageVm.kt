package com.daonvshu.bangumi.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.shared.backendservice.BackendDataObserver
import com.daonvshu.shared.backendservice.GlobalSpeedLimitUpdate
import com.daonvshu.shared.backendservice.ProxyInfoSync
import com.daonvshu.shared.backendservice.SpecialIntCommand
import com.daonvshu.shared.backendservice.TrackerListUpdateRequest
import com.daonvshu.shared.backendservice.sendToBackend
import com.daonvshu.shared.settings.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

class SettingPageVm : ViewModel() {
    // 限速设置
    val downloadSpeed = MutableStateFlow(0f)
    val downloadSpeedDst = MutableStateFlow(0f)
    val uploadSpeed = MutableStateFlow(0f)
    val uploadSpeedDst = MutableStateFlow(0f)

    // 代理设置
    val proxyEnabled = MutableStateFlow(AppSettings.settings.general.proxyEnabled)
    val proxyAddress = MutableStateFlow(AppSettings.settings.general.proxyAddress)
    val proxyPort = MutableStateFlow(AppSettings.settings.general.proxyPort)

    // Trackers设置
    val trackerEnabled = MutableStateFlow(false)
    val trackerList = MutableStateFlow("")

    init {
        BackendDataObserver.globalSpeedLimit.onEach {
            if (it != null) {
                downloadSpeed.value = it.download / 1024f / 1024f
                downloadSpeedDst.value = (downloadSpeed.value * 2).roundToInt() / 2f
                uploadSpeed.value = it.upload / 1024f / 1024f
                uploadSpeedDst.value = (uploadSpeed.value * 2).roundToInt() / 2f
            }
        }.launchIn(viewModelScope)

        BackendDataObserver.trackerListSetting.onEach {
            if (it != null) {
                trackerEnabled.value = it.enabled
                trackerList.value = it.trackers
            }
        }.launchIn(viewModelScope)
    }

    fun loadSettings() {
        SpecialIntCommand.GLOBAL_SPEED_LIMIT_REQUEST.sendToBackend()
        SpecialIntCommand.TRACKER_LIST_REQUEST.sendToBackend()
    }

    fun updateSpeedLimit() {
        GlobalSpeedLimitUpdate(
            download = (downloadSpeed.value * 1024f * 1024f).toInt(),
            upload = (uploadSpeed.value * 1024f * 1024f).toInt()
        ).sendToBackend()
    }

    fun proxyChanged() {
        AppSettings.settings.general.proxyEnabled = proxyEnabled.value
        AppSettings.settings.general.proxyAddress = proxyAddress.value
        AppSettings.settings.general.proxyPort = proxyPort.value
        AppSettings.notifyProxyChanged()
        AppSettings.save()
        ProxyInfoSync(
            enabled = AppSettings.settings.general.proxyEnabled,
            proxyAddress = AppSettings.settings.general.proxyAddress,
            proxyPort = AppSettings.settings.general.proxyPort,
        ).sendToBackend()
    }

    fun updateTrackSetting() {
        TrackerListUpdateRequest(
            enable = trackerEnabled.value,
            trackers = trackerList.value,
        ).sendToBackend()
    }
}