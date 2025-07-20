package com.daonvshu.shared.components

import com.daonvshu.shared.utils.PrimaryColors
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daonvshu.shared.styles.TextStyleProvider
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@JvmInline
value class StandardButtonSize(val height: Dp) {
    companion object {
        val SMALL = StandardButtonSize(24.dp)
        val MEDIUM = StandardButtonSize(32.dp)
        val LARGE = StandardButtonSize(40.dp)
    }
}

data class StandardButtonStyle(
    val textSize: TextUnit = 14.sp,
    val color: PrimaryColors = PrimaryColors.BLUE,
    val width: Dp = Dp.Unspecified,
    val iconColorFollowText: Boolean = true,
    val iconTextSpace: Dp = 4.dp,
    val alignment: Alignment = Alignment.Center,
    val size: StandardButtonSize = StandardButtonSize.MEDIUM,
    val enabled: Boolean = true,
) {
    fun merge(other: StandardButtonStyle): StandardButtonStyle {
        return StandardButtonStyle(
            textSize = if (other.textSize != TextUnit.Unspecified) other.textSize else textSize,
            color = if (other.color != PrimaryColors.Unspecified) other.color else color,
            width = if (other.width != Dp.Unspecified) other.width else width,
            iconColorFollowText = other.iconColorFollowText,
            iconTextSpace = if (other.iconTextSpace != Dp.Unspecified) other.iconTextSpace else iconTextSpace,
            alignment = other.alignment,
            size = other.size,
            enabled = other.enabled,
        )
    }
}

val LocalStandardButtonStyle = compositionLocalOf {
    StandardButtonStyle()
}

@Composable
fun StandardButton(
    text: String,
    icon: DrawableResource? = null,
    textSize: TextUnit = TextUnit.Unspecified,
    color: PrimaryColors = PrimaryColors.Unspecified,
    width: Dp = Dp.Unspecified,
    iconColorFollowText: Boolean = true,
    iconTextSpace: Dp = Dp.Unspecified,
    alignment: Alignment = Alignment.Center,
    size: StandardButtonSize = StandardButtonSize.MEDIUM,
    enabled: Boolean = true,
    style: StandardButtonStyle = LocalStandardButtonStyle.current,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val curStyle = style.merge(
        StandardButtonStyle(
            textSize = textSize,
            color = color,
            width = width,
            iconColorFollowText = iconColorFollowText,
            iconTextSpace = iconTextSpace,
            alignment = alignment,
            size = size,
            enabled = enabled,
        )
    )

    val clickable = onClick != null && curStyle.enabled
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor by animateColorAsState(
        curStyle.color.color(
            if (!curStyle.enabled) 2 else if (hovered) 5 else 6
        )
    )
    Box(
        modifier = modifier
            .width(curStyle.width)
            .height(curStyle.size.height)
            .clip(RoundedCornerShape(6.dp))
            .clickable(clickable, onClick = onClick ?: {})
            .hoverable(
                enabled = clickable,
                interactionSource = interactionSource,
            )
            .background(backgroundColor),
        contentAlignment = curStyle.alignment
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp)
                .then(
                    if (curStyle.width != Dp.Unspecified) {
                        Modifier.fillMaxWidth()
                    } else {
                        Modifier
                    }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon), // 加载图标
                    contentDescription = null,
                    tint = if (curStyle.iconColorFollowText) PrimaryColors.White else Color.Unspecified,
                )
                if (text.isNotBlank()) {
                    HSpacer(curStyle.iconTextSpace)
                }
            }

            TextStyleProvider(
                fontSize = curStyle.textSize
            ) {
                Text(
                    text = text,
                    color = PrimaryColors.White,
                )
            }
        }
    }
}

@Composable
fun StrokeButton(
    text: String,
    icon: DrawableResource? = null,
    textSize: TextUnit = TextUnit.Unspecified,
    color: PrimaryColors = PrimaryColors.Unspecified,
    width: Dp = Dp.Unspecified,
    iconColorFollowText: Boolean = true,
    iconTextSpace: Dp = Dp.Unspecified,
    alignment: Alignment = Alignment.Center,
    size: StandardButtonSize = StandardButtonSize.MEDIUM,
    enabled: Boolean = true,
    style: StandardButtonStyle = LocalStandardButtonStyle.current,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val curStyle = style.merge(
        StandardButtonStyle(
            textSize = textSize,
            color = color,
            width = width,
            iconColorFollowText = iconColorFollowText,
            iconTextSpace = iconTextSpace,
            alignment = alignment,
            size = size,
            enabled = enabled,
        )
    )

    val clickable = onClick != null && curStyle.enabled
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val borderColor by animateColorAsState(
        if (!curStyle.enabled) curStyle.color.color(2)
        else if (hovered) curStyle.color.color(5) else PrimaryColors.GRAY.color(5)
    )
    val textColor by animateColorAsState(
        if (!curStyle.enabled) curStyle.color.color(2)
        else if (hovered) curStyle.color.color(5) else PrimaryColors.Black
    )
    Box(
        modifier = modifier
            .width(curStyle.width)
            .height(curStyle.size.height)
            .clip(RoundedCornerShape(6.dp))
            .clickable(clickable, onClick = onClick ?: {})
            .hoverable(
                enabled = clickable,
                interactionSource = interactionSource,
            ).border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(6.dp),
            ),
        contentAlignment = curStyle.alignment
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(icon), // 加载图标
                    contentDescription = null,
                    tint = if (curStyle.iconColorFollowText) textColor else Color.Unspecified,
                )
                if (text.isNotBlank()) {
                    HSpacer(curStyle.iconTextSpace)
                }
            }

            TextStyleProvider(
                fontSize = curStyle.textSize
            ) {
                Text(
                    text = text,
                    color = textColor,
                )
            }
        }
    }
}