package com.daonvshu.shared.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daonvshu.shared.utils.PrimaryColors

data class InputColors(
    val border: Color = PrimaryColors.GRAY.color(level = 4),
    val borderInFocus: Color = PrimaryColors.BLUE.color(),
    val borderDisabled: Color = PrimaryColors.GRAY.color(level = 3),
    val borderHinted: Color = PrimaryColors.RED.color(),

    val background: Color = Color.Transparent,
    val backgroundDisabled: Color = PrimaryColors.GRAY.color(level = 2),

    val text: Color = PrimaryColors.Text_Normal,
    val textDisabled: Color = PrimaryColors.GRAY.color(level = 7)
)

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CustomTextArea(
    value: String = "",
    onValueChange: (String) -> Unit,

    fontSize: TextUnit = 14.sp,
    singleLine: Boolean = false,

    colors: InputColors = InputColors(),
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    hint: String = "",
) {
    val hoverInteraction = remember { MutableInteractionSource() }
    val isHovered by hoverInteraction.collectIsHoveredAsState()
    var isFocused by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(when {
        isFocused || isHovered -> colors.borderInFocus
        else -> colors.border
    })

    BasicTextField(
        value = value,
        onValueChange = {
            if (enabled) {
                onValueChange(it)
            }
        },
        enabled = enabled,
        modifier = modifier
            .background(if (enabled) colors.background else colors.backgroundDisabled, RoundedCornerShape(8.dp))
            .border(0.5.dp, borderColor, RoundedCornerShape(8.dp))
            .hoverable(hoverInteraction)
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .onFocusChanged { focusState -> isFocused = focusState.isFocused },
        singleLine = singleLine,
        textStyle = TextStyle(
            color = if (enabled) colors.text else colors.textDisabled,
            fontSize = fontSize
        ),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.TopStart,
                modifier = Modifier.fillMaxSize()
            ) {
                if (value.isEmpty() && hint.isNotEmpty()) {
                    Text(
                        text = hint,
                        color = colors.textDisabled,
                        fontSize = fontSize
                    )
                }
                innerTextField()
            }
        }
    )
}