package com.mahjong.helper.engine

import com.mahjong.helper.engine.model.*

/**
 * Per-opponent analysis: discard patterns, hand direction, tenpai detection.
 */
data class OpponentAnalysis(
    val playerIndex: Int,
    val discardsCount: Int,
    val suitDistribution: Map<Suit, Int>,       // 弃牌花色分布
    val terminalRatio: Float,                     // 幺九牌占比
    val recentDiscards: List<Tile>,              // 最近3张弃牌
    val estimatedDirection: String,               // 推测手牌走向
    val tenpaiProbability: Float,                 // 听牌概率 0.0-1.0
    val dangerSignals: List<String>,              // 危险信号
    val safeSuits: List<Suit>,                    // 相对安全的花色
    val dangerousTiles: List<String>              // 高危牌
)

/**
 * Global analysis of the table situation.
 */
data class TableAnalysis(
    val roundWind: Int,
    val remainingTiles: Int,
    val wallTiles: List<Tile>,                   // 已绝张的牌(4张全见)
    val opponents: List<OpponentAnalysis>,
    val overallRisk: String,                     // 整体风险评估
    val summary: String                          // 局势总结
)

class OpponentAnalyzer {
    private val safetyAnalyzer = SafetyAnalyzer()

    fun analyzeTable(state: GameState): TableAnalysis {
        val opponents = state.players.mapIndexed { idx, player ->
            analyzeOpponent(idx, player, state)
        }
        val wallTiles = findWallTiles(state)
        val overallRisk = assessOverallRisk(state, opponents)
        val summary = buildSummary(state, opponents, wallTiles)

        return TableAnalysis(
            roundWind = state.roundWind,
            remainingTiles = state.remainingTiles,
            wallTiles = wallTiles,
            opponents = opponents,
            overallRisk = overallRisk,
            summary = summary
        )
    }

    private fun analyzeOpponent(idx: Int, player: PlayerView, state: GameState): OpponentAnalysis {
        val discards = player.discards
        val suitDist = discards.groupBy { it.suit }.mapValues { it.value.size }
            .let { map ->
                Suit.entries.associateWith { map[it] ?: 0 }
            }
        val terminalCount = discards.count { it.isTerminal }
        val terminalRatio = if (discards.isNotEmpty()) terminalCount.toFloat() / discards.size else 0f
        val recentDiscards = discards.takeLast(3)
        val direction = inferDirection(discards, suitDist)
        val tenpaiProb = estimateTenpaiProbability(discards, state.roundWind)
        val dangerSignals = detectDangerSignals(discards, state.roundWind)
        val safeSuits = findSafeSuits(discards, suitDist)
        val dangerousTiles = findDangerousTiles(discards, state)

        return OpponentAnalysis(
            playerIndex = idx,
            discardsCount = discards.size,
            suitDistribution = suitDist,
            terminalRatio = terminalRatio,
            recentDiscards = recentDiscards,
            estimatedDirection = direction,
            tenpaiProbability = tenpaiProb,
            dangerSignals = dangerSignals,
            safeSuits = safeSuits,
            dangerousTiles = dangerousTiles
        )
    }

    /**
     * Infer opponent's hand direction from their discards.
     */
    private fun inferDirection(discards: List<Tile>, suitDist: Map<Suit, Int>): String {
        if (discards.isEmpty()) return "牌局刚开始"

        // Check for flush (清一色) tendency: discarding heavily in 2 suits
        val suitsWithDiscards = suitDist.filter { it.value > 0 }.size
        val totalDiscards = discards.size
        val maxSuitDiscards = suitDist.maxOf { it.value }
        val maxSuitRatio = if (totalDiscards > 0) maxSuitDiscards.toFloat() / totalDiscards else 0f

        if (totalDiscards >= 5 && suitsWithDiscards >= 2 && maxSuitRatio > 0.6) {
            val remainingSuits = suitDist.filter { it.value < maxSuitDiscards }.keys
            return "可能在做${remainingSuits.joinToString("")}的清一色"
        }

        // High terminal ratio → likely honor/tanyao focus
        val terminalRatio = discards.count { it.isTerminal }.toFloat() / totalDiscards
        if (terminalRatio > 0.4 && totalDiscards >= 4) {
            return "偏向断幺九(大量打出幺九牌)"
        }

        // Middle tile heavy discards → likely going for sequences
        val middleRatio = discards.count { it.isMiddle }.toFloat() / totalDiscards
        if (middleRatio > 0.5 && totalDiscards >= 4) {
            return "偏重顺子型(打出较多中张)"
        }

        if (totalDiscards >= 3) return "牌型尚不明确"
        return "弃牌太少，难以判断"
    }

