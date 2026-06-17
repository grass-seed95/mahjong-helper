package com.mahjong.helper.engine

import com.mahjong.helper.engine.model.Hand
import com.mahjong.helper.engine.model.Tile

class ShantenCalculator {

    fun shanten(hand: Hand): Int {
        val tiles = hand.allTiles
        val normalShanten = calculateNormalShanten(tiles)
        val sevenPairsShanten = shantenSevenPairs(hand)
        return minOf(normalShanten, sevenPairsShanten)
    }

    private fun calculateNormalShanten(tiles: List<Tile>): Int {
        var best = 8
        val counts = tileCounts(tiles)

        fun dfs(meldCount: Int, pairCount: Int, partialCount: Int, idx: Int) {
            val used = meldCount * 3 + pairCount * 2 + partialCount
            if (used > tiles.size) return
            best = minOf(best, 8 - 2 * meldCount - pairCount - partialCount)
            if (idx >= 27) return

            val cnt = counts[idx]
            if (cnt == 0) { dfs(meldCount, pairCount, partialCount, idx + 1); return }

            // 刻子
            if (cnt >= 3) {
                counts[idx] -= 3
                dfs(meldCount + 1, pairCount, partialCount, idx)
                counts[idx] += 3
            }

            // 顺子 (同花色连续)
            val num = idx % 9
            if (num < 7 && counts[idx + 1] > 0 && counts[idx + 2] > 0) {
                counts[idx]--; counts[idx + 1]--; counts[idx + 2]--
                dfs(meldCount + 1, pairCount, partialCount, idx)
                counts[idx]++; counts[idx + 1]++; counts[idx + 2]++
            }

            // 对子
            if (cnt >= 2 && pairCount == 0) {
                counts[idx] -= 2
                dfs(meldCount, 1, partialCount, idx)
                counts[idx] += 2
            }

            // 搭子 (两面/边张/坎张)
            if (partialCount < 4 - meldCount) {
                if (num < 8) {  // 两面或边张
                    counts[idx]--; counts[idx + 1]--
                    dfs(meldCount, pairCount, partialCount + 1, idx)
                    counts[idx]++; counts[idx + 1]++
                }
                if (num < 7) {  // 坎张
                    counts[idx]--; counts[idx + 2]--
                    dfs(meldCount, pairCount, partialCount + 1, idx)
                    counts[idx]++; counts[idx + 2]++
                }
            }

            // 单张（跳过这个index）
            dfs(meldCount, pairCount, partialCount, idx + 1)
        }

        dfs(0, 0, 0, 0)
        return best
    }

    fun shantenSevenPairs(hand: Hand): Int {
        val pairs = tileCounts(hand.allTiles).count { it >= 2 }
        return 6 - pairs
    }

    private fun tileCounts(tiles: List<Tile>): IntArray {
        val counts = IntArray(27)  // 0..8=m, 9..17=p, 18..26=s
        for (t in tiles) counts[t.ordinal]++
        return counts
    }
}
