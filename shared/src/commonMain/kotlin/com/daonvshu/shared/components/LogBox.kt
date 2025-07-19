package com.daonvshu.shared.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daonvshu.shared.utils.LogEntry
import com.daonvshu.shared.utils.PrimaryColors

@Composable
fun LogBox(
    textColor: Color = PrimaryColors.White,
    modifier: Modifier = Modifier,
    logList: List<LogEntry>
) {
    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(
            fontSize = 10.sp, lineHeight = 12.sp, color = textColor
        )
    ) {
        LazyColumn(
            modifier = modifier,
            reverseLayout = true,
            contentPadding = PaddingValues(8.dp)
        ) {
            items(logList) { entry ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = "[${entry.time}] ")
                    Text(text = entry.message)
                }
            }
        }
    }
}