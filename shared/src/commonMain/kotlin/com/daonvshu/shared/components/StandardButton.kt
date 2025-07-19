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
    val size: StandardButtonSize = StandardButtonSize.MEDIUM,
    val iconColorFollowText: Boolean = true,
    val iconTextSpace: Dp = 4.dp,
    val alignment: Alignment = Alignment.Center,
)

val LocalStandardButtonStyle = compositionLocalOf {
    StandardButtonStyle()
}

@Composable
fun StandardButton(
    text: String,
    textSize: TextUnit = 14.sp,
    color: PrimaryColors = PrimaryColors.BLUE,
    width: Dp = Dp.Unspecified,
    icon: DrawableResource? = null,
    iconColorFollowText: Boolean = true,
    iconTextSpace: Dp = 4.dp,
    alignment: Alignment = Alignment.Center,
    size: StandardButtonSize = StandardButtonSize.MEDIUM,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val clickable = onClick != null
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor by animateColorAsState(color.color(
        if (hovered) 5 else 6
    ))
    Box(
        modifier = modifier
            .width(width)
            .height(size.height)
            .clip(RoundedCornerShape(6.dp))
            .clickable(clickable, onClick = onClick ?: {})
            .hoverable(
                enabled = clickable,
                interactionSource = interactionSource,
            )
            .background(backgroundColor),
        contentAlignment = alignment
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 12.dp)
                .then(
                    if (width != Dp.Unspecified) {
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
                    tint = if (iconColorFollowText) PrimaryColors.White else Color.Unspecified,
                )
                if (text.isNotBlank()) {
                    HSpacer(iconTextSpace)
                }
            }

            TextStyleProvider(
                fontSize = textSize
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
fun StandardButton(
    text: String,
    icon: DrawableResource? = null,
    style: StandardButtonStyle = LocalStandardButtonStyle.current,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    StandardButton(
        text = text,
        textSize = style.textSize,
        color = style.color,
        width = style.width,
        icon = icon,
        iconColorFollowText = style.iconColorFollowText,
        iconTextSpace = style.iconTextSpace,
        alignment = style.alignment,
        size = style.size,
        modifier = modifier,
        onClick = onClick
    )
}

@Composable
fun StrokeButton(
    text: String,
    textSize: TextUnit = 14.sp,
    color: PrimaryColors = PrimaryColors.BLUE,
    width: Dp = Dp.Unspecified,
    icon: DrawableResource? = null,
    iconColorFollowText: Boolean = true,
    iconTextSpace: Dp = 4.dp,
    alignment: Alignment = Alignment.Center,
    size: StandardButtonSize = StandardButtonSize.MEDIUM,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val clickable = onClick != null
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val borderColor by animateColorAsState(if (hovered) color.color(5) else PrimaryColors.GRAY.color(5))
    val textColor by animateColorAsState(if (hovered) color.color(5) else PrimaryColors.Black)
    Box(
        modifier = modifier
            .width(width)
            .height(size.height)
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
        contentAlignment = alignment
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
                    tint = if (iconColorFollowText) textColor else Color.Unspecified,
                )
                if (text.isNotBlank()) {
                    HSpacer(iconTextSpace)
                }
            }

            TextStyleProvider(
                fontSize = textSize
            ) {
                Text(
                    text = text,
                    color = textColor,
                )
            }
        }
    }
}

@Composable
fun StrokeButton(
    text: String,
    icon: DrawableResource? = null,
    style: StandardButtonStyle = LocalStandardButtonStyle.current,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    StrokeButton(
        text = text,
        textSize = style.textSize,
        color = style.color,
        width = style.width,
        icon = icon,
        iconColorFollowText = style.iconColorFollowText,
        iconTextSpace = style.iconTextSpace,
        alignment = style.alignment,
        size = style.size,
        modifier = modifier,
        onClick = onClick
    )
}