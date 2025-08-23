package com.daonvshu.shared.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.mataku.middleellipsistext.MiddleEllipsisText

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NormalCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    label: String? = null,
    labelColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Row(
            modifier = modifier
                .clickable(
                    enabled,
                    onClick = {
                        onCheckedChange(!checked)
                    }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                modifier = Modifier.scale(0.8f).padding(top = 4.dp, bottom = 4.dp, end = 4.dp),
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
            )

            if (label != null) {
                MiddleEllipsisText(label, color = labelColor)
            }
        }
    }
}