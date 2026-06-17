package com.mahjong.helper.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onNewGame: () -> Unit = {},
    onLiveMonitor: () -> Unit = {},
    onReviews: () -> Unit = {},
    onStats: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("\uD83C\uDC04", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text("麻将助手", style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("推倒胡策略分析", style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))

        // Primary: Live monitor
        Button(
            onClick = onLiveMonitor,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("实时监控")
        }
        Spacer(Modifier.height(12.dp))

        // Secondary: Manual input
        OutlinedButton(
            onClick = onNewGame,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("手动分析")
        }
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onReviews,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("历史对局")
        }
        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onStats,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("数据统计")
        }
    }
}
