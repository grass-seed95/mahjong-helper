package com.mahjong.helper.ui.review

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mahjong.helper.data.dao.GameRecordDao
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReviewListScreen(dao: GameRecordDao, onGameClick: (Long) -> Unit) {
    val games by dao.allGames().collectAsState(initial = emptyList())

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("历史对局", style = MaterialTheme.typography.titleLarge) }
        item { Spacer(Modifier.height(12.dp)) }

        if (games.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center) {
                    Text("暂无对局记录", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        items(games) { game ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    .clickable { onGameClick(game.id) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            formatDate(game.startedAt),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            "${game.totalRounds}轮 | 排名: ${game.finalRank ?: "进行中"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    game.finalRank?.let {
                        Text("#$it", style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
