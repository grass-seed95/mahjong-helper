package com.mahjong.helper.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mahjong.helper.data.dao.GameRecordDao
import com.mahjong.helper.data.entity.RoundEntity
import kotlinx.coroutines.launch

@Composable
fun ReviewDetailScreen(dao: GameRecordDao, gameId: Long) {
    var rounds by remember { mutableStateOf<List<RoundEntity>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(gameId) {
        scope.launch { rounds = dao.roundsForGame(gameId) }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("对局回放", style = MaterialTheme.typography.titleLarge) }
        item { Spacer(Modifier.height(16.dp)) }

        items(rounds) { round ->
            val borderColor = when {
                round.wasOptimal == true -> Color(0xFF22C55E)
                round.wasOptimal == false -> Color(0xFFEF4444)
                else -> Color(0xFF3B82F6)
            }

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "第${round.roundNumber}轮",
                        style = MaterialTheme.typography.labelMedium,
                        color = borderColor
                    )
                    Text("手牌: ${round.handTiles}",
                        style = MaterialTheme.typography.bodySmall)
                    round.suggestedDiscard?.let {
                        Text("建议打: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF22C55E))
                    }
                    round.actualDiscard?.let {
                        Text("实际打: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (round.wasOptimal == false) Color(0xFFEF4444)
                                    else Color.Unspecified)
                    }
                    Text("向听数: ${round.shantenBefore} → ${round.shantenAfter ?: "?"}",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
