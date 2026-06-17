package com.mahjong.helper.ui.input

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mahjong.helper.engine.model.Hand
import com.mahjong.helper.engine.model.Suit
import com.mahjong.helper.engine.model.Tile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualInputScreen(
    viewModel: ManualInputViewModel = viewModel(),
    onAnalyze: (Hand) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("输入手牌", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))

        if (state.tiles.isNotEmpty()) {
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
        }

        Spacer(Modifier.height(16.dp))
        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                viewModel.parseAndAnalyze()
                state.parsedHand?.let { onAnalyze(it) }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.tiles.size in 13..14
        ) {
            Text("分析局势")
        }
    }
}
