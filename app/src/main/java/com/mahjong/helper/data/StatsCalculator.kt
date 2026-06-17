package com.mahjong.helper.data

import com.mahjong.helper.data.dao.GameRecordDao

data class PlayerStats(
    val totalGames: Int,
    val firstPlaceRate: Float,
    val avgRank: Float,
    val dealInRate: Float,
    val optimalRate: Float
)

class StatsCalculator(private val dao: GameRecordDao) {

    suspend fun calculate(): PlayerStats {
        val totalGames = dao.completedGamesCount()
        val ranks = dao.allRanks()
        val firstPlaces = ranks.count { it == 1 }
        val avgRank = if (ranks.isNotEmpty()) ranks.average().toFloat() else 0f
        val totalDecisions = dao.optimalDecisionsCount() + dao.suboptimalDecisionsCount()
        val optimalRate = if (totalDecisions > 0)
            dao.optimalDecisionsCount().toFloat() / totalDecisions else 0f

        return PlayerStats(
            totalGames = totalGames,
            firstPlaceRate = if (totalGames > 0) firstPlaces.toFloat() / totalGames else 0f,
            avgRank = avgRank,
            dealInRate = 0f,
            optimalRate = optimalRate
        )
    }
}
