package com.mahjong.helper.engine

import com.mahjong.helper.engine.model.*
import org.junit.Assert.*
import org.junit.Test

class UkeireCalculatorTest {
    private val calc = UkeireCalculator()

    @Test
    fun `returns recommendation for every free tile`() {
        val hand = Hand.fromString("1m,2m,3m,4m,5m,6p,7p,8p,2s,2s,9p,1s,3s,5p")
        val gameState = GameState(hand)
        val result = calc.calculate(hand, gameState)
        assertEquals(hand.freeTiles.toSet().size, result.size)
    }

    @Test
    fun `acceptance count is non-negative for all tiles`() {
        val hand = Hand.fromString("1m,2m,3m,4m,5m,6m,7m,8m,9m,1p,2p,3p,5s,5s")
        val gameState = GameState(hand)
        val result = calc.calculate(hand, gameState)
        for (r in result) {
            assertTrue("acceptance for ${r.tile} should be >= 0", r.acceptanceCount >= 0)
        }
    }

    @Test
    fun `results are sorted by acceptance descending`() {
        val hand = Hand.fromString("1m,2m,3m,4m,5m,6p,7p,8p,2s,2s,9p,9p,5s,5s")
        val gameState = GameState(hand)
        val result = calc.calculate(hand, gameState)
        for (i in 0 until result.size - 1) {
            assertTrue(result[i].acceptanceCount >= result[i + 1].acceptanceCount)
        }
    }
}
