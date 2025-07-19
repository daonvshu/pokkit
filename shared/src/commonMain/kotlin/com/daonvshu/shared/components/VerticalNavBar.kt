package com.daonvshu.shared.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daonvshu.shared.generated.resources.Res
import com.daonvshu.shared.generated.resources.ic_paw
import org.jetbrains.compose.resources.painterResource

@Composable
fun VerticalNavBar(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    normalColor: Color,
    selectedColor: Color,
    onSelect: (Int) -> Unit,
) {
    Column(
        modifier
            .padding(vertical = 20.dp, horizontal = 12.dp),
    ) {
        items.forEachIndexed { i, item ->
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null   // 无点击高亮
                    ) { onSelect(i) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                val scale by animateFloatAsState(if (i == selectedIndex) 1f else 0f)
                // 左侧箭头圆圈
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape), // 灰色圆圈
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        modifier = Modifier.scale(scale),
                        painter = painterResource(Res.drawable.ic_paw),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(selectedColor)
                    )
                }
                HSpacer(6.dp)

                val selectedColor by animateColorAsState(if (i == selectedIndex) selectedColor else normalColor)
                Text(
                    text = item,
                    color = selectedColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
