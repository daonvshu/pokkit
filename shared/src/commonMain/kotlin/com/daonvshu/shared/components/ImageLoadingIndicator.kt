package com.daonvshu.shared.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
fun ImageLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF42A5F5),
    arcAngle: Float = 270f,        // 圆弧角度
    strokeWidthDp: Float = 4f,     // 圆弧粗细
    sizeDp: Int = 36               // 组件大小
) {
    val infiniteTransition = rememberInfiniteTransition()
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 900, easing = LinearEasing),
            RepeatMode.Restart
        )
    )
    Canvas(modifier = modifier.size(sizeDp.dp)) {
        val sizePx = size.minDimension
        val strokeWidthPx = strokeWidthDp.dp.toPx()
        drawArc(
            color = color,
            startAngle = angle,
            sweepAngle = arcAngle,
            useCenter = false,
            topLeft = Offset(strokeWidthPx, strokeWidthPx),
            size = androidx.compose.ui.geometry.Size(
                width = sizePx - 2 * strokeWidthPx,
                height = sizePx - 2 * strokeWidthPx
            ),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round
            )
        )
    }
}