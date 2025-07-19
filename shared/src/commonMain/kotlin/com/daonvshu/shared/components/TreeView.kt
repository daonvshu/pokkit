import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewResponder
import androidx.compose.foundation.relocation.bringIntoViewResponder
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.daonvshu.shared.components.HSpacer
import com.daonvshu.shared.generated.resources.Res
import com.daonvshu.shared.generated.resources.ic_arrow_down
import com.daonvshu.shared.generated.resources.ic_arrow_right
import org.jetbrains.compose.resources.painterResource
import java.util.*

// ✅ 三态状态定义
enum class CheckState {
    Checked, Unchecked, Indeterminate
}

// ✅ 树节点数据结构（使用组合方式）
data class TreeNode<T>(
    val label: String,
    val data: T? = null,
    val children: MutableList<TreeNode<T>> = mutableListOf(),
    val checkState: MutableState<CheckState> = mutableStateOf(CheckState.Checked),
    val isExpanded: MutableState<Boolean> = mutableStateOf(true),
    var parent: TreeNode<T>? = null,
    val id: String = UUID.randomUUID().toString(), // ✅ 唯一标识
)

// ✅ 设置 parent 引用
fun <T> setupTreeParentLinks(node: TreeNode<T>) {
    node.children.forEach {
        it.parent = node
        setupTreeParentLinks(it)
    }
}

// ✅ 切换选中状态
fun <T> toggleCheckState(node: TreeNode<T>, newState: CheckState) {
    node.checkState.value = newState
    node.children.forEach { toggleCheckState(it, newState) }
    updateParentState(node.parent)
}

// ✅ 更新父节点状态
fun <T> updateParentState(node: TreeNode<T>?) {
    node ?: return
    val states = node.children.map { it.checkState.value }
    node.checkState.value = when {
        states.all { it == CheckState.Checked } -> CheckState.Checked
        states.all { it == CheckState.Unchecked } -> CheckState.Unchecked
        else -> CheckState.Indeterminate
    }
    updateParentState(node.parent)
}

// ✅ 扁平化树结构
data class FlatTreeNode<T>(
    val node: TreeNode<T>,
    val indentLevel: Int
)

fun <T> flattenTree(
    nodes: List<TreeNode<T>>,
    level: Int = 0
): List<FlatTreeNode<T>> {
    val result = mutableListOf<FlatTreeNode<T>>()
    for (node in nodes) {
        result.add(FlatTreeNode(node, level))
        if (node.isExpanded.value && node.children.isNotEmpty()) {
            result.addAll(flattenTree(node.children, level + 1))
        }
    }
    return result
}

@Composable
fun measureMaxTextWidthWithStyle(
    labels: List<String>,
    trailingPadding: Dp = 0.dp,
    textStyle: TextStyle = LocalTextStyle.current
): Dp {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current

    return labels.maxOfOrNull {
        val widthPx = measurer.measure(it, style = textStyle).size.width
        with(density) { widthPx.toDp() + trailingPadding }
    } ?: 0.dp
}

// ✅ 改造后的 TreeCheckBoxView（支持 Lazy + 滚动）
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> TreeView(
    nodes: List<TreeNode<T>>,
    iconHint: Color = Color.Unspecified,
    checkStateChanged: (() -> Unit)? = null,
) {
    val listState = rememberLazyListState()
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val hScrollState = rememberScrollState()

    // ⚠️ 动态响应树结构变化
    val flatList by remember {
        derivedStateOf { flattenTree(nodes) }
    }

    // 📏 提取最大 label 宽度（文本对齐用）
    val maxLabelWidth = measureMaxTextWidthWithStyle(
        labels = flatList.map { it.node.label },
        trailingPadding = 4.dp
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(hScrollState) // 横向滚动
                ) {
                    LazyColumn(
                        state = listState,
                        flingBehavior = flingBehavior,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        itemsIndexed(flatList, key = { _, item -> item.node.id }) { _, (node, indent) ->
                            val checkState = node.checkState.value
                            val toggleableState = when (checkState) {
                                CheckState.Checked -> ToggleableState.On
                                CheckState.Unchecked -> ToggleableState.Off
                                CheckState.Indeterminate -> ToggleableState.Indeterminate
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .padding(start = (indent * 16).dp)
                            ) {
                                if (node.children.isNotEmpty()) {
                                    Icon(
                                        painter = painterResource(
                                            if (node.isExpanded.value)
                                                Res.drawable.ic_arrow_down
                                            else
                                                Res.drawable.ic_arrow_right
                                        ),
                                        contentDescription = null,
                                        tint = iconHint,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clickable {
                                                node.isExpanded.value = !node.isExpanded.value
                                            }
                                    )
                                } else {
                                    HSpacer(24.dp)
                                }

                                CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
                                    @Suppress("DEPRECATION")
                                    Row(
                                        modifier = Modifier
                                            .bringIntoViewResponder(object : BringIntoViewResponder {
                                                override fun calculateRectForParent(localRect: Rect): Rect {
                                                    return Rect.Zero
                                                }

                                                override suspend fun bringChildIntoView(localRect: () -> Rect?) {
                                                }
                                            })
                                            .clickable {
                                                val newState = if (checkState == CheckState.Checked)
                                                    CheckState.Unchecked else CheckState.Checked
                                                toggleCheckState(node, newState)
                                                checkStateChanged?.invoke()
                                            },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TriStateCheckbox(
                                            modifier = Modifier
                                                .scale(0.8f)
                                                .padding(top = 4.dp, bottom = 4.dp, end = 4.dp),
                                            state = toggleableState,
                                            onClick = {
                                                val newState = if (checkState == CheckState.Checked)
                                                    CheckState.Unchecked else CheckState.Checked
                                                toggleCheckState(node, newState)
                                                checkStateChanged?.invoke()
                                            }
                                        )

                                        Text(
                                            text = node.label,
                                            modifier = Modifier
                                                .width(maxLabelWidth)
                                                .padding(end = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ✅ 纵向滚动条
                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(listState),
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(8.dp)
                )
            }

            // ✅ 横向滚动条
            HorizontalScrollbar(
                adapter = rememberScrollbarAdapter(hScrollState),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
        }
    }
}
