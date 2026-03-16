package com.example.searchhistorydemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.searchhistorydemo.model.HistoryItem
import com.example.searchhistorydemo.ui.components.SearchHistoryView
import com.example.searchhistorydemo.ui.theme.SearchHistoryDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SearchHistoryDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SearchHistoryScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun SearchHistoryScreen(modifier: Modifier = Modifier) {
    var refreshKey by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "搜索历史",
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        val shortHistoryItems = remember(refreshKey) { generateRandomHistoryItems(3, 5) }

        Text(
            text = "少于2行的历史记录（不显示展开/收起按钮）：",
            fontSize = 14.sp,
            color = androidx.compose.ui.graphics.Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        SearchHistoryView(
            historyItems = shortHistoryItems,
            onItemClick = { item ->
                println("点击了: ${item.text}")
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        val longHistoryItems = remember(refreshKey) { generateRandomHistoryItems(10, 20) }

        Text(
            text = "超过2行的历史记录（显示展开/收起按钮）：",
            fontSize = 14.sp,
            color = androidx.compose.ui.graphics.Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        SearchHistoryView(
            historyItems = longHistoryItems,
            onItemClick = { item ->
                println("点击了: ${item.text}")
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { refreshKey++ },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Text("刷新文案")
        }
    }
}

fun generateRandomHistoryItems(minCount: Int, maxCount: Int): List<HistoryItem> {
    val count = (minCount..maxCount).random()
    val keywords = listOf(
        "Android", "Kotlin", "Compose", "Jetpack", "Material",
        "MVVM", "Room", "Retrofit", "Coroutines", "DataStore",
        "Navigation", "Hilt", "WorkManager", "LiveData", "ViewModel",
        "RecyclerView", "Fragment", "Activity", "Service", "Broadcast",
        "ContentProvider", "SQLite", "SharedPreferences", "Gson", "Moshi",
        "OkHttp", "Glide", "Coil", "Lottie", "CameraX",
        "Firebase", "Google", "Maps", "Location", "Push",
        "Notification", "Permission", "Storage", "File", "Network",
        "HTTP", "REST", "API", "JSON", "XML",
        "Gradle", "Maven", "Git", "CI/CD", "Unit Test",
        "UI Test", "Debug", "Log", "Performance", "Memory",
        "Thread", "Process", "Lifecycle", "State", "Event",
        "Animation", "Transition", "Gesture", "Touch", "Scroll",
        "Layout", "View", "Canvas", "Paint", "Path",
        "Bitmap", "Drawable", "Color", "Theme", "Style",
        "Resource", "Asset", "Manifest", "Build", "Release"
    )

    val verbs = listOf(
        "开发", "编程", "学习", "教程", "实战",
        "入门", "进阶", "精通", "指南", "手册",
        "原理", "源码", "架构", "设计", "优化",
        "调试", "测试", "部署", "发布", "维护",
        "更新", "升级", "迁移", "重构", "集成"
    )

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