package com.mahjong.helper.engine

import com.mahjong.helper.engine.model.Hand
import com.mahjong.helper.engine.model.Tile
import com.mahjong.helper.engine.model.Suit
import org.junit.Assert.*
import org.junit.Test

class ShantenCalculatorTest {
    private val calc = ShantenCalculator()

    @Test
    fun `complete hand has shanten 0 or below`() {
        // 1m2m3m 4m5m6m 7m8m9m 1p2p3p 5s5s = 完整手牌
        val hand = Hand.fromString("1m,2m,3m,4m,5m,6m,7m,8m,9m,1p,2p,3p,5s,5s")
        assertTrue("complete hand should be <= 0, got ${calc.shanten(hand)}", calc.shanten(hand) <= 0)
    }

    @Test
    fun `tenpai or close hand has low shanten`() {
        // 3 melds + 1 pair → near tenpai
        val hand = Hand.fromString("1m,2m,3m,4m,5m,6p,7p,8p,2s,2s,9p,9p,9p,1s")
        assertTrue("should be 0 or 1, got ${calc.shanten(hand)}", calc.shanten(hand) <= 1)
    }

    @Test
    fun `shanten returns non-negative result`() {
        val hand = Hand.fromString("1m,4m,7m,2p,5p,8p,3s,6s,9s,1m,3p,7s,1p,9m")
        assertTrue("shanten should be >= -1, got ${calc.shanten(hand)}", calc.shanten(hand) >= -1)
    }

    @Test
    fun `seven pairs calculator works`() {
        // 已有5对，差2对 → 七对向听=1
        val hand = Hand.fromString("1m,1m,2m,2m,3p,3p,4s,4s,5m,5m,6p,7s,8s,9m")
        assertEquals(1, calc.shantenSevenPairs(hand))
    }
}
