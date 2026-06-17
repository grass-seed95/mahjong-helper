package com.mahjong.helper.engine

import com.mahjong.helper.engine.model.*

data class DiscardOption(
    val tile: Tile,
    val acceptanceCount: Int,
    val shantenAfter: Int,
    val improvesHand: Boolean
)

class UkeireCalculator {
    private val shantenCalc = ShantenCalculator()

    fun calculate(hand: Hand, gameState: GameState): List<DiscardOption> {
        return hand.freeTiles.map { tile ->
            evaluateDiscard(hand, tile, gameState)
        }.sortedByDescending { it.acceptanceCount }
    }

    private fun evaluateDiscard(hand: Hand, discard: Tile, state: GameState): DiscardOption {
        val afterDiscard = hand.allTiles.toMutableList()
        afterDiscard.remove(discard)

        val handAfter = Hand(afterDiscard.sorted())
        val baseShanten = shantenCalc.shanten(handAfter)

        var acceptance = 0
        val seenTiles = state.allVisibleTiles().groupBy { it.id }.mapValues { it.value.size }

        for (tile in Tile.allUnique()) {
            val remaining = 4 - (seenTiles[tile.id] ?: 0)
            if (remaining <= 0) continue

            val simulatedDraw = afterDiscard.toMutableList()
            simulatedDraw.add(tile)
            val newHand = Hand(simulatedDraw.sorted())
            val newShanten = shantenCalc.shanten(newHand)

            if (newShanten < baseShanten) {
                acceptance += remaining
            }
        }

        return DiscardOption(
            tile = discard,
            acceptanceCount = acceptance,
            shantenAfter = baseShanten,
            improvesHand = baseShanten <= shantenCalc.shanten(hand)
        )
    }
}
