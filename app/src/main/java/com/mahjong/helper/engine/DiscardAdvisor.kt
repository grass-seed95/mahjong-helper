package com.mahjong.helper.engine

import com.mahjong.helper.engine.model.*

data class DiscardRecommendation(
    val tile: Tile,
    val offenseScore: Double,       // 0-100
    val safetyScore: Int,           // 0-100
    val combinedScore: Double,      // 0-100
    val acceptanceCount: Int,
    val shantenAfter: Int,
    val reason: String
)

class DiscardAdvisor {
    private val ukeireCalc = UkeireCalculator()
    private val safetyAnalyzer = SafetyAnalyzer()

    fun recommend(hand: Hand, state: GameState): List<DiscardRecommendation> {
        val ukeireResults = ukeireCalc.calculate(hand, state)

        val maxAcceptance = ukeireResults.maxOfOrNull { it.acceptanceCount } ?: 1

        return ukeireResults.map { ukeire ->
            val offenseScore = if (maxAcceptance > 0)
                (ukeire.acceptanceCount.toDouble() / maxAcceptance * 100) else 0.0
            val safetyScore = safetyAnalyzer.overallSafety(ukeire.tile, state)
            val weight = attackWeight(hand, state)
            val combinedScore = offenseScore * weight + safetyScore * (1.0 - weight)
            val reason = buildReason(ukeire, safetyScore)

            DiscardRecommendation(
                tile = ukeire.tile,
                offenseScore = offenseScore,
                safetyScore = safetyScore,
                combinedScore = combinedScore,
                acceptanceCount = ukeire.acceptanceCount,
                shantenAfter = ukeire.shantenAfter,
                reason = reason
            )
        }.sortedByDescending { it.combinedScore }
    }

    fun bestDiscard(recommendations: List<DiscardRecommendation>): DiscardRecommendation? =
        recommendations.firstOrNull()

    private fun attackWeight(hand: Hand, state: GameState): Double {
        val shantenCalc = ShantenCalculator()
        val shanten = shantenCalc.shanten(hand)

        if (shanten == 0) return 0.85
        if (shanten == 1) return 0.65
        if (shanten == 2) return 0.50
        return 0.35
    }

    private fun buildReason(
        ukeire: DiscardOption,
        safetyScore: Int
    ): String = when {
        ukeire.acceptanceCount >= 20 && safetyScore >= 80 ->
            "高效且安全，进张${ukeire.acceptanceCount}张"
        ukeire.acceptanceCount >= 20 ->
            "进张多(${ukeire.acceptanceCount}张)但需注意安全"
        safetyScore >= 80 ->
            "安全但进张较少(${ukeire.acceptanceCount}张)"
        else ->
            "进张${ukeire.acceptanceCount}张，安全${safetyScore}分"
    }
}
