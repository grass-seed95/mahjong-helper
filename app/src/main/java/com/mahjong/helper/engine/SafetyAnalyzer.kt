package com.mahjong.helper.engine

import com.mahjong.helper.engine.model.*

class SafetyAnalyzer {

    /**
     * 对单个对手评估某张牌的危险度。
     * 返回 0.0 (完全安全) ~ 1.0 (极度危险)
     */
    fun tileDangerScore(
        tile: Tile,
        playerDiscards: List<Tile>,
        roundWind: Int
    ): Double {
        // 1. 对手已打过 = 完全安牌
        if (tile in playerDiscards) return 0.0

        // 2. 筋牌检查
        if (isSujiSafe(tile, playerDiscards)) return 0.20 + roundWind * 0.01

        // 3. 幺九牌天然更安全
        if (tile.isTerminal) return 0.30 + roundWind * 0.01

        // 4. 早巡相对安全
        if (roundWind < 4) return 0.25 + roundWind * 0.02

        // 5. 中张 + 晚巡 = 危险
        if (tile.isMiddle && roundWind >= 8) return 0.75 + (roundWind - 8) * 0.02

        return 0.50 + roundWind * 0.01
    }

    /**
     * 对整个局面评估某张牌的总体安全度。
     * 取三个对手中危险度最高的值，然后归一化为0-100的安全分。
     */
    fun overallSafety(
        tile: Tile,
        gameState: GameState
    ): Int {
        val maxDanger = gameState.players.mapIndexed { idx, player ->
            tileDangerScore(tile, player.discards, gameState.roundWind)
        }.maxOrNull() ?: 0.0

        return ((1.0 - maxDanger.coerceIn(0.0, 1.0)) * 100).toInt()
    }

    private fun isSujiSafe(tile: Tile, discards: List<Tile>): Boolean {
        val num = tile.number
        val suit = tile.suit
        // 筋牌关系：对手打n则n±3相对安全
        return discards.any { it.suit == suit && (it.number == num - 3 || it.number == num + 3) }
    }
}
