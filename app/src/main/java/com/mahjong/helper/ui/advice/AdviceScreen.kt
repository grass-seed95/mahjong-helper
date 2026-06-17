package com.mahjong.helper.ui.advice

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mahjong.helper.engine.DiscardAdvisor
import com.mahjong.helper.engine.DiscardRecommendation
import com.mahjong.helper.engine.model.GameState
import com.mahjong.helper.engine.model.Hand

@Composable
fun AdviceScreen(hand: Hand) {
    val advisor = remember { DiscardAdvisor() }
    val state = remember { GameState(hand) }
    val recommendations = remember { advisor.recommend(hand, state) }
    val best = advisor.bestDiscard(recommendations)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("分析结果", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        best?.let { rec ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("建议打出", style = MaterialTheme.typography.labelMedium)
                    Text(
                        rec.tile.toString(),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(rec.reason, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ScoreBadge("安全", rec.safetyScore)
                        ScoreBadge("进张", rec.acceptanceCount.toString())
                        ScoreBadge("向听", rec.shantenAfter.toString())
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("全部候选 (按综合分排序)", style = MaterialTheme.typography.titleSmall)

        LazyColumn {
            items(recommendations) { rec ->
                DiscardCard(rec)
            }
        }
    }
}

@Composable
fun ScoreBadge(label: String, value: Any) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun DiscardCard(rec: DiscardRecommendation) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(rec.tile.toString(), style = MaterialTheme.typography.titleMedium)
            Text("综合 ${rec.combinedScore.toInt()}分",
                style = MaterialTheme.typography.bodySmall)
        }
    }
}
