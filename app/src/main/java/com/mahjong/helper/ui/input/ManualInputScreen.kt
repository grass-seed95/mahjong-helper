package com.mahjong.helper.ui.input

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahjong.helper.engine.model.Hand
import com.mahjong.helper.engine.model.Suit
import com.mahjong.helper.engine.model.Tile
import com.mahjong.helper.engine.DiscardRecommendation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(
    viewModel: ManualInputViewModel = viewModel(),
    onAnalyze: (Hand) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("输入手牌", style = MaterialTheme.typography.titleLarge)
        Text(
            "点击牌面添加，已选中的牌再次点击可移除",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        // 已输入的手牌
        if (state.tiles.isNotEmpty()) {
            Text(
                "已选 ${state.tiles.size} 张",
                style = MaterialTheme.typography.labelMedium
            )
            LazyRow(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(state.tiles) { index, tile ->
                    AssistChip(
                        onClick = { viewModel.removeTile(index) },
                        label = { Text(tile.toString()) },
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // 快速选牌按钮（按花色分组）
        for (suit in Suit.entries) {
            Text(suit.display, style = MaterialTheme.typography.labelMedium)
            LazyRow {
                items(9) { i ->
                    val tile = Tile(suit, i + 1)
                    FilterChip(
                        selected = false,
                        onClick = { viewModel.addTile(tile) },
                        label = { Text(tile.toString()) },
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(12.dp))
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = { viewModel.parseAndAnalyze() },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.tiles.size in 13..14
        ) {
            Text("分析局势")
        }

        // 分析结果
        if (state.analyzed && state.bestDiscard != null) {
            Spacer(Modifier.height(20.dp))
            AnalysisResultSection(state)
        }
    }
}

@Composable
private fun AnalysisResultSection(state: InputUiState) {
    val best = state.bestDiscard!!

    Text("分析结果", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(12.dp))

    // 最佳推荐卡片
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("建议打出", style = MaterialTheme.typography.labelMedium)
            Text(
                best.tile.toString(),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                best.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatItem("安全度", "${best.safetyScore}")
                StatItem("进张数", "${best.acceptanceCount}")
                StatItem("向听", "${best.shantenAfter}")
                StatItem("综合分", "${best.combinedScore.toInt()}")
            }
        }
    }

    Spacer(Modifier.height(16.dp))
    Text("全部候选", style = MaterialTheme.typography.titleSmall)

    state.recommendations.take(8).forEach { rec ->
        val isBest = rec.tile == best.tile
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            colors = if (isBest) CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) else CardDefaults.cardColors()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    rec.tile.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("攻${rec.offenseScore.toInt()}", style = MaterialTheme.typography.bodySmall)
                    Text("守${rec.safetyScore}", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${rec.combinedScore.toInt()}分",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}
