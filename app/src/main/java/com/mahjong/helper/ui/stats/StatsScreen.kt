package com.mahjong.helper.ui.stats

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mahjong.helper.data.StatsCalculator
import com.mahjong.helper.data.PlayerStats
import com.mahjong.helper.data.dao.GameRecordDao
import kotlinx.coroutines.launch

@Composable
fun StatsScreen(dao: GameRecordDao) {
    var stats by remember { mutableStateOf<PlayerStats?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            stats = StatsCalculator(dao).calculate()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("数据统计", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(24.dp))

        stats?.let { s ->
            StatCard("总对局数", "${s.totalGames}")
            Spacer(Modifier.height(8.dp))
            StatCard("一位率", "${(s.firstPlaceRate * 100).toInt()}%")
            Spacer(Modifier.height(8.dp))
            StatCard("平均排名", String.format("%.1f", s.avgRank))
            Spacer(Modifier.height(8.dp))
            StatCard("最优选择率", "${(s.optimalRate * 100).toInt()}%")
        } ?: Text("暂无数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun StatCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(value, style = MaterialTheme.typography.headlineSmall)
        }
    }
}
