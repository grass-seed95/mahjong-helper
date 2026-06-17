package com.mahjong.helper.engine

import com.mahjong.helper.engine.model.*
import org.junit.Assert.*
import org.junit.Test

class SafetyAnalyzerTest {
    private val analyzer = SafetyAnalyzer()

    @Test
    fun `tile already discarded by opponent is completely safe`() {
        val playerDiscards = listOf(Tile(Suit.M, 4))
        val score = analyzer.tileDangerScore(
            Tile(Suit.M, 4), playerDiscards, roundWind = 5
        )
        assertEquals(0.0, score, 0.01)
    }

    @Test
    fun `suji tile has lower danger`() {
        // 对手打4万 → 1万和7万是筋牌，相对安全
        val playerDiscards = listOf(Tile(Suit.M, 4))
        val score1m = analyzer.tileDangerScore(
            Tile(Suit.M, 1), playerDiscards, roundWind = 5
        )
        val score7m = analyzer.tileDangerScore(
            Tile(Suit.M, 7), playerDiscards, roundWind = 5
        )
        assertTrue("1m should be safe (suji of 4m), got $score1m", score1m < 0.50)
        assertTrue("7m should be safe (suji of 4m), got $score7m", score7m < 0.50)
    }

    @Test
    fun `unseen middle tile in late game is dangerous`() {
        val playerDiscards = listOf(Tile(Suit.P, 1), Tile(Suit.P, 9), Tile(Suit.S, 2))
        val score = analyzer.tileDangerScore(
            Tile(Suit.M, 5), playerDiscards, roundWind = 12
        )
        assertTrue("unseen middle tile late game should be >0.50, got $score", score > 0.50)
    }

    @Test
    fun `terminal tiles are safer than middle tiles`() {
        val playerDiscards = emptyList<Tile>()
        val scoreMiddle = analyzer.tileDangerScore(
            Tile(Suit.M, 5), playerDiscards, roundWind = 8
        )
        val scoreTerminal = analyzer.tileDangerScore(
            Tile(Suit.M, 1), playerDiscards, roundWind = 8
        )
        assertTrue("terminal($scoreTerminal) should be safer than middle($scoreMiddle)",
            scoreMiddle > scoreTerminal)
    }

    @Test
    fun `overallSafety returns 0-100`() {
        val hand = Hand.fromString("1m,2m,3m,4m,5m,6p,7p,8p,2s,2s,9p,1s,3s,5p")
        val gameState = GameState(hand)
        val score = analyzer.overallSafety(Tile(Suit.M, 5), gameState)
        assertTrue("safety score should be 0-100, got $score", score in 0..100)
    }
}
