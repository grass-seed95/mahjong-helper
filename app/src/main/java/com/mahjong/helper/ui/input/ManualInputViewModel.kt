package com.mahjong.helper.ui.input

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mahjong.helper.MahjongApp
import com.mahjong.helper.data.dao.GameRecordDao
import com.mahjong.helper.data.entity.GameRecordEntity
import com.mahjong.helper.data.entity.RoundEntity
import com.mahjong.helper.engine.DiscardAdvisor
import com.mahjong.helper.engine.DiscardRecommendation
import com.mahjong.helper.engine.ShantenCalculator
import com.mahjong.helper.engine.OpponentAnalyzer
import com.mahjong.helper.engine.TableAnalysis
import com.mahjong.helper.engine.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InputUiState(
    val tiles: List<Tile> = emptyList(),
    val inputText: String = "",
    val parsedHand: Hand? = null,
    val recommendations: List<DiscardRecommendation> = emptyList(),
    val bestDiscard: DiscardRecommendation? = null,
    val shanten: Int = -1,
    val tableAnalysis: TableAnalysis? = null,
    val error: String? = null,
    val analyzed: Boolean = false
)

class ManualInputViewModel(application: Application) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(InputUiState())
    val state: StateFlow<InputUiState> = _state

    private val dao: GameRecordDao
    private val advisor = DiscardAdvisor()
    private val shantenCalc = ShantenCalculator()
    private val opponentAnalyzer = OpponentAnalyzer()

    private var currentGameId: Long? = null
    private var roundNumber = 0

    init {
        val app = application as MahjongApp
        dao = app.database.gameRecordDao()
    }

    fun addTile(tile: Tile) {
        _state.update { it.copy(tiles = it.tiles + tile, error = null) }
    }

    fun removeTile(index: Int) {
        val newTiles = _state.value.tiles.toMutableList().apply { removeAt(index) }
        _state.update { it.copy(tiles = newTiles, error = null) }
    }

    fun parseAndAnalyze() {
        val tiles = _state.value.tiles
        if (tiles.size !in 13..14) {
            _state.update { it.copy(error = "需要13或14张手牌") }
            return
        }

        val drawnTile = if (tiles.size == 14) tiles.last() else null
        val freeTiles = if (drawnTile != null) tiles.dropLast(1) else tiles
        val hand = Hand(freeTiles.sorted(), drawnTile = drawnTile)
        val gameState = GameState(hand, roundWind = roundNumber)
        val shanten = shantenCalc.shanten(hand)
        val recommendations = advisor.recommend(hand, gameState)
        val best = advisor.bestDiscard(recommendations)
        val tableAnalysis = opponentAnalyzer.analyzeTable(gameState)

        _state.update {
            it.copy(
                parsedHand = hand,
                recommendations = recommendations,
                bestDiscard = best,
                shanten = shanten,
                tableAnalysis = tableAnalysis,
                error = null,
                analyzed = true
            )
        }

        viewModelScope.launch {
            ensureGame()
            currentGameId?.let { gameId ->
                val round = RoundEntity(
                    gameId = gameId,
                    roundNumber = ++roundNumber,
                    handTiles = hand.allTiles.joinToString(",") { it.id },
                    drawnTile = drawnTile?.id,
                    suggestedDiscard = best?.tile?.id,
                    shantenBefore = shanten,
                    shantenAfter = best?.shantenAfter,
                    acceptanceCount = best?.acceptanceCount,
                    safetyScore = best?.safetyScore,
                    actualDiscard = null,
                    wasOptimal = null,
                    result = null
                )
                dao.insertRound(round)
                dao.updateGame(GameRecordEntity(id = gameId, totalRounds = roundNumber))
            }
        }
    }

    fun recordActualDiscard(actualTile: Tile) {
        val best = _state.value.bestDiscard
        viewModelScope.launch {
            currentGameId?.let { gameId ->
                val wasOptimal = actualTile == best?.tile
                val round = RoundEntity(
                    gameId = gameId,
                    roundNumber = roundNumber,
                    handTiles = _state.value.tiles.joinToString(",") { it.id },
                    actualDiscard = actualTile.id,
                    suggestedDiscard = best?.tile?.id,
                    wasOptimal = wasOptimal,
                    shantenBefore = _state.value.shanten,
                    shantenAfter = best?.shantenAfter,
                    acceptanceCount = best?.acceptanceCount,
                    safetyScore = best?.safetyScore,
                    drawnTile = null,
                    result = null
                )
                dao.insertRound(round)
                dao.updateGame(GameRecordEntity(id = gameId, totalRounds = roundNumber))
            }
        }
    }

    fun finishGame(rank: Int) {
        viewModelScope.launch {
            currentGameId?.let { gameId ->
                dao.updateGame(GameRecordEntity(
                    id = gameId,
                    endedAt = System.currentTimeMillis(),
                    finalRank = rank,
                    totalRounds = roundNumber
                ))
            }
            currentGameId = null
            roundNumber = 0
        }
    }

    private suspend fun ensureGame() {
        if (currentGameId == null) {
            currentGameId = dao.insertGame(GameRecordEntity())
        }
    }
}
