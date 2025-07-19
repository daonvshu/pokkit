package com.daonvshu.shared.styles

import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun TextStyleProvider(
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontWeight: FontWeight? = null,
    content: @Composable () -> Unit,
) {
    val density = LocalDensity.current
    val extraSp = with(density) { 8.sp }

    // 将 TextUnit 转成 Float（以 sp 为单位）
    val fontSizeValue = fontSize.value
    val extraValue = extraSp.value

    // 相加
    val lineHeightValue = fontSizeValue + extraValue

    // 重新构造 TextUnit (sp单位)
    val lineHeight = lineHeightValue.sp

    val textStyle = TextStyle(
        color = color,
        fontSize = fontSize,
        lineHeight = lineHeight,
        fontWeight = fontWeight,
    )

    CompositionLocalProvider(LocalTextStyle provides textStyle) {
        content()
    }
}