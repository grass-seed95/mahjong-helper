package com.mahjong.helper.ui.advice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahjong.helper.engine.DiscardAdvisor
import com.mahjong.helper.engine.DiscardRecommendation
import com.mahjong.helper.engine.ShantenCalculator
import com.mahjong.helper.engine.UkeireCalculator
import com.mahjong.helper.engine.model.*

@Composable
fun AdviceScreen(hand: Hand) {
    val advisor = remember { DiscardAdvisor() }
    val state = remember { GameState(hand) }
    val recommendations = remember { advisor.recommend(hand, state) }
    val best = advisor.bestDiscard(recommendations)
    val shantenCalc = remember { ShantenCalculator() }
    val shanten = remember { shantenCalc.shanten(hand) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Text("分析结果", style = MaterialTheme.typography.titleLarge)
        }

        // Best recommendation card
        item {
            best?.let { rec ->
                BestRecommendationCard(rec, shanten)
            }
        }

        // Hand decomposition
        item {
            HandDecompositionCard(hand, shanten)
        }

        // All candidates ranking
        item {
            Text("候选排名", style = MaterialTheme.typography.titleSmall)
        }

        items(recommendations.take(10)) { rec ->
            CandidateRow(rec, isBest = rec.tile == best?.tile)
        }

        // Explanation
        item {
            Spacer(Modifier.height(8.dp))
            ExplanationCard(shanten, recommendations.size)
        }
    }
}

@Composable
private fun BestRecommendationCard(rec: DiscardRecommendation, shanten: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("建议打出", style = MaterialTheme.typography.labelMedium)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    rec.tile.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "综合 ${rec.combinedScore.toInt()} 分",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                rec.reason,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetricChip("进攻分", rec.offenseScore.toInt().toString(), Color(0xFF22C55E))
                MetricChip("安全分", rec.safetyScore.toString(), Color(0xFF3B82F6))
                MetricChip("进张数", "${rec.acceptanceCount}张", Color(0xFFF59E0B))
                MetricChip("向听", "${rec.shantenAfter}", if (shanten == 0) Color(0xFFEF4444) else Color(0xFF8B5CF6))
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HandDecompositionCard(hand: Hand, shanten: Int) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("手牌分析", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            // Display tiles grouped by suit
            val tilesBySuit = hand.allTiles.groupBy { it.suit }
            for (suit in Suit.entries) {
                val tiles = tilesBySuit[suit] ?: emptyList()
                if (tiles.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            suit.display,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.width(24.dp)
                        )
                        Text(
                            tiles.sortedBy { it.number }.joinToString(" ") { it.number.toString() },
                            style = MaterialTheme.typography.bodyMedium,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val statusText = when (shanten) {
                    0 -> "已听牌"
                    1 -> "一向听"
                    2 -> "两向听"
                    else -> "${shanten}向听"
                }
                val statusColor = when (shanten) {
                    0 -> Color(0xFF22C55E)
                    1 -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("向听数", style = MaterialTheme.typography.labelSmall)
                    Text(statusText, fontWeight = FontWeight.Bold, color = statusColor)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("手牌数", style = MaterialTheme.typography.labelSmall)
                    Text("${hand.allTiles.size}张")
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("面子", style = MaterialTheme.typography.labelSmall)
                    Text("${hand.melds.size}副")
                }
            }

            // Drawn tile indicator
            hand.drawnTile?.let { drawn ->
                Spacer(Modifier.height(4.dp))
                Text(
                    "刚摸: ${drawn}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CandidateRow(rec: DiscardRecommendation, isBest: Boolean) {
    val bgColor = if (isBest)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    else
        MaterialTheme.colorScheme.surface

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isBest) {
                    Text("★ ", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
                }
                Text(
                    rec.tile.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("进攻", style = MaterialTheme.typography.labelSmall, color = Color(0xFF22C55E))
                    Text("${rec.offenseScore.toInt()}", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("安全", style = MaterialTheme.typography.labelSmall, color = Color(0xFF3B82F6))
                    Text("${rec.safetyScore}", style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("进张", style = MaterialTheme.typography.labelSmall, color = Color(0xFFF59E0B))
                    Text("${rec.acceptanceCount}", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "${rec.combinedScore.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isBest) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ExplanationCard(shanten: Int, candidateCount: Int) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("策略说明", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))

            val (strategyTitle, strategyDesc) = when {
                shanten == 0 -> "进攻优先" to "已听牌，全力追求和牌。安全度仅作参考，优先打出不影响听牌范围的牌。"
                shanten == 1 -> "偏进攻" to "一向听，需要尽快听牌。兼顾进张效率和安全度，优先打出进张多的牌。"
                shanten == 2 -> "攻守平衡" to "两向听，需同时注意牌效和安全。避免打出高风险的生张。"
                else -> "偏防守" to "距离听牌较远，以防守为主。优先打出安全牌，保留进张面广的搭子。"
            }

            Text(strategyTitle, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(strategyDesc, style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            Text("评分说明", style = MaterialTheme.typography.labelMedium)
            Text(
                "进攻分: 打出这张牌后，进张效率的评分\n" +
                "安全分: 这张牌被别家需要（放炮）的风险\n" +
                "进张数: 打出后剩余牌墙中能改善手牌的张数\n" +
                "综合分 = 进攻分 × 权重 + 安全分 × (1-权重)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
