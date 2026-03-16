# SearchHistoryDemo

一个基于 Jetpack Compose 实现的搜索历史组件，支持多行显示、展开/收起功能，以及智能的按钮位置管理。

## 功能特性

- ✅ 流式布局（Flow Layout）：历史记录标签自动换行排列
- ✅ 智能展开/收起：超过 2 行时显示展开按钮
- ✅ 按钮位置优化：
  - 收起状态：展开按钮始终在第 2 行末尾
  - 展开状态：收起按钮在最后一行末尾（空间不足时单独占一行）
- ✅ 随机数据生成：支持一键刷新生成随机测试数据
- ✅ 图标化交互：使用箭头图标替代文本按钮
- ✅ 统一样式：所有元素保持一致的视觉风格

## 核心实现原理

### 1. 自定义布局系统

项目使用了 Compose 的 `Layout` 组件来实现自定义的流式布局，这是整个功能的核心。

```kotlin
Layout(
    content = {
        // 历史记录标签
        items.forEach { item ->
            HistoryChip(text = item.text, onClick = { onItemClick(item) })
        }
        // 展开/收起按钮
        ExpandCollapseButton(
            isExpanded = isExpanded,
            onClick = onToggleExpand
        )
    },
    modifier = modifier
) { measurables, constraints ->
    // 布局测量和放置逻辑
}
```

### 2. 布局测量策略

#### 2.1 宽松约束测量

为了确保元素根据内容大小测量，而不是占据整个容器宽度，使用了宽松约束：

```kotlin
val chipPlaceables = chipMeasurables.map { 
    it.measure(androidx.compose.ui.unit.Constraints()) 
}
val buttonPlaceable = buttonMeasurable.measure(
    androidx.compose.ui.unit.Constraints()
)
```

这样可以确保每个元素只占用其实际需要的空间。

#### 2.2 行布局计算

通过遍历所有可放置的元素，计算每行应该包含哪些元素：

```kotlin
val rows = mutableListOf<List<Placeable>>()
var currentRow = mutableListOf<Placeable>()
var currentRowWidth = 0

chipPlaceables.forEach { placeable ->
    if (currentRow.isEmpty()) {
        currentRow.add(placeable)
        currentRowWidth = placeable.width
    } else {
        if (currentRowWidth + horizontalSpacingPx + placeable.width <= constraints.maxWidth) {
            // 当前行还能放下，添加到当前行
            currentRow.add(placeable)
            currentRowWidth += horizontalSpacingPx + placeable.width
        } else {
            // 当前行放不下，换行
            rows.add(currentRow.toList())
            currentRow = mutableListOf(placeable)
            currentRowWidth = placeable.width
        }
    }
}
```

### 3. 展开/收起逻辑

#### 3.1 收起状态（显示前 2 行）

```kotlin
if (!isExpanded) {
    val displayRows = rows.take(2)
    displayRows.forEachIndexed { index, row ->
        if (index < displayRows.size - 1) {
            // 第 1 行：直接显示
            finalRows.add(row.toList())
        } else {
            // 第 2 行：需要为按钮腾出空间
            val lastRow = row.toMutableList()
            val buttonWidth = buttonPlaceable.width
            val currentRowWidthPx = lastRow.sumOf { it.width } + 
                (lastRow.size - 1) * horizontalSpacingPx

            if (currentRowWidthPx + horizontalSpacingPx + buttonWidth <= constraints.maxWidth) {
                // 空间足够，按钮放在第 2 行末尾
                lastRow.add(buttonPlaceable)
                finalRows.add(lastRow.toList())
            } else {
                // 空间不足，移除一些元素为按钮腾出空间
                val adjustedRow = mutableListOf<Placeable>()
                var adjustedWidth = 0
                for (placeable in lastRow) {
                    if (adjustedWidth + horizontalSpacingPx + placeable.width + 
                        horizontalSpacingPx + buttonWidth <= constraints.maxWidth) {
                        adjustedRow.add(placeable)
                        adjustedWidth += placeable.width + horizontalSpacingPx
                    } else {
                        break
                    }
                }
                adjustedRow.add(buttonPlaceable)
                finalRows.add(adjustedRow.toList())
            }
        }
    }
}
```

**关键点：**
- 只显示前 2 行内容
- 按钮始终在第 2 行末尾
- 如果第 2 行空间不足，会从后往前移除元素，确保按钮可见

#### 3.2 展开状态（显示所有行）

```kotlin
if (isExpanded) {
    // 显示所有行
    rows.forEach { row ->
        finalRows.add(row.toList())
    }
    
    // 处理最后一行的按钮位置
    val lastRow = finalRows.last().toMutableList()
    val buttonWidth = buttonPlaceable.width
    val currentRowWidthPx = lastRow.sumOf { it.width } + 
        (lastRow.size - 1) * horizontalSpacingPx

    if (currentRowWidthPx + horizontalSpacingPx + buttonWidth <= constraints.maxWidth) {
        // 空间足够，按钮放在最后一行末尾
        lastRow.add(buttonPlaceable)
        finalRows[finalRows.size - 1] = lastRow.toList()
    } else {
        // 空间不足，按钮单独占一行
        finalRows.add(listOf(buttonPlaceable))
    }
}
```

