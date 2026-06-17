package com.mahjong.helper.engine

import com.mahjong.helper.engine.model.*
import org.junit.Assert.*
import org.junit.Test

class DiscardAdvisorTest {
    private val advisor = DiscardAdvisor()

    @Test
    fun `returns recommendation for every tile in hand`() {
        val hand = Hand.fromString("1m,2m,3m,4m,5m,6p,7p,8p,2s,2s,9p,1s,3s,5p")
        val state = GameState(hand)
        val result = advisor.recommend(hand, state)
        assertEquals(hand.freeTiles.size, result.size)
    }

    @Test
    fun `best discard has highest combined score`() {
        val hand = Hand.fromString("1m,2m,3m,4m,5m,6p,7p,8p,2s,2s,9p,9p,5s,5s")
        val state = GameState(hand)
        val result = advisor.recommend(hand, state)
        val best = advisor.bestDiscard(result)
        assertNotNull(best)
        val bestScore = result.maxOf { it.combinedScore }
        assertEquals(bestScore, best!!.combinedScore, 0.01)
    }

    @Test
    fun `combined score is between 0 and 100`() {
        val hand = Hand.fromString("1m,2m,3m,4m,5m,6m,7m,8m,9m,1p,2p,3p,5s,5s")
        val state = GameState(hand)
        val result = advisor.recommend(hand, state)
        for (r in result) {
            assertTrue("score ${r.combinedScore} should be 0-100", r.combinedScore in 0.0..100.0)
        }
    }

    @Test
    fun `recommendation includes reason string`() {
        val hand = Hand.fromString("1m,2m,3m,4m,5m,6p,7p,8p,2s,2s,9p,1s,3s,5p")
        val state = GameState(hand)
        val result = advisor.recommend(hand, state)
        for (r in result) {
            assertTrue("reason should not be empty", r.reason.isNotEmpty())
        }
    }

    @Test
    fun `results are sorted by combined score descending`() {
        val hand = Hand.fromString("1m,2m,3m,4m,5m,6p,7p,8p,2s,2s,9p,9p,5s,5s")
        val state = GameState(hand)
        val result = advisor.recommend(hand, state)
        for (i in 0 until result.size - 1) {
            assertTrue("should be sorted descending", result[i].combinedScore >= result[i + 1].combinedScore)
        }
    }
}
