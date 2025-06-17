package com.daonvshu.mikan.pages

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.daonvshu.mikan.network.MikanApi
import com.daonvshu.shared.database.schema.MikanDataRecord
import com.daonvshu.shared.utils.ImageCacheLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MikanBangumiDetailPageVm(val data: MikanDataRecord): ViewModel() {

    val imageCache = MutableStateFlow<ImageBitmap?>(null)

    fun loadImage(url: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val imageData = try {
                ImageCacheLoader.getImage(url, "mikan_image", MikanApi.apiService::getImage)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            withContext(Dispatchers.Main) {
                imageCache.value = imageData
            }
        }
    }
}