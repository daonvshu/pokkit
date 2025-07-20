package com.daonvshu.shared.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daonvshu.shared.utils.PrimaryColors
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@JvmInline
value class IconButtonSize(val size: Dp) {
    companion object {
        val SMALL = IconButtonSize(24.dp)
        val MEDIUM = IconButtonSize(32.dp)
        val LARGE = IconButtonSize(40.dp)
    }
}

enum class ShapeIconButtonType {
    CIRCLE,
    SQUARE
}

data class ShapeIconButtonStyle(
    val color: PrimaryColors = PrimaryColors.GRAY,
    val alpha: Float = 0.6f,
    val size: IconButtonSize = IconButtonSize.MEDIUM,
    val shape: ShapeIconButtonType = ShapeIconButtonType.CIRCLE,
) {
    fun merge(other: ShapeIconButtonStyle): ShapeIconButtonStyle {
        return ShapeIconButtonStyle(
            color = if (other.color != PrimaryColors.Unspecified) other.color else color,
            alpha = other.alpha,
            size = other.size,
            shape = other.shape
        )
    }
}

val LocalShapeIconButtonStyle = compositionLocalOf {
    ShapeIconButtonStyle()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ShapeIconButton(
    resource: DrawableResource,
    color: PrimaryColors = PrimaryColors.Unspecified,
    alpha: Float = 0.6f,
    size: IconButtonSize = IconButtonSize.MEDIUM,
    shape: ShapeIconButtonType = ShapeIconButtonType.CIRCLE,
    style: ShapeIconButtonStyle = LocalShapeIconButtonStyle.current,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val curStyle = style.merge(
        ShapeIconButtonStyle(
            color = color,
            alpha = alpha,
            size = size,
            shape = shape
        )
    )

    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    val iconColor by animateColorAsState(targetValue =
        curStyle.color.color(if (hovered) 5 else 6, alpha = curStyle.alpha)
    )

    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        IconButton(
            modifier = modifier
                .size(curStyle.size.size)
                .clip(if (curStyle.shape == ShapeIconButtonType.CIRCLE) CircleShape else RoundedCornerShape(6.dp))
                .hoverable(
                    enabled = onClick != null,
                    interactionSource = interactionSource,
                ),
            onClick = onClick ?: {}
        ) {
            Icon(
                painterResource(resource),
                contentDescription = "",
                tint = iconColor,
            )
        }
    }
}
