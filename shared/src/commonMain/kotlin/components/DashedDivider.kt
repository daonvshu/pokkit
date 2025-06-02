package components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class DividerOrientation { Vertical, Horizontal }

@Composable
fun DashedDivider(
    orientation: DividerOrientation,
    color: Color = Color(0xFFE0E0E0),
    dashLength: Dp = 6.dp,
    dashGap: Dp = 4.dp,
    thickness: Dp = 1.dp,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = when (orientation) {
            DividerOrientation.Vertical -> modifier
                .fillMaxHeight()
                .width(thickness)
            DividerOrientation.Horizontal -> modifier
                .fillMaxWidth()
                .height(thickness)
        }
    ) {
        val isVertical = orientation == DividerOrientation.Vertical
        val fullLength = if (isVertical) size.height else size.width
        val dashPx = dashLength.toPx()
        val gapPx = dashGap.toPx()
        val halfLine = if (isVertical) size.width / 2 else size.height / 2
        var position = 0f
        while (position < fullLength) {
            val end = (position + dashPx).coerceAtMost(fullLength)
            if (isVertical) {
                drawLine(
                    color = color,
                    start = Offset(halfLine, position),
                    end = Offset(halfLine, end),
                    strokeWidth = thickness.toPx()
                )
            } else {
                drawLine(
                    color = color,
                    start = Offset(position, halfLine),
                    end = Offset(end, halfLine),
                    strokeWidth = thickness.toPx()
                )
            }
            position += dashPx + gapPx
        }
    }
}