**关键点：**
- 显示所有元素，不丢失任何内容
- 按钮优先放在最后一行末尾
- 如果最后一行空间不足，按钮单独占新的一行

### 4. 元素放置

计算好所有行的布局后，进行实际的元素放置：

```kotlin
layout(constraints.maxWidth, totalHeight) {
    var y = 0
    finalRows.forEach { row ->
        var x = 0
        row.forEach { placeable ->
            placeable.place(x, y)
            x += placeable.width + horizontalSpacingPx
        }
        y += (row.firstOrNull()?.height ?: 0) + verticalSpacingPx
    }
}
```

### 5. 状态管理

使用 `remember` 和 `mutableStateOf` 管理展开/收起状态：

```kotlin
var isExpanded by remember { mutableStateOf(false) }
```

当用户点击按钮时，切换状态：

```kotlin
onToggleExpand = { isExpanded = !isExpanded }
```

### 6. 随机数据生成

为了方便测试，实现了随机数据生成功能：

```kotlin
fun generateRandomHistoryItems(minCount: Int, maxCount: Int): List<HistoryItem> {
    val count = (minCount..maxCount).random()
    val keywords = listOf("Android", "Kotlin", "Compose", ...)
    val verbs = listOf("开发", "编程", "学习", ...)
    
    val items = mutableListOf<HistoryItem>()
    repeat(count) { index ->
        val keyword = keywords.random()
        val verb = verbs.random()
        val text = when ((1..3).random()) {
            1 -> keyword
            2 -> "$keyword$verb"
            else -> "$keyword${verb}教程"
        }
        items.add(HistoryItem(index.toString(), text))
    }
    return items
}
```

使用 `refreshKey` 来触发数据重新生成：

```kotlin
var refreshKey by remember { mutableStateOf(0) }
val historyItems = remember(refreshKey) { 
    generateRandomHistoryItems(10, 20) 
}

Button(onClick = { refreshKey++ }) {
    Text("刷新文案")
}
```

### 7. 组件设计

#### 7.1 HistoryChip（历史记录标签）

使用 `Box` 作为容器，确保元素是块级元素：

```kotlin
@Composable
fun HistoryChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp))
            .border(width = 1.dp, color = Color(0xFFE0E0E0), shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 14.sp,
            color = Color(0xFF333333),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
```

#### 7.2 ExpandCollapseButton（展开/收起按钮）

使用图标替代文本，保持与其他元素一致的样式：

```kotlin
@Composable
fun ExpandCollapseButton(isExpanded: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .background(color = Color(0xFFF5F5F5), shape = RoundedCornerShape(16.dp))
            .border(width = 1.dp, color = Color(0xFFE0E0E0), shape = RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = if (isExpanded) "收起" else "展开",
            modifier = Modifier.padding(6.dp).size(24.dp),
            tint = Color(0xFF2196F3)
        )
    }
}
```

## 技术要点

### 1. 自定义 Layout 的优势

- **精确控制**：可以完全控制元素的测量和放置逻辑
- **性能优化**：只测量一次，避免重复测量
- **灵活性**：可以实现复杂的布局需求

### 2. 宽松约束的重要性

使用宽松约束确保元素根据内容大小测量，而不是占据整个容器宽度：

```kotlin
it.measure(androidx.compose.ui.unit.Constraints())
```

### 3. 状态驱动的 UI

使用 `remember` 和 `mutableStateOf` 实现响应式 UI，状态变化自动触发重组。

### 4. 无障碍支持

为图标添加 `contentDescription`，提升无障碍体验。

## 项目结构

```
app/src/main/java/com/example/searchhistorydemo/
├── MainActivity.kt                    # 主 Activity
├── model/
│   └── HistoryItem.kt                # 历史记录数据模型
└── ui/
    ├── components/
    │   ├── SearchHistoryView.kt      # 搜索历史主组件
    │   └── FlowLayout.kt             # 流式布局组件
    └── theme/
        ├── Color.kt                  # 颜色定义
        ├── Theme.kt                  # 主题配置
        └── Type.kt                   # 字体样式
```

## 运行项目

1. 克隆项目到本地
2. 使用 Android Studio 打开项目
3. 点击运行按钮或执行 `./gradlew assembleDebug`
4. 在模拟器或真机上查看效果

## 依赖项

- Jetpack Compose
- Material Design 3
- Kotlin Coroutines

## 总结

这个项目展示了如何使用 Jetpack Compose 的自定义 Layout 功能实现复杂的流式布局需求。核心思想是：

1. **精确测量**：使用宽松约束确保元素按内容大小测量
2. **智能布局**：根据容器宽度自动计算每行应该包含的元素
3. **状态管理**：使用 Compose 的状态管理实现响应式 UI
4. **用户体验**：优化按钮位置，确保始终可见且易于点击

通过这种方式，我们实现了一个既美观又实用的搜索历史组件，可以作为类似需求的参考实现。
