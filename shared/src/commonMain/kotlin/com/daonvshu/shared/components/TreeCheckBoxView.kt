package com.daonvshu.shared.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TriStateCheckbox
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.state.ToggleableState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown

// ✅ 三态状态定义
enum class CheckState {
    Checked, Unchecked, Indeterminate
}

// ✅ 树节点数据结构
data class TreeNode<T>(
    val id: String,
    val label: String,
    val children: List<TreeNode<T>> = emptyList(),
    val checkState: MutableState<CheckState> = mutableStateOf(CheckState.Unchecked),
    val isExpanded: MutableState<Boolean> = mutableStateOf(true), // 默认展开
    var parent: TreeNode<T>? = null,
    val data: T,
)

// ✅ 设置 parent 引用（初始化一次）
fun<T> setupTreeParentLinks(node: TreeNode<T>) {
    node.children.forEach {
        it.parent = node
        setupTreeParentLinks(it)
    }
}

// ✅ 切换选中状态，递归子节点并向上通知父节点
fun<T> toggleCheckState(node: TreeNode<T>, newState: CheckState) {
    node.checkState.value = newState
    node.children.forEach { toggleCheckState(it, newState) }
    updateParentState(node.parent)
}

// ✅ 向上递归更新父节点的三态状态
fun<T> updateParentState(node: TreeNode<T>?) {
    node ?: return
    val states = node.children.map { it.checkState.value }
    node.checkState.value = when {
        states.all { it == CheckState.Checked } -> CheckState.Checked
        states.all { it == CheckState.Unchecked } -> CheckState.Unchecked
        else -> CheckState.Indeterminate
    }
    updateParentState(node.parent)
}

// ✅ 核心 UI 组件
@Composable
fun<T> TreeCheckBoxView(
    nodes: List<TreeNode<T>>,
    indentLevel: Int = 0
) {
    Column {
        for (node in nodes) {
            val checkState = node.checkState.value
            val toggleableState = when (checkState) {
                CheckState.Checked -> ToggleableState.On
                CheckState.Unchecked -> ToggleableState.Off
                CheckState.Indeterminate -> ToggleableState.Indeterminate
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = (indentLevel * 16).dp)
            ) {
                // 展开图标
                if (node.children.isNotEmpty()) {
                    Icon(
                        imageVector = if (node.isExpanded.value)
                            Icons.Default.KeyboardArrowDown
                        else
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable {
                                node.isExpanded.value = !node.isExpanded.value
                            }
                    )
                } else {
                    Spacer(modifier = Modifier.width(24.dp))
                }

                // 三态复选框
                TriStateCheckbox(
                    state = toggleableState,
                    onClick = {
                        val newState = if (checkState == CheckState.Checked)
                            CheckState.Unchecked else CheckState.Checked
                        toggleCheckState(node, newState)
                    }
                )

                // 标签：只有父节点可点击展开
                Text(
                    text = node.label,
                    modifier = Modifier
                        .clickable(enabled = node.children.isNotEmpty()) {
                            node.isExpanded.value = !node.isExpanded.value
                        }
                )
            }

            // 递归子节点
            if (node.isExpanded.value && node.children.isNotEmpty()) {
                TreeCheckBoxView(node.children, indentLevel + 1)
            }
        }
    }
}