    /**
     * Estimate probability that opponent is tenpai.
     */
    private fun estimateTenpaiProbability(discards: List<Tile>, roundWind: Int): Float {
        if (discards.isEmpty()) return 0f

        var prob = 0f

        // Base probability from round wind
        prob += when {
            roundWind < 5 -> 0.05f
            roundWind < 9 -> 0.20f
            roundWind < 13 -> 0.40f
            else -> 0.60f
        }

        // Check for "tsumogiri" pattern (drawing and discarding same tile quickly)
        if (discards.size >= 2) {
            val lastTwo = discards.takeLast(2)
            // If last 2 discards are unrelated (different suits), likely tsumogiri
            if (lastTwo[0].suit != lastTwo[1].suit) {
                prob += 0.15f
            }
        }

        // Many middle tiles discarded late → might be tenpai
        val recent3 = discards.takeLast(3)
        val recentMiddleCount = recent3.count { it.isMiddle }
        if (recentMiddleCount >= 2 && roundWind >= 6) {
            prob += 0.20f
        }

        // Discarding a tile that was previously held (tedashi-like pattern)
        if (discards.size >= 4) {
            val earlier = discards.take(discards.size - 2).toSet()
            val recent = discards.takeLast(2)
            if (recent.none { it in earlier } && roundWind >= 5) {
                prob += 0.10f
            }
        }

        return prob.coerceIn(0f, 0.95f)
    }

    private fun detectDangerSignals(discards: List<Tile>, roundWind: Int): List<String> {
        val signals = mutableListOf<String>()
        if (discards.isEmpty()) return signals

        // Late game signals
        if (roundWind >= 8) {
            val last3 = discards.takeLast(3)
            // Tsumogiri pattern in late game
            if (last3.map { it.suit }.distinct().size >= 2) {
                signals.add("连续摸打(弃牌节奏一致)，可能已听牌")
            }
            // Discarding valuable middle tiles late
            if (last3.any { it.isMiddle }) {
                signals.add("晚巡打出中张，注意放炮风险")
            }
        }

        // Many discards but few terminals → holding terminals for pair/yaku
        if (discards.size >= 6) {
            val terminalCount = discards.count { it.isTerminal }
            if (terminalCount < 2) {
                signals.add("很少打幺九牌，可能有幺九对子或刻子")
            }
        }

        if (roundWind >= 10 && discards.size >= 8) {
            signals.add("进入残局，警惕放炮")
        }

        return signals
    }

    private fun findSafeSuits(discards: List<Tile>, suitDist: Map<Suit, Int>): List<Suit> {
        // Suits where opponent has discarded many tiles → they likely don't need them
        return suitDist.filter { it.value >= 3 }.keys.toList()
    }

    private fun findDangerousTiles(discards: List<Tile>, state: GameState): List<String> {
        val dangerous = mutableListOf<String>()
        if (discards.isEmpty()) return dangerous

        // Tiles near discarded tiles (suji relation) that HAVEN'T been discarded
        val safeNumbers = discards.map { it.number to it.suit }.toSet()
        for (suit in Suit.entries) {
            val suitDiscards = discards.filter { it.suit == suit }.map { it.number }.toSet()
            // Unopened middle numbers in this suit are dangerous
            for (num in 2..8) {
                if (num !in suitDiscards && suitDiscards.isNotEmpty()) {
                    val neighborsDiscarded = (num - 1 in suitDiscards) || (num + 1 in suitDiscards)
                    if (neighborsDiscarded) {
                        dangerous.add("${num}${suit.display}")
                    }
                }
            }
            if (dangerous.size >= 5) break
        }
        return dangerous.take(5)
    }

    /**
     * Find "wall" tiles — tiles where all 4 copies are accounted for.
     */
    private fun findWallTiles(state: GameState): List<Tile> {
        val allSeen = state.allVisibleTiles()
        val counts = allSeen.groupBy { it.id }.mapValues { it.value.size }
        return counts.filter { it.value >= 4 }.keys.mapNotNull { Tile.parse(it) }
    }

    private fun assessOverallRisk(state: GameState, opponents: List<OpponentAnalysis>): String {
        val maxTenpaiProb = opponents.maxOfOrNull { it.tenpaiProbability } ?: 0f
        val dangerCount = opponents.sumOf { it.dangerSignals.size }

        return when {
            maxTenpaiProb > 0.7 -> "高危 — 至少一家极可能已听牌"
            maxTenpaiProb > 0.4 -> "中等风险 — 有人可能接近听牌"
            maxTenpaiProb > 0.2 -> "低风险 — 局面尚早"
            else -> "安全 — 牌局初期"
        }
    }

    private fun buildSummary(state: GameState, opponents: List<OpponentAnalysis>, wallTiles: List<Tile>): String {
        val sb = StringBuilder()

        // Round status
        sb.append("第${state.roundWind + 1}巡·")
        sb.append("剩${state.remainingTiles}张·")
        sb.append(if (state.isDealer) "庄家" else "闲家")

        // Wall tiles
        if (wallTiles.isNotEmpty()) {
            sb.append("\n已绝张: ${wallTiles.take(5).joinToString(" ")}")
        }

        // Per-opponent summary
        opponents.forEachIndexed { idx, opp ->
            if (opp.dangerSignals.isNotEmpty()) {
                sb.append("\n对家${idx + 1}: ${opp.dangerSignals.first()}")
            }
        }

        return sb.toString()
    }
}
