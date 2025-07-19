package com.daonvshu.shared.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daonvshu.shared.utils.PrimaryColors

@Composable
fun<T> FlowRowGroup(
    modifier: Modifier = Modifier,
    title: String = "",
    items: List<T>,
    selectedValue: T? = null,
    selectedIndex: Int? = null,
    itemWidth: Dp = 60.dp,
    itemHeight: Dp = 28.dp,
    fontSize: TextUnit = 16.sp,
    padding: Dp = 10.dp,
    normalTextColor: Color = PrimaryColors.Text_Normal,
    selectedTextColor: Color = PrimaryColors.White,
    normalBackgroundColor: Color = PrimaryColors.Button_Normal.copy(alpha = 0.1f),
    selectedBackgroundColor: Color = PrimaryColors.Button_Normal,
    onClicked : (Int, T) -> Unit,
) {
    Row (
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (title.isNotEmpty()) {
            Box(
                modifier = Modifier.height(itemHeight),
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.Bold,
                    color = normalTextColor,
                    textAlign = TextAlign.End,
                    fontSize = fontSize,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = when {
                    selectedValue != null -> item == selectedValue
                    selectedIndex != null -> index == selectedIndex
                    else -> false
                }
                val color by animateColorAsState(targetValue =
                    if (isSelected) selectedBackgroundColor
                    else normalBackgroundColor
                )
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = itemWidth)
                        .height(itemHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                onClicked(index, item)
                            }
                        )
                        .background(color, shape = RoundedCornerShape(6.dp))
                ) {
                    Text(
                        text = item.toString(),
                        modifier = Modifier.padding(start = padding, end = padding).align(Alignment.Center),
                        color = if (isSelected) selectedTextColor else normalTextColor,
                        fontSize = fontSize
                    )
                }
            }
        }
    }
}