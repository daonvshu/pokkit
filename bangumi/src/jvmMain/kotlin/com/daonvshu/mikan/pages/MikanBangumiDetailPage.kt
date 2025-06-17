package com.daonvshu.mikan.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.daonvshu.mikan.BangumiSharedVm
import com.daonvshu.shared.components.ImageLoadingIndicator
import com.daonvshu.shared.components.TabNavBar
import com.daonvshu.shared.generated.resources.Res
import com.daonvshu.shared.generated.resources.ic_back
import com.daonvshu.shared.generated.resources.ic_download
import com.daonvshu.shared.generated.resources.ic_paw
import com.daonvshu.shared.generated.resources.ic_refresh
import org.jetbrains.compose.resources.painterResource

@Composable
fun MikanBangumiDetailPage(sharedVm: BangumiSharedVm) {
    val vm = viewModel { MikanBangumiDetailPageVm(sharedVm.detailBangumiItem) }

    LaunchedEffect(vm) {
        vm.loadImage(sharedVm.detailBangumiItem.thumbnail)
    }

    Column {
        DetailInfoBox(vm, sharedVm)
        DownloadLinkView(vm)
    }
}

@Composable
fun DetailInfoBox(vm: MikanBangumiDetailPageVm, sharedVm: BangumiSharedVm) {
    Row(
        modifier = Modifier.height(300.dp)
    ) {
        Box(
            modifier = Modifier
                .width(240.dp)
                .fillMaxHeight()
        ) {
            val image by vm.imageCache.collectAsStateWithLifecycle()
            if (image != null) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    bitmap = image!!,
                    contentDescription = null,
                )
            } else {
                ImageLoadingIndicator()
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = vm.data.title,
                    fontSize = 20.sp,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF6B4D36),
                )

                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = {
                        //refresh data
                    }
                ) {
                    Icon(
                        painterResource(Res.drawable.ic_refresh),
                        contentDescription = "",
                        tint = Color(0xFFFF639C).copy(alpha = 0.4f),
                    )
                }

                IconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = {
                        sharedVm.navHost.value = "pop"
                    }
                ) {
                    Icon(
                        painterResource(Res.drawable.ic_back),
                        contentDescription = "",
                        tint = Color(0xFFFF639C).copy(alpha = 0.4f),
                    )
                }
            }

            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = vm.data.link,
                    fontSize = 12.sp,
                    color = Color(0xFF98918F),
                )
            }

            Divider(color = Color(0xFF98918F).copy(alpha = 0.4f), thickness = 1.dp)

            CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(
                fontSize = 14.sp, color = Color(0xFF6B4D36)
            )) {
                Row(
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("集数：")
                    Text("12")
                }

                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("相关链接：")
                    val items = listOf("Source1", "Source2", "Source3", "Source4")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items.forEachIndexed { index, item ->
                            Box(
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 60.dp)
                                    .height(24.dp)
                                    .clickable(
                                        onClick = {

                                        }
                                    )
                                    .background(
                                        Color(0xFF6B4D36).copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                            ) {
                                Text(
                                    text = item,
                                    modifier = Modifier.padding(start = 10.dp, end = 10.dp)
                                        .align(Alignment.Center),
                                )
                            }
                        }
                    }
                }

                Row {
                    Text("官方网站：")
                    Text(buildAnnotatedString {
                        withLink(LinkAnnotation.Url(
                            "https://www.bilibili.com/",
                            TextLinkStyles(style = SpanStyle(color = Color(0xFF22A9C3)))
                        )) {
                            append("https://www.bilibili.com/")
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun DownloadLinkView(vm: MikanBangumiDetailPageVm) {
    Column(
        modifier = Modifier.padding(top = 12.dp)
    ) {
        var selectedIndex by remember { mutableStateOf(0) }
        val group = listOf("ANi", "LoliHouse", "漫猫字幕组", "MingY", "喵萌奶茶屋", "北宇治字幕组", "NyaaSUB", "NyaaSUB-CN", "NyaaSUB-JP", "NyaaSUB-KR", "NyaaSUB-TW", "NyaaSUB-TH")

        TabNavBar(
            titles = group,
            selectedIndex = selectedIndex,
            normalColor = Color(0xFF6B4D36),
            selectedColor = Color(0xFF22A9C3),
            scrollable = true,
            fontSize = 14.sp,
            iconScale = 0.8f,
        ) {
            selectedIndex = it
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = {
                    //refresh data
                }
            ) {
                Icon(
                    painterResource(Res.drawable.ic_refresh),
                    contentDescription = "",
                    tint = Color(0xFFFF639C).copy(alpha = 0.4f),
                )
            }

            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = {
                    //refresh data
                }
            ) {
                Icon(
                    painterResource(Res.drawable.ic_download),
                    modifier = Modifier,
                    contentDescription = "",
                    tint = Color(0xFFFF639C).copy(alpha = 0.4f),
                )
            }
        }
    }
}