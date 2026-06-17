package com.mahjong.helper.ui.input

import androidx.lifecycle.ViewModel
import com.mahjong.helper.engine.DiscardAdvisor
import com.mahjong.helper.engine.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class InputUiState(
    val tiles: List<Tile> = emptyList(),
    val inputText: String = "",
    val parsedHand: Hand? = null,
    val error: String? = null
)

class ManualInputViewModel : ViewModel() {
    private val _state = MutableStateFlow(InputUiState())
    val state: StateFlow<InputUiState> = _state
    private val advisor = DiscardAdvisor()

    fun addTile(tile: Tile) {
        val newTiles = _state.value.tiles + tile
        _state.value = _state.value.copy(tiles = newTiles, error = null)
    }

    fun removeTile(index: Int) {
        val newTiles = _state.value.tiles.toMutableList().apply { removeAt(index) }
        _state.value = _state.value.copy(tiles = newTiles, error = null)
    }

    fun parseAndAnalyze() {
        val tiles = _state.value.tiles
        if (tiles.size !in 13..14) {
            _state.value = _state.value.copy(error = "需要13或14张手牌")
            return
        }
        val hand = Hand(tiles.sorted())
        _state.value = _state.value.copy(
            parsedHand = hand,
            error = null
        )
    }
}
