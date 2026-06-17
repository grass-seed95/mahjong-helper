package com.mahjong.helper.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNewGame: () -> Unit = {},
    onReviews: () -> Unit = {},
    onStats: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("\uD83C\uDC04", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text("麻将助手", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("推倒胡策略分析", style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        Button(onClick = onNewGame,
            modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("开始新对局")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onReviews,
            modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("历史对局")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onStats,
            modifier = Modifier.fillMaxWidth().height(56.dp)) {
            Text("数据统计")
        }
    }
}
