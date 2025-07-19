package com.daonvshu.shared.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HSpacer(
    width: Dp = 4.dp,
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier.width(width)
    )
}

@Composable
fun RowScope.HSpacer(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier.weight(1f)
    )
}

@Composable
fun VSpacer(
    height: Dp = 4.dp,
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier.height(height)
    )
}

@Composable
fun ColumnScope.VSpacer(
    modifier: Modifier = Modifier
) {
    Spacer(
        modifier = modifier.weight(1f)
    )
}