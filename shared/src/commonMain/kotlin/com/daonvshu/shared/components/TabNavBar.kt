package com.daonvshu.shared.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daonvshu.shared.generated.resources.Res
import com.daonvshu.shared.generated.resources.ic_paw
import org.jetbrains.compose.resources.painterResource

@Composable
fun TabNavBar(
    modifier: Modifier = Modifier,
    titles: List<String>,
    selectedIndex: Int,
    normalColor: Color,
    selectedColor: Color,
    fontSize: TextUnit = 16.sp,
    iconScale: Float = 1f,
    scrollable: Boolean = false,
    onClicked: (Int) -> Unit,
) {
    val tabContent = @Composable {
        titles.forEachIndexed { index, title ->
            val scale by animateFloatAsState(if (index == selectedIndex) iconScale else 0f)
            Tab(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧箭头圆圈
                        Box(
                            modifier = Modifier.size(18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                modifier = Modifier.scale(scale),
                                painter = painterResource(Res.drawable.ic_paw),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(selectedColor)
                            )
                        }
                        Spacer(Modifier.width(6.dp))

                        val selectedColor by animateColorAsState(
                            if (index == selectedIndex) selectedColor else normalColor
                        )
                        Text(
                            text = title,
                            color = selectedColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = fontSize
                        )
                    }
                },
                selected = selectedIndex == index,
                onClick = {
                    onClicked(index)
                },
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }

    val indicator: @Composable (List<TabPosition>) -> Unit = { tabPositions ->
        TabRowDefaults.Indicator(
            Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
            color = selectedColor
        )
    }

    if (scrollable) {
        ScrollableTabRow(
            modifier = modifier,
            selectedTabIndex = selectedIndex,
            backgroundColor = Color.Transparent,
            indicator = indicator,
            tabs = tabContent,
            edgePadding = 0.dp,
        )
    } else {
        TabRow(
            modifier = modifier,
            selectedTabIndex = selectedIndex,
            backgroundColor = Color.Transparent,
            indicator = indicator,
            tabs = tabContent,
        )
    }
}